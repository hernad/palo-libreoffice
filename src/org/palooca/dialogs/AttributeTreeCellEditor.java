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

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import com.jedox.palojlib.interfaces.IElement;
import org.palooca.dialogs.nodes.AttributeTreeNode;

/**
 *
 * @author MichaelRaue
 */
public class AttributeTreeCellEditor extends DefaultTreeCellEditor {
    public AttributeTreeCellEditor(JTree tree, DefaultTreeCellRenderer renderer) {
        super(tree, renderer);
    }

    @Override
    protected void determineOffset(JTree tree, Object value, boolean isSelected, boolean expanded,
                                    boolean leaf, int row) {

        AttributeTreeNode node = (AttributeTreeNode)value;
        if (node.getAttribute().getType() == IElement.ElementType.ELEMENT_NUMERIC) {
            editingIcon = new javax.swing.ImageIcon(getClass().getResource("/images/numeric.png"));
        } else {
            editingIcon = new javax.swing.ImageIcon(getClass().getResource("/images/string.png"));
        }
        offset = renderer.getIconTextGap() + editingIcon.getIconWidth();
    }
}
