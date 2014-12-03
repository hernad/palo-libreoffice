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

package org.palooca.dialogs.nodes;

import javax.swing.tree.DefaultMutableTreeNode;
import com.jedox.palojlib.interfaces.IAttribute;
import com.jedox.palojlib.interfaces.IDimension;
import com.jedox.palojlib.interfaces.IElement;
import javax.swing.tree.TreeNode;

/**
 *
 * @author MichaelRaue
 */
public class ElementTreeNode extends DefaultMutableTreeNode {

    protected IElement element;
    protected IDimension dimension;
    protected boolean showWeight = false;
    protected IAttribute attribute = null;
    protected boolean editConsolidationMode = false;
    private Double weight;

    public double getWeight() {
        if (weight == null) { //delegate to IElement
            TreeNode parentNode = getParent();
            if (parent instanceof ElementTreeNode) {
                ElementTreeNode parentElementNode = (ElementTreeNode)parent;
                try {
                    weight = element.getWeight(parentElementNode.getElement());
                } catch (Exception e) {
                    weight = 1.0;
                }
            }
            return weight;
        }
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public boolean isShowWeight() {
        return showWeight;
    }

    public void setShowWeight(boolean showWeight) {
        this.showWeight = showWeight;
    }

    public boolean isEditConsolidationMode() {
        return editConsolidationMode;
    }

    public void setEditConsolidationMode(boolean editConsolidationMode) {
        this.editConsolidationMode = editConsolidationMode;
    }

    public void setElement(IElement element) {
        this.element = element;
    }

    public IElement getElement() {
        return element;
    }

    public ElementTreeNode(IElement element, IDimension dimension) {
        super();
        this.element = element;
        this.dimension = dimension;
    }

    public IDimension getDimension() {
        return dimension;
    }

    @Override
    public String toString() {
        if (element == null)
            return "No element assigned";
        else {
            if (isShowWeight() && getWeight() != 1.0) {
                return element.getName() + "    (" + getWeight() + ")";
            } else {
                return element.getName();
            }
        }
    }
}
