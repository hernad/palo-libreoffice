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
import com.jedox.palojlib.interfaces.*;
import org.palooca.PaloLibUtil;
import org.palooca.subsets.Subset2;

public class SubsetListItem extends Object {

    private IDimension dimension = null;
    private Subset2 subset = null;
    private IAttribute attribute = null;
    public Vector<String[]> selectedElements = new Vector<String[]>();
    public Vector<IElement> selectedElementObjects = new Vector<IElement>();
    private boolean open = false;
    private int currentIndex = 0;

    public SubsetListItem(SubsetListItem other) {
        super();
        dimension = other.dimension;
        subset = other.subset;
        open = other.open;
        attribute = other.attribute;
        selectedElements = (Vector<String[]>)other.selectedElements.clone();
        selectedElementObjects = (Vector<IElement>)other.selectedElementObjects.clone();
    }

    public SubsetListItem(IDimension dimension, boolean initialize) {
        super();
        this.dimension = dimension;
        IElement[] elements = dimension.getElements(false);
        if (initialize && elements.length > 0) {
            String[] filter = new String[2];
            filter[0] = null;
            int index = 0;
            IElement element = null;
            do {
                element = elements[index];
                index++;
            } while (element != null && PaloLibUtil.getElementDepth(element) != 0);
            if (element != null) {
                filter[1] = new String(element.getName());
                selectedElementObjects.add(element);
            }
            selectedElements.add(filter);
        }
    }

    public Vector<String[]> getSelectedElements() {
        return selectedElements;
    }

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

        IElement[] elements = subset.getRootNodes();
        IElement parent = null;
        int rootlevel = 0;
        int level = 0;

        IElement[]   subsetElements = subset.getElements();
        ArrayList<String> path = new ArrayList<String>();

        for (int i = 0; i < subsetElements.length; i++) {
            path.clear();
            addParentToStringPath(subsetElements[i], path);

            String[] stringPath = new String[path.size() + 2];
            stringPath[0] = null;
            for (int j = 0; j < path.size(); j++) {
                stringPath[path.size() - j] = path.get(j);
            }
            stringPath[path.size() + 1] = subsetElements[i].getName();

            getSelectedElements().add(stringPath);
            getSelectedElementObjects().add(subsetElements[i]);
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
}


