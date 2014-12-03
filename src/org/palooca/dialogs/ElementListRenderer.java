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
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import com.jedox.palojlib.interfaces.IAttribute;
import com.jedox.palojlib.interfaces.IElement;

/**
 *
 * @author MichaelRaue
 */
public class ElementListRenderer  extends DefaultListCellRenderer {
    IAttribute   attribute;
    int         attributeFormat = -1;

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

    public ElementListRenderer(IAttribute attribute) {
        super();
        this.attribute = attribute;
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (value instanceof ListElement) {
            ListElement node = (ListElement)value;

            IElement element = node.getNode().getElement();

            switch (element.getType()) {
                case ELEMENT_CONSOLIDATED: setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/consolidated.png"))); break;
                case ELEMENT_NUMERIC: setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/numeric.png"))); break;
                case ELEMENT_STRING: setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/string.png"))); break;
                default: setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/numeric.png")));
            }

            if (attribute == null) {
                setText(element.getName());
            } else {
                //get attributed element
                IElement attributedElement = node.getNode().getDimension().getElementByName(node.getNode().getElement().getName(), true);
                String  attrtext = attributedElement.getAttributeValue(attribute.getName()).toString();
                if (attrtext.length() == 0)
                    attrtext = element.getName();
                String  label = "";
                switch (attributeFormat) {
                    case 1:             // AliasFormatName=Elementname
                        label = element.getName();
                        break;
                    case 2:             // AliasFormatName-Alias=Elementname - Alias
                        label = element.getName() + " - " + attrtext;
                        break;
                    case 3:             // AliasFormatAlias-Name=Alias - Elementname
                        label = attrtext +  " - " + element.getName();
                        break;
                    case 4:             // AliasFormatNameAliasP=Elementname (Alias)
                        label = element.getName() + " (" + attrtext + ")";
                        break;
                    case 5:             // AliasFormatAliasNameP=Alias (Elementname)
                        label = attrtext + " (" + element.getName() + ")";
                        break;
                    case 6:             // AliasFormatNameAlias=Elementname Alias
                        label = element.getName() + " " + attrtext;
                        break;
                    case 7:             // AliasFormatAliasName=Alias Elementname
                        label = attrtext + " " + element.getName();
                        break;
                    case 0:             // AliasFormatAlias=Alias
                    default:
                        label = attrtext;
                        break;
                }
                setText(label);
            }
        }

        return this;
    }
}
