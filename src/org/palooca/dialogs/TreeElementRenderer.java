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

import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import com.jedox.palojlib.interfaces.IAttribute;
import com.jedox.palojlib.interfaces.IElement;
import org.palooca.dialogs.nodes.ElementTreeNode;

/**
 *
 * @author MichaelRaue
 */
public class TreeElementRenderer  extends DefaultTreeCellRenderer {
    IAttribute   attribute = null;
    int         attributeFormat = -1;
    boolean     showIcon = false;

    public boolean isShowIcon() {
        return showIcon;
    }

    public void setShowIcon(boolean showIcon) {
        this.showIcon = showIcon;
    }

    public int getAttributeFormat() {
        return attributeFormat;
    }

    public void setAttributeFormat(int attributeFormat) {
        this.attributeFormat = attributeFormat;
    }

    public IAttribute getAttribute() {
        return attribute;
    }

    public void setAttribute(IAttribute attribute) {
        this.attribute = attribute;
    }

    public TreeElementRenderer(IAttribute attribute) {
        this.attribute = attribute;
    }

    public Component getTreeCellRendererComponent(
                        JTree tree,
                        Object value,
                        boolean sel,
                        boolean expanded,
                        boolean leaf,
                        int row,
                        boolean hasFocus) {

        super.getTreeCellRendererComponent(
                        tree, value, sel,
                        expanded, leaf, row,
                        hasFocus);

        setIcon(null);
        if (value instanceof ElementTreeNode) {
            ElementTreeNode node = (ElementTreeNode)value;
            if (attribute == null) {
                setText(node.getElement().getName());
            } else {
                IElement attributedElement = node.getDimension().getElementByName(node.getElement().getName(), true);
                String  attrtext = attributedElement.getAttributeValue(attribute.getName()).toString();
                if (attrtext.length() == 0)
                    attrtext = node.getElement().getName();
                String  label = "";
                switch (attributeFormat) {
                    case 1:             // AliasFormatName=Elementname
                        label = node.getElement().getName();
                        break;
                    case 2:             // AliasFormatName-Alias=Elementname - Alias
                        label = node.getElement().getName() + " - " + attrtext;
                        break;
                    case 3:             // AliasFormatAlias-Name=Alias - Elementname
                        label = attrtext +  " - " + node.getElement().getName();
                        break;
                    case 4:             // AliasFormatNameAliasP=Elementname (Alias)
                        label = node.getElement().getName() + " (" + attrtext + ")";
                        break;
                    case 5:             // AliasFormatAliasNameP=Alias (Elementname)
                        label = attrtext + " (" + node.getElement().getName() + ")";
                        break;
                    case 6:             // AliasFormatNameAlias=Elementname Alias
                        label = node.getElement().getName() + " " + attrtext;
                        break;
                    case 7:             // AliasFormatAliasName=Alias Elementname
                        label = attrtext + " " + node.getElement().getName();
                        break;
                    case 0:             // AliasFormatAlias=Alias
                    default:
                        label = attrtext;
                        break;
                }
                setText(label);
            }
        }

        if (isShowIcon() && value instanceof ElementTreeNode) {
            ElementTreeNode node = (ElementTreeNode)value;

            IElement element = node.getElement();

            switch (element.getType()) {
                case ELEMENT_CONSOLIDATED: setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/consolidated.png"))); break;
                case ELEMENT_NUMERIC: setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/numeric.png"))); break;
                case ELEMENT_STRING:  setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/string.png"))); break;
                default: setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/numeric.png")));
            }
            
        }

        return this;
    }
}
