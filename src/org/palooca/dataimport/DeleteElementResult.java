/*
 * DeleteElementResult.java
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
 * Created on 04. November 2007, 16:28
 *
 */

package org.palooca.dataimport;

import com.sun.star.uno.XComponentContext;
import com.jedox.palojlib.interfaces.IDatabase;
import com.jedox.palojlib.interfaces.IDimension;
import com.jedox.palojlib.interfaces.IElement;

/**
 *
 * @author Andreas Schneider
 */
public class DeleteElementResult extends ImportResult {
    
    protected String servdb;
    protected String dimensionName;
    protected String elementName;
    
    public DeleteElementResult(XComponentContext context, String servdb, String dimensionName, String elementName) {
        super(context);
        this.servdb = servdb;
        this.dimensionName = dimensionName;
        this.elementName = elementName;
        
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
            
            IElement element = dimension.getElementByName(elementName,false);
            if (element == null)
                return false;
            
            dimension.removeElements(new IElement[]{element});
            
            return true;
        } catch( Exception e ) {
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
        final DeleteElementResult other = (DeleteElementResult) obj;
        if ((this.context == null) ? (other.context != null) : !this.context.equals(other.context)) {
            return false;
        }
        if ((this.servdb == null) ? (other.servdb != null) : !this.servdb.equals(other.servdb)) {
            return false;
        }
        if ((this.dimensionName == null) ? (other.dimensionName != null) : !this.dimensionName.equals(other.dimensionName)) {
            return false;
        }
        if ((this.elementName == null) ? (other.elementName != null) : !this.elementName.equals(other.elementName)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.context != null ? this.context.hashCode() : 0);
        hash = 79 * hash + (this.servdb != null ? this.servdb.hashCode() : 0);
        hash = 79 * hash + (this.dimensionName != null ? this.dimensionName.hashCode() : 0);
        hash = 79 * hash + (this.elementName != null ? this.elementName.hashCode() : 0);
        return hash;
    }



    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("PALO.EDELETE(\"");
        result.append(servdb);
        result.append("\"; \"");
        result.append(dimensionName);
        result.append("\"; \"");
        result.append(elementName);
        result.append("\")");
        
        return result.toString();
    }
    
}