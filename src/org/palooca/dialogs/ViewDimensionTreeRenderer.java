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

import org.palooca.dialogs.nodes.SubsetTreeNode;
import org.palooca.dialogs.nodes.DimensionTreeNode;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 *
 * @author MichaelRaue
 */
public class ViewDimensionTreeRenderer  extends DefaultTreeCellRenderer {
    boolean isTargetNode;
    boolean isTargetNodeLeaf;
    boolean isLastItem;

    public ViewDimensionTreeRenderer() {
    }

    @Override
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

        if (value instanceof DimensionTreeNode) {
            setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/dimension.png")));
        } else if (value instanceof SubsetTreeNode) {
            setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/subset.png")));
        } else {
                setFont(getFont().deriveFont(Font.BOLD));
        }

        return this;
    }

    @Override
    public void paintComponent(Graphics g)
    {
     // super.paintComponent(g);
          g.drawRect(0,0, getSize().width - 1, getSize().height - 1);

    }
}
