/*
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
 */

         package org.palooca.dialogs;

import java.util.ArrayList;
import java.util.Vector;
import com.jedox.palojlib.interfaces.IAttribute;
import com.jedox.palojlib.interfaces.IDatabase;
import com.jedox.palojlib.interfaces.IDimension;
import com.jedox.palojlib.interfaces.IElement;
import java.util.List;
import java.util.Set;
import org.palooca.PaloLibUtil;
import org.palooca.dialogs.nodes.ElementTreeNode;
import org.palooca.network.ConnectionInfo;
import org.palooca.subsets.Subset2;

public class DimensionSubsetListItem extends Object {

    private IDimension dimension = null;
    private Subset2 subset = null;
    private IAttribute attribute = null;
    public Vector<String[]> selectedElements = new Vector<String[]>();
    public Vector<IElement> selectedElementObjects = new Vector<IElement>();
    private List<String[]> elementsBackup = new ArrayList<String[]>();
    private List<IElement> elementObjectsBackup = new ArrayList<IElement>();
    private boolean open = false;
    private int currentIndex = 0;
    private IDatabase database;
    private Subset2[] availableSubsets;
    private ConnectionInfo connectionInfo;

    public DimensionSubsetListItem(DimensionSubsetListItem other) {
        super();
        dimension = other.dimension;
        subset = other.subset;
        open = other.open;
        attribute = other.attribute;
        database = other.database;
        selectedElements = (Vector<String[]>)other.selectedElements.clone();
        selectedElementObjects = (Vector<IElement>)other.selectedElementObjects.clone();
        connectionInfo = other.connectionInfo;
    }

    public DimensionSubsetListItem(ConnectionInfo connectionInfo, IDatabase database, IDimension dimension, boolean initialize, boolean filterDimension) {
        super();
        this.database = database;
        this.dimension = dimension;
        this.connectionInfo = connectionInfo;
        IElement[] elements = dimension.getElements(false);
        if (initialize && elements.length > 0) {
//            if (filterDimension) {
//                String[] filter = new String[2];
//                filter[0] = null;
//                int index = 0;
//                Element element = null;
//                do {
//                    element = dimension.getElementAt(index);
//                    index++;
//                } while (element != null && element.getDepth() != 0);
//                if (element != null) {
//                    filter[1] = new String(element.getName());
//                    selectedElementObjects.add(element);
//                }
//                selectedElements.add(filter);
//            } else {

                for (int i = 0; i < elements.length; i++) {
                    if (PaloLibUtil.getElementDepth(elements[i]) == 0) {
                        String[] stringArray = PaloDialogUtilities.stringPathToStringArray(elements[i].getName());
                        getSelectedElements().add(stringArray);
                        getSelectedElementObjects().add(elements[i]);
                    }
                }
//            }
        }
    }

    public IDatabase getDatabase() {
        return database;
    }

    public void setDatabase(IDatabase database) {
        this.database = database;
    }

    public void setConnectionInfo(ConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    public void updateElements(Set<String> elements, boolean withBackup, boolean refresh) {
        elementsBackup.clear();
        elementObjectsBackup.clear();
        if (withBackup) {
            elementsBackup.addAll(selectedElements);
            elementObjectsBackup.addAll(selectedElementObjects);
        }
        if (elements != null) {
            List<Integer> indices = new ArrayList<Integer>();
            for (int i=0; i<selectedElementObjects.size(); i++) {
                IElement element = selectedElementObjects.elementAt(i);
                if (!elements.contains(element.getName())) {
                    indices.add(i);
                }
            }
            IElement lastElement = null;
            String[] lastPath = null;
            for (int i=indices.size()-1;i>=0;i--) {
                int idx = indices.get(i).intValue();
                lastElement = selectedElementObjects.remove(idx);
                lastPath = selectedElements.remove(idx);
            }
            if (selectedElementObjects.isEmpty() && lastPath != null) { //ensure we do not empty dimension completly. Keep topmost element in this case
                selectedElementObjects.add(lastElement);
                selectedElements.add(lastPath);
            }
            if (refresh) {
                List<IElement> toRefresh = new ArrayList<IElement>();
                toRefresh.addAll(selectedElementObjects);
                selectedElementObjects.clear();
                for (IElement e : toRefresh) {
                    selectedElementObjects.add(dimension.getElementByName(e.getName(), true));
                }
            }
        }
    }

    public void restoreElements() {
        selectedElements.clear();
        selectedElementObjects.clear();
        selectedElements.addAll(elementsBackup);
        selectedElementObjects.addAll(elementObjectsBackup);
    }

    public Vector<String[]> getSelectedElements() {
        return selectedElements;
    }

    /*
    public Vector<String[]> getSelectedElementsCleaned() {
        Vector<String[]> input = getSelectedElements();
        Vector<String[]> result = new Vector<String[]>();
        for (String[] sa : input) {
            boolean needsCleaning = false;
            for (String s : sa) {
                if (s == null) {
                    needsCleaning = true;
                    break;
                }
            }
            if (needsCleaning) {
                List<String> temp = new ArrayList<String>();
                for (String s : sa) {
                    if (s != null) temp.add(s);
                }
                result.add(temp.toArray(new String[0]));
            } else {
                result.add(sa);
            }
        }
        return result;
    }
     * */

    public String getSelectedElementStringPath(int index) {
        String[] path = selectedElements.get(index);
        if (path != null && path.length > 0) {
            String  result = new String("");

            for (int i = 1; i < path.length; i++) {
                result += path[i];
                if (i < path.length - 1)
                    result += "\\";
            }

            return result;
        }

        return new String("");
    }

    public Vector<IElement> getSelectedElementObjects() {
        return selectedElementObjects;
    }

    public IElement getSelectedElementObject(int index) {
        return selectedElementObjects.get(index);
    }

    public String[] getSelectedFilter() {
        if (selectedElements.size() > 0) {
            return selectedElements.get(0);
        } else {
            return null;
        }
    }

    public void setSelectedFilterPath(String[] path) {
        selectedElements.clear();
        selectedElements.add(path);
    }

    public void setSelectedFilterElementObject(IElement element) {
        selectedElementObjects.clear();
        selectedElementObjects.add(element);
    }

    public String getSelectedFilterName() {
        if (selectedElements.size() > 0) {
            String[] filter = selectedElements.get(0);
            if (filter != null && filter.length > 0) {
                return filter[filter.length - 1];
            }
        }

        return new String("");
    }

    public String getSelectedFilterPath() {
        if (selectedElements.size() > 0) {
            String[] filter = selectedElements.get(0);
            if (filter != null && filter.length > 0) {
                String  result = new String("");

                for (int i = 1; i < filter.length; i++) {
                    result += filter[i];
                    if (i < filter.length - 1)
                        result += "\\";
                }

                return result;
            }
        }

        return new String("");
    }

    public IAttribute getAttribute() {
        return attribute;
    }

    public void setAttribute(IAttribute attribute) {
        this.attribute = attribute;
    }

    public IDimension getDimension() {
        return dimension;
    }

    public void setDimension(IDimension dimension) {
        this.dimension = dimension;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public Subset2 getSubset() {
        return subset;
    }

    public void setSubset(Subset2 subset) {
        this.subset = subset;

        if (subset == null)
            return;

        getSelectedElements().clear();
        getSelectedElementObjects().clear();


        IElement roots[] = subset.getRootNodes();
        ArrayList<String> path = new ArrayList<String>();

        for (int i = 0; i < roots.length; i++) {
            ElementTreeNode elementNode = new ElementTreeNode(roots[i],subset.getDimension());

            path.clear();
            IElement element = elementNode.getElement();
            addParentToStringPath(element, path);

            String[] stringPath = new String[path.size() + 2];
            stringPath[0] = null;
            for (int j = 0; j < path.size(); j++) {
                stringPath[path.size() - j] = path.get(j);
            }
            stringPath[path.size() + 1] = element.getName();

            getSelectedElements().add(stringPath);
            getSelectedElementObjects().add(element);

            addElementChildren(roots[i]);
        }

    }

    private void addElementChildren(IElement node) {
        ArrayList<String> path = new ArrayList<String>();

        IElement[] children = node.getChildren();
        if (children == null)
            return;

        for (int i = 0; i < children.length; i++) {
            path.clear();

            IElement element = children[i];

            addParentToStringPath(element, path);

            String[] stringPath = new String[path.size() + 2];
            stringPath[0] = null;
            for (int j = 0; j < path.size(); j++) {
                stringPath[path.size() - j] = path.get(j);
            }
            stringPath[path.size() + 1] = element.getName();

            getSelectedElements().add(stringPath);
            getSelectedElementObjects().add(element);
            
            addElementChildren(children[i]);
        }
    }


    public void addParentToStringPath(IElement element, ArrayList<String> path)
    {
        if (element == null)
            return;

        IElement[] parents = element.getParents();

        if (parents == null || parents.length == 0)
            return;

        IElement parent = parents[0];

        path.add(parent.getName());
        addParentToStringPath(parent, path);
    }

    public Subset2[] getAvailableSubsets() {
        /*
        if (availableSubsets == null) {
            availableSubsets = SubsetHandler.loadSubsets(database, dimension, connectionInfo.getUsername());
            return availableSubsets;
        }
         * 
         */
        return new Subset2[0];
    }
}


