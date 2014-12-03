/*
 * PalOOCa.java
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
 */

package org.palooca;

import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.uno.XComponentContext;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.lib.uno.helper.WeakBase;
import java.util.ArrayList;
import com.jedox.palojlib.interfaces.*;
import java.util.HashMap;
import java.util.Map;
import org.palooca.dataimport.*;
import org.palooca.network.ConnectionHandler;

public final class PalOOCaImpl extends WeakBase
   implements com.sun.star.lang.XServiceInfo,
              com.sun.star.lang.XLocalizable,
              org.palooca.XPalOOCa
{
    private final XComponentContext xContext;
    
    private static final String implementationName = PalOOCaImpl.class.getName();
    private static final String[] serviceNames = new String[] {
        "org.palooca.PalOOCa" };

    private com.sun.star.lang.Locale m_locale = new com.sun.star.lang.Locale();
    
    private PalOOCaManager manager;
    private ConnectionHandler connectionHandler;

    public PalOOCaImpl( XComponentContext context ) {
        xContext = context;
        manager = PalOOCaManager.getInstance(context);
        connectionHandler = manager.getConnectionHandler();
        
    }

    public static XSingleComponentFactory __getComponentFactory(String sImplementationName ) {
        XSingleComponentFactory xFactory = null;

        if ( sImplementationName.equals( implementationName ) )
            xFactory = Factory.createComponentFactory(PalOOCaImpl.class, serviceNames);
        return xFactory;
    }

    public static boolean __writeRegistryServiceInfo(final XRegistryKey registryKey ) {
        return Factory.writeRegistryServiceInfo(implementationName, serviceNames, registryKey);
    }

    // com.sun.star.lang.XServiceInfo:
    @Override
    public String getImplementationName() {
         return implementationName;
    }

    @Override
    public boolean supportsService(String service) {
        int len = serviceNames.length;

        for( int i = 0; i < len; i++) {
            if (service.equals(serviceNames[i]))
                return true;
        }
        return false;
    }

    @Override
    public String[] getSupportedServiceNames() {
        return serviceNames;
    }

    // com.sun.star.lang.XLocalizable:
    @Override
    public void setLocale( com.sun.star.lang.Locale eLocale )
    {
        m_locale = eLocale;
    }

    @Override
    public com.sun.star.lang.Locale getLocale()
    {
        return m_locale;
    }

    // org.openoffice.addin.XPalOOCa:
    
    public static String processElementName(Object elementName) {
        if (elementName instanceof Double)
            return Integer.toString(((Double)elementName).intValue());
        else
            return elementName.toString();
    }
    
    public static String[] processCoordinates(final Object[] coordinates) {
        String[] stringCoords = new String[coordinates.length];
        for (int i = 0; i < coordinates.length; i++)
            stringCoords[i] = processElementName(coordinates[i]);
        return stringCoords;
    }


    private IElement getElement(String servdb, String dimensionName,
                               String elementName) {

        IDimension dimension = manager.getDimension(servdb, dimensionName);
        if ( dimension == null )
            return null;

        IElement element = dimension.getElementByName(elementName,false);
        if ( element == null )
            return null;

        return element;
    }
    
    private String getElementAtOffset(String servdb, String dimensionName,
                                      String elementName, int offset) {
        try {
            IDimension dimension = manager.getDimension(servdb, dimensionName);
            if ( dimension == null )
                return null;
            
            IElement element = dimension.getElementByName(elementName,false);
            if ( element == null )
                return null;
            
            //get all elements of the dimension
            IElement[] elements = dimension.getElements(false);
            int ownID = -1; //the index of the element itself within the siblings
            for (int i = 0; i < elements.length; i++) {
                if (elements[i] == element) {
                    ownID = i;
                    break;
                }
            }
            
            int nextID = (ownID + offset);
            if (nextID < 0 || nextID >= elements.length)
                return null;
            
            return elements[nextID].getName();
            
        } catch( Exception e ) {
            return null;
        }    
    }
    
    /**
     * Retrieves a value from a Palo database.
     * @return Returns the value at the given coordinates.
     * @param servdb Connection and Database name (e.g. localhost/Demo)
     * @param cubeName Name of the desired cube.
     * @param coordinates The coordinates of the desired value.
     */
    @Override
    public Object PALO_DATA(String servdb, String cubeName, Object[] coordinates) {
        try {
            return DataValueHandler.getPaloDataValue(xContext, servdb, cubeName, coordinates);
        } catch( Exception e ) {
            return null;
        }
    }
    
//    public XVolatileResult PALO_DATAC(String servdb, String cubeName, Object[] coordinates) {
//        DataResult data = new DataResult("Test");
//        dataResults.add(data);
//        if (dataResults.size() > 100) {
//            for (int i = 0; i < dataResults.size(); i++) {
//                data = dataResults.get(i);
//                data.incrementValue();
//                data.removeResultListeners();
//            }
//            dataResults.removeAllElements();
//        }
//        return data;
//    }

    @Override
    public Object PALO_DATAC(String servdb, String cubeName, Object[] coordinates) {
        return PALO_DATA(servdb, cubeName, coordinates);
    }
    
    @Override
    public Object PALO_DATAV(String servdb, String cubeName, Object[] coordinates) {
        return PALO_DATA(servdb, cubeName, coordinates);
    }
    
    /**
     * Retrieves a value from a Palo database and allows the UI
     * to edit the content with a special dialog.
     */
    @Override
    public Object PALO_DATAT(String servdb, String cubeName, Object[] coordinates) {
        return PALO_DATA(servdb, cubeName, coordinates);
    }

    private String getAliasElementName(IDimension dimension, String elementName, Object alias) {
        if (alias != null && !alias.toString().isEmpty()) {
            IElement attributedElement = dimension.getElementByName(elementName, true);
            Object attText = attributedElement.getAttributeValue(alias.toString());
            return (attText != null && !attText.toString().isEmpty()) ? attText.toString() : elementName;
        }
        return elementName;
    }
    
    /**
     * Retrieves the name of an element within a dimension.
     * It also offers several undocumented features, which are used to communicate
     * with the UI.
     * @param servdb Connection and Database name (e.g. localhost/Demo)
     * @param dimensionName Name of the desired dimension.
     * @param aindex Default: The index of the desired element.
     * Also: The name of the element to search for.
     * @param afig When searching for an element, this has to be "1".
     * @param apath Is used in conjunction with afig == 0, afig == 2 and afig == 3.
     * @param alias Alias field name to be displayed instead of element name.
     * @return Returns the name of the element.
     */
    @Override
    public String PALO_ENAME(String servdb, String dimensionName, Object aindex, Object afig, Object apath, Object alias) {
        try {
            if (aindex instanceof Double) {
                IDimension dimension = manager.getDimension(servdb, dimensionName);
                if ( dimension == null )
                    return null;
                
                int index = ((Double)aindex).intValue();
                index--;
                IElement element = dimension.getElements(false)[index];
                if ( element == null )
                    return null;

                return getAliasElementName(dimension, element.getName(), alias); // TODO
            } else if (aindex instanceof String) {
                /*int fig = 0;
                if (afig instanceof Double)
                    fig = ((Double)afig).intValue();
                if (fig < 0 || fig == 1 || fig > 3)*/ //doesn't matter currently
                    return PALO_ESIBLING(servdb, dimensionName, (String)aindex, 0, alias);
            }
            
            return "#INVALID PARAMETERS";
        } catch( Exception e ) {
            return null;
        }
    }
    
    /**
     * Retrieves the name of the first element within a dimension.
     * @return Returns the name of an element.
     * @param servdb Connection and Database name (e.g. localhost/Demo)
     * @param dimensionName Name of the desired dimension.
     */
    @Override
    public String PALO_EFIRST(String servdb, String dimensionName) {
       return PALO_ENAME(servdb, dimensionName, new Double(1), null, null, null);
    }

    /**
     * Retrieves the name of the next element within a dimension, relative to a given element.
     * @return Returns the name of an element.
     * @param servdb Connection and Database name (e.g. localhost/Demo)
     * @param dimensionName Name of the desired dimension.
     * @param elementName Name of the element.
     */
    @Override
    public String PALO_ENEXT(String servdb, String dimensionName, String elementName) {
        return getElementAtOffset(servdb, dimensionName, elementName, 1);
    }
    
    /**
     * Retrieves the name of the previous element within a dimension, relative to a given element.
     * @return Returns the name of an element.
     * @param servdb Connection and Database name (e.g. localhost/Demo)
     * @param dimensionName Name of the desired dimension.
     * @param elementName Name of the element.
     */
    @Override
    public String PALO_EPREV(String servdb, String dimensionName, String elementName) {
        return getElementAtOffset(servdb, dimensionName, elementName, -1);
    }

    /**
     * Retrieves the name of the child element within a dimension, relative to a given element, specified by an index.
     * @return Returns the name of an element.
     * @param servdb Connection and Database name (e.g. localhost/Demo)
     * @param dimensionName Name of the desired dimension.
     * @param elementName Name of the element.
     * @param childID The index of the child.
     */
    @Override
    public String PALO_ECHILD(String servdb, String dimensionName,
                              String elementName, int childID) {
        try {
            IElement element = getElement(servdb, dimensionName, elementName);
            if ( element == null )
                return null;
            
            IElement[] children = element.getChildren();
            childID--;
            if ( children == null || children.length <= childID )
                return null;
            
            return children[childID].getName();
            
        } catch( Exception e ) {
            return null;
        }
    }
    
    /**
     * Returns the name of the parent element at the given index.
     * @return The name of the parent element.
     * @param servdb Connection and Database name (e.g. localhost/Demo)
     * @param dimensionName The name of the dimension.
     * @param elementName The name of the child element.
     * @param parentNum The index of the parent element.
     */
    @Override
    public String PALO_EPARENT(String servdb, String dimensionName,
                               String elementName, int parentNum) {
        try {
            IElement element = getElement(servdb, dimensionName, elementName);
            if ( element == null )
                return null;
            
            IElement[] parents = element.getParents();
            parentNum--;
            if ( parents == null || parents.length <= parentNum )
                return null;
            
            return parents[parentNum].getName();
            
        } catch( Exception e ) {
            return null;
        } 
    }

    /**
     * Retrieves the name of the sibling at a given offset of an element within a dimension.
     * A sibling is an element with the same depth as the given element.
     * @return Returns the name of an element.
     * @param servdb Connection and Database name (e.g. localhost/Demo)
     * @param dimensionName Name of the desired dimension.
     * @param elementName Name of the element.
     * @param siblingNum The offset of the sibling.
     */
    @Override
    public String PALO_ESIBLING(String servdb, String dimensionName,
                                String elementName, int siblingNum, Object alias) {
        try {
            IDimension dimension = manager.getDimension(servdb, dimensionName);
            if ( dimension == null )
                return null;
            
            IElement element = dimension.getElementByName(elementName,false);
            if ( element == null )
                return null;

            // Element exists and you want the current item
            // Happens when the UI generates the PALO.ENAME functions
            if (siblingNum == 0)
                return getAliasElementName(dimension, elementName, alias);

            // If a root element
            if (element.getParentCount() == 0){
                Object currentPosition = PALO_EINDEX(servdb, dimensionName, elementName);

                if (currentPosition == null)
                    return null;

                // PALO_EINDEX returns a 1-based number.  We need a 0-based number
                int position = ((Double)currentPosition).intValue() - 1;

                if (position + siblingNum >= dimension.getElements(false).length)
                    return null;
                
                return dimension.getElements(false)[position + siblingNum].getName();
            }
            else {
                // Loop through parents, counting down the sibling offset
                // until the offset is 0 and return that element
                int adjustedSibNum = siblingNum;
                parent_loop:
                for (IElement parent : element.getParents()){
                    int relativePos = 0;
                    boolean startSearching = false;
                    for(IElement child : parent.getChildren()){
                        if (startSearching == false &&
                                child.equals(element)){

                            // If the parent doesn't have enough children left to satisfy
                            // the offset, go over to the next parent
                            int siblingsLeft = parent.getChildCount() - relativePos - 1;
                            if (adjustedSibNum > siblingsLeft){
                                adjustedSibNum = adjustedSibNum - siblingsLeft;
                                continue parent_loop;
                            }

                            startSearching = true;
                        }
                        
                        if (startSearching){
                            if (adjustedSibNum == 0){
                                if (alias != null && alias instanceof String) {
                                    String aliasName = (String)alias;
                                    if (aliasName.length() == 0) {
                                        return child.getName();
                                    } else {
                                        IAttribute attr = dimension.getAttributeByName((String)alias);
                                        IElement attributedChild = dimension.getElementByName(child.getName(), true);
                                        String value = attributedChild.getAttributeValue(attr.getName()).toString();
                                        if (value.length() == 0)
                                            return child.getName();
                                        else
                                            return value;
                                    }
                                }
                                else return child.getName();
                            }
                            adjustedSibNum = adjustedSibNum - 1;
                        }
                        relativePos = relativePos + 1;
                    }
                }
            }
            return null;

        } catch( Exception e ) {
            return null;
        }
    }
    
    /**
     * Retrieves the index of the element within all elements of a dimension.
     * @return Returns the index of an element.
     * @param servdb Connection and Database name (e.g. localhost/Demo)
     * @param dimensionName Name of the desired dimension.
     * @param elementName Name of the element to query.
     */
    @Override
    public Object PALO_EINDEX(String servdb, String dimensionName, String elementName) {
        try {
            IDimension dimension = manager.getDimension(servdb, dimensionName);
            if ( dimension == null )
                return null;
            
           IElement[] elements = dimension.getElements(false);
           int index = -1;
           for (int i = 0; i < elements.length && index == -1; i++)
               if (elementName.compareTo(elements[i].getName()) == 0)
                   index = i;
           
           if (index == -1)
               return null;
           else
               return new Double(index + 1);
            
        } catch( Exception e ) {
            return null;
        }
    }

    /**
     * Retrieves the number of elements within a dimension.
     * @return Returns the number of child elements.
     * @param servdb Connection and Database name (e.g. localhost/Demo)
     * @param dimensionName Name of the desired dimension.
     */
    @Override
    public Object PALO_ECOUNT(String servdb, String dimensionName) {
        try {

//            String[][] strs = new String[1][2];
//
//            strs[0][0] = new String("Test1");
//            strs[0][1] = new String("Test2");
//            return strs;

            IDimension dimension = manager.getDimension(servdb, dimensionName);
            if ( dimension == null )
                return null;

            IElement[] elements = dimension.getElements(false);
            return new Double(elements.length);
            
        } catch( Exception e ) {
            return null;
        }
    }

    /**
     * Retrieves the number of child elements of an element within a dimension.
     * @return Returns the number of child elements.
     * @param servdb Connection and Database name (e.g. localhost/Demo)
     * @param dimensionName Name of the desired dimension.
     * @param elementName Name of the parent element.
     */
    @Override
    public Object PALO_ECHILDCOUNT(String servdb, String dimensionName, String elementName) {
        try {
            IElement element = getElement(servdb, dimensionName, elementName);
            if ( element == null )
                return null;
            
            IElement[] children = element.getChildren();
            return new Double(children.length);
            
        } catch( Exception e ) {
            return null;
        }
    }
    
    /**
     * Returns the number of parents a certain element has.
     * @return The number of parents.
     * @param servdb Connection and Database name (e.g. localhost/Demo)
     * @param dimensionName The name of the dimension.
     * @param elementName The name of the child element.
     */
    @Override
    public Object PALO_EPARENTCOUNT(String servdb, String dimensionName, String elementName) {
        try {
            IElement element = getElement(servdb, dimensionName, elementName);
            if ( element == null )
                return null;
            
            return new Double(element.getParentCount());
            
        } catch( Exception e ) {
            return null;
        } 
    }

   


    /**
     * Retrieves the depth of a certain element within a dimension.
     * @return Returns the depth of the element.
     * @param servdb Connection and Database name (e.g. localhost/Demo)
     * @param dimensionName Name of the desired dimension.
     * @param elementName Name of the element to query.
     */
    @Override
    public Object PALO_EINDENT(String servdb, String dimensionName, String elementName){
        try {
            IElement element = getElement(servdb, dimensionName, elementName);
            if ( element == null )
                return null;
            
            return new Double(PaloLibUtil.getElementDepth(element) + 1);
            
        } catch( Exception e ) {
            return null;
        }
    }

    /**
     * Checks if a given element has a certain parent.
     * @return 1 if the element is a child of the given parent, 0 if this is not the case.
     * @param servdb Connection and Database name (e.g. localhost/Demo)
     * @param dimensionName The name of the dimension.
     * @param parentName The name of the parent element.
     * @param childName The name of the child element.
     */
    @Override
    public Object PALO_EISCHILD(String servdb, String dimensionName,
                                String parentName, String childName) {
        try {
            IElement parent = getElement(servdb, dimensionName, parentName);
            IElement child = getElement(servdb, dimensionName, childName);
            if ( parent == null || child == null )
                return null;
            
            IElement[] children = parent.getChildren();
            boolean found = false;
            for (int i = 0; i < children.length && !found; i++)
                if (children[i] == child)
                    found = true;
            
            if (found)
                return new Double(1);
            else
                return new Double(0);
            
        } catch( Exception e ) {
            return null;
        }
    }

    /**
     * Returns the level of a certain element within the consolidation hierachy.
     * @return The level of the element.
     * @param servdb Connection and Database name (e.g. localhost/Demo)
     * @param dimensionName The name of the dimension.
     * @param elementName The name of the element.
     */
    @Override
    public Object PALO_ELEVEL(String servdb, String dimensionName, String elementName) {
        try {
            IElement element = getElement(servdb, dimensionName, elementName);
            if ( element == null )
                return null;
            
            return new Double(PaloLibUtil.getElementDepth(element));
            
        } catch( Exception e ) {
            return null;
        }
    }

    /**
     * Returns the type of the element.
     * @return A string representation of the type of the given element.
     * @param servdb Connection and Database name (e.g. localhost/Demo)
     * @param dimensionName The name of the dimension.
     * @param elementName The name of the element.
     */
    @Override
    public String PALO_ETYPE(String servdb, String dimensionName, String elementName) {
        try {
            IElement element = getElement(servdb, dimensionName, elementName);
            if ( element == null )
                return null;

            switch (element.getType()) {
                case ELEMENT_NUMERIC: return "numeric";
                case ELEMENT_STRING: return "string";
                case ELEMENT_CONSOLIDATED: return "consolidated";
                default: return "numeric";
            }
            
        } catch( Exception e ) {
            return null;
        }
    }

    /**
     * Returns the factor of a consolidation for a given child/parent element pair.
     * @return The weight of the consolidated element.
     * @param servdb Connection and Database name (e.g. localhost/Demo)
     * @param dimensionName The name of the dimension.
     * @param parentName The name of the parent element.
     * @param childName The name of the child element.
     */
    @Override
    public Object PALO_EWEIGHT(String servdb, String dimensionName, 
                               String parentName, String childName) {
        try {
            IDimension dimension = manager.getDimension(servdb, dimensionName);
            if (dimension == null)
                return null;
            
            IElement parent = dimension.getElementByName(parentName,false);
            IElement child = dimension.getElementByName(childName,false);
            if (parent == null || child == null)
                return null;

            return child.getWeight(parent);
                    
        } catch( Exception e ) {
            return null;
        }
    }
    
    /**
     * Returns the maximum level within a dimension.
     * @return The maximum level.
     * @param servdb Connection and Database name (e.g. localhost/Demo)
     * @param dimensionName The name of the dimension.
     */
    @Override
    public Object PALO_ETOPLEVEL(String servdb, String dimensionName) {
        try {
            IDimension dimension = manager.getDimension(servdb, dimensionName);
            if (dimension == null)
                return null;
            
            return new Double(dimension.getDimensionInfo().getMaximumLevel());
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public String[][] PALO_DATABASE_LIST_DIMENSIONS(String servdb) {
        try {
            IDatabase database = connectionHandler.getDatabase(servdb);
            if (database == null)
                return null;
            
            IDimension[] dimensions = database.getDimensions();
            ArrayList<String> validDimensions = new ArrayList<String>();
            for (int i = 0; i < dimensions.length; i++)
                if (dimensions[i].getType().equals(IDimension.DimensionType.DIMENSION_NORMAL))
                    validDimensions.add(dimensions[i].getName());
            String[][] result = new String[validDimensions.size()][1];
            for (int i = 0; i < validDimensions.size(); i++)
                result[i][0] = validDimensions.get(i);
            
            return result;
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public String[][] PALO_CUBE_LIST_DIMENSIONS(String servdb, String cubeName) {
        try {
            IDatabase database = connectionHandler.getDatabase(servdb);
            if (database == null)
                return null;
            
            ICube cube = database.getCubeByName(cubeName);
            if (cube == null)
                return null;
            
            IDimension[] dimensions = cube.getDimensions();
            ArrayList<String> validDimensions = new ArrayList<String>();
            for (int i = 0; i < dimensions.length; i++)
                if (dimensions[i].getType().equals(IDimension.DimensionType.DIMENSION_NORMAL))
                    validDimensions.add(dimensions[i].getName());
            String[][] result = new String[validDimensions.size()][1];
            for (int i = 0; i < validDimensions.size(); i++)
                result[i][0] = validDimensions.get(i);
            
            return result;
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public String[][] PALO_DIMENSION_LIST_CUBES(String servdb, String dimensionName) {
        try {
            IDatabase database = connectionHandler.getDatabase(servdb);
            if (database == null)
                return null;
            
            IDimension dimension = database.getDimensionByName(dimensionName);
            if (dimension == null)
                return null;
            
            ICube[] cubes = database.getCubes();
            ArrayList<String> parentCubes = new ArrayList<String>();
            for (int i = 0; i < cubes.length; i++) {
                if (cubes[i].getType().equals(ICube.CubeType.CUBE_NORMAL)) {
                    IDimension[] cubeDimensions = cubes[i].getDimensions();
                    for (int j = 0; j < cubeDimensions.length; j++) {
                        if (cubeDimensions[j] == dimension) {
                            parentCubes.add(cubes[i].getName());
                            break;
                        }
                    }
                }
            }
            String[][] result = new String[parentCubes.size()][1];
            for (int i = 0; i < parentCubes.size(); i++)
                result[i][0] = parentCubes.get(i);
            
            return result;
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public String[][] PALO_DIMENSION_LIST_ELEMENTS(String servdb, String dimensionName) {
        try {
            IDimension dimension = manager.getDimension(servdb, dimensionName);
            if (dimension == null)
                return null;
            
            IElement[] children = dimension.getElements(false);
            String[][] result = new String[children.length][1];
            for (int i = 0; i < children.length; i++)
                result[i][0] = children[i].getName();
            
            return result;
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public String[][] PALO_ELEMENT_LIST_CONSOLIDATION_ELEMENTS(String servdb, String dimensionName, String elementName) {
        try {
            IDimension dimension = manager.getDimension(servdb, dimensionName);
            if (dimension == null)
                return null;
            
            IElement element = dimension.getElementByName(elementName,false);
            if (element == null)
                return null;
            
            String[][] result = new String[element.getChildCount()][1];
            IElement[] children = element.getChildren();
            for (int i = 0; i < children.length; i++)
                result[i][0] = children[i].getName();
            
            return result;
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public Object PALO_SETDATA(Object value, Object splash, String servdb, String cubeName, Object[] coordinate) {
        if (splash instanceof Double) {
            return ImportResult.doImport(new SetDataResult(xContext, value, (Double)splash, servdb, cubeName, coordinate)).evaluate();
        } else
            return null;
    }

    @Override
    public Object PALO_EADD(String servdb, String dimensionName, String type, String elementName, Object parentName, Object weight, Object clear) {
        double clearParsed = 0;
        if (clear instanceof Double)
            clearParsed = (Double) clear;

        return ImportResult.doImport(new AddElementResult(xContext, servdb, dimensionName, type, elementName, parentName, weight, clearParsed)).evaluate();
    }

    @Override
    public Object PALO_EDELETE(String servdb, String dimensionName, String elementName) {
        return ImportResult.doImport(new DeleteElementResult(xContext, servdb, dimensionName, elementName)).evaluate();
    }
    
    @Override
    public Object PALO_ERENAME(String servdb, String dimensionName, String elementName, String newName) {
        return ImportResult.doImport(new RenameElementResult(xContext, servdb, dimensionName, elementName, newName)).evaluate();
    }

    @Override
    public Object PALO_ERROR_LOG(String errorFielValue, String path, String value, Object cube, Object[] coordinate) {
        String cubeName = (cube instanceof String ? (String) cube : "");
        
        return ImportResult.doImport(new ErrorLogResult(xContext, errorFielValue, path, value, cubeName, coordinate)).evaluate();
    }
    
    @Override
    public Object PALO_CLEARDIMENSION(String servdb, String dimensionName) {
        return ImportResult.doImport(new ClearDimensionResult(xContext, servdb, dimensionName)).evaluate();
    }
}
