/*
 * AddElementResult.java
 *
 * Palo Open Office Calc AddIn
 * Copyright (C) 2008 PalOOCa Team,  Tensegrity Software GmbH, 2009

 * The software is licensed under an Open-Source License (GPL).
 * If you want to redistribute the software you must observe the regulations of
 * the GPL . If you want to redistribute the software without the
 * restrictions of the GPL, you have to contact Tensegrity Software GmbH
 * (Tensegrity) for written consent to do so.
 * Tensegrity may offer commercial licenses for redistribution (Dual Licensing)
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * Created on 04. November 2007, 14:12
 *
 */

package org.palooca.dataimport;

import com.sun.star.uno.Any;
import com.sun.star.uno.Type;
import com.sun.star.uno.XComponentContext;
import com.jedox.palojlib.interfaces.IConsolidation;
import com.jedox.palojlib.interfaces.IDatabase;
import com.jedox.palojlib.interfaces.IDimension;
import com.jedox.palojlib.interfaces.IElement;
import com.jedox.palojlib.exceptions.*;
import org.palooca.ExternalImportListener;
import org.palooca.ImportFunctionListener;
import java.text.NumberFormat;
import java.util.ArrayList;

/**
 *
 * @author Andreas Schneider
 */
public class AddElementResult extends ImportResult {
    
    protected static boolean initialized = false;
    protected static ArrayList<String> clearParameterHandled = new ArrayList<String>();

    protected String servdb;
    protected String dimensionName;
    protected char type;
    protected String elementName;
    protected String parentName;
    protected Double weight;
    protected boolean clear;
    
    public AddElementResult(XComponentContext context, String servdb, String dimensionName, String type, String elementName, Object parentName, Object weight, Double clear) {
        super(context);

        if (initialized == false){
            ImportResult.addExternalImportListener(new ExternalImportListener() {
                @Override
                public void ExternalImportBegin() {
                }
                @Override
                public void ExternalImportEnd() {
                    AddElementResult.ExternalImportEnd();
                }
            });

            ImportResult.addImportFunctionListener(new ImportFunctionListener() {
                @Override
                public void ImportFunctionBegin() {
                }
                @Override
                public void ImportFunctionEnd() {
                    AddElementResult.ImportFunctionEnd();
                }
            });
            initialized = true;
        }

        this.servdb = servdb;
        this.dimensionName = dimensionName;
        this.type = type.length() > 0 ? type.toLowerCase().charAt(0) : 'n';
        this.elementName = elementName;

        if (parentName.equals(new Any(Type.VOID, null)))
            this.parentName = null;
        else if (parentName instanceof Double)
            // A double typically has a .0 ex 123.0.  This strips off the 0 to be 123
            // but will keep 123.1
            this.parentName = NumberFormat.getNumberInstance().format(parentName);
        else this.parentName = parentName.toString();

        this.weight = (weight instanceof Double ? (Double)weight : new Double(1.0));
        this.clear = (clear != 0.0);

        // Element name should show in the cell on import success
        this.value = this.elementName;
    }
    
    @Override
    protected boolean execute() {
        try {
            IDatabase database = connectionHandler.getDatabase(servdb);
            if (database == null)
                return false;
            
            IDimension dimension = database.getDimensionByName(dimensionName);
            if (dimension == null)
                return false;

            //  Would be nice if there was a bulk delete option in the jpalo
            //  api since this can take very long for big dimensions
            if (clear == true && clearParameterHandled.contains(dimensionName) == false)
            {
                dimension.removeElements(dimension.getElements(false));
                clearParameterHandled.add(dimensionName);
            }

            // Find or create the parent
            IElement parent = null;
            if (parentName != null && parentName.length() > 0){
                parent = dimension.getElementByName(parentName,false);

                if (parent == null) {
                    // We need to create the parent as numeric because the server expects
                    // children if you create it as a consolidated element.  The jpalo api
                    // doesn't support creating a consolidated item and giving children at the
                    // moment.
                    // When we set the child later on, the element is converted to consolidated
                    // in any case.
                    dimension.addElements(new String[]{parentName}, new IElement.ElementType[]{IElement.ElementType.ELEMENT_NUMERIC});
                    parent = dimension.getElementByName(parentName, false);
                }
                // Parent was not found and could not be added
                if (parent == null)
                    return false;
            }

            IElement element = dimension.getElementByName(elementName,false);

            // If the element doesn't exist, create it
            if (element == null)
            {
                IElement.ElementType internalType;

                switch (type) {
                    case 't':
                        internalType = IElement.ElementType.ELEMENT_STRING;
                        break;
                    case 'c':
                        // This parameter seems useless.  If you look at the Excel behaviour
                        // the only difference is that you don't get to specify
                        // the element type.  It is defaulted to numeric.  The parent
                        // is always consolidated.
                        internalType = IElement.ElementType.ELEMENT_NUMERIC;
                        break;
                    default:
                        internalType = IElement.ElementType.ELEMENT_NUMERIC;
                        break;
                }
            
                dimension.addElements(new String[]{elementName}, new IElement.ElementType[]{internalType});
                element = dimension.getElementByName(elementName, false);
            }

            // If a parent was specified, add the element as its child
            if (parent != null) {
                boolean updateRequired = true;
                boolean appendChild = true;
                IElement[] children = parent.getChildren();
                ArrayList<IConsolidation> newConsolidations = new ArrayList<IConsolidation>();
                for (int i = 0; i < children.length; i++){
                    // First see if the child is maybe already a child with the correct weight
                    if (children[i].equals(element)){
                        appendChild = false;
                        if (children[i].getWeight(parent) == weight){
                            updateRequired = false;
                            break;
                        }
                        else newConsolidations.add(dimension.newConsolidation(parent, element, weight));
                    }
                    else newConsolidations.add(dimension.newConsolidation(parent, element, children[i].getWeight(parent)));
                }

                // If the child doesn't exist with the same weight already
                if (updateRequired){
                    if (appendChild)
                        newConsolidations.add(dimension.newConsolidation(parent, element, weight));

                    dimension.updateConsolidations(newConsolidations.toArray(new IConsolidation[newConsolidations.size()]));
                }
            }
            
            return true;
        } catch (PaloException pe) {
            error = pe.getLocalizedMessage();
            return false;
        } catch (PaloJException e) {
            error = e.getLocalizedMessage();
            return false;
        } catch( Exception e ) {
            error = e.getLocalizedMessage();
            return false;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AddElementResult other = (AddElementResult) obj;
        if ((this.context == null) ? (other.context != null) : !this.context.equals(other.context)) {
            return false;
        }
        if ((this.servdb == null) ? (other.servdb != null) : !this.servdb.equals(other.servdb)) {
            return false;
        }
        if ((this.dimensionName == null) ? (other.dimensionName != null) : !this.dimensionName.equals(other.dimensionName)) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        if ((this.elementName == null) ? (other.elementName != null) : !this.elementName.equals(other.elementName)) {
            return false;
        }
        if ((this.parentName == null) ? (other.parentName != null) : !this.parentName.equals(other.parentName)) {
            return false;
        }
        if (this.weight != other.weight && (this.weight == null || !this.weight.equals(other.weight))) {
            return false;
        }
        if (this.clear != other.clear) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + (this.context != null ? this.context.hashCode() : 0);
        hash = 47 * hash + (this.servdb != null ? this.servdb.hashCode() : 0);
        hash = 47 * hash + (this.dimensionName != null ? this.dimensionName.hashCode() : 0);
        hash = 47 * hash + this.type;
        hash = 47 * hash + (this.elementName != null ? this.elementName.hashCode() : 0);
        hash = 47 * hash + (this.parentName != null ? this.parentName.hashCode() : 0);
        hash = 47 * hash + (this.weight != null ? this.weight.hashCode() : 0);
        hash = 47 * hash + (this.clear ? 1 : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("PALO.EADD(\"");
        result.append(servdb);
        result.append("\"; \"");
        result.append(dimensionName);
        result.append("\"; \"");
        result.append(type);
        result.append("\"; \"");
        result.append(elementName);
        result.append("\"; \"");
        result.append(parentName);
        result.append("\"; ");
        result.append(weight);
        result.append("; ");
        result.append(clear);
        result.append(")");
        
        return result.toString();
    }

    protected static void ImportFunctionEnd() {
        // If we are in external import mode, the import functions will run for every iteration
        // and we don't want to clear the dimension with every iteration read from file
        if (ImportResult.getExternalImportMode() == false)
            clearParameterHandled.clear();
    }

    protected static void ExternalImportEnd() {
        clearParameterHandled.clear();
    }
    
}