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

import org.palooca.dialogs.nodes.RoleTreeNode;
import org.palooca.dialogs.nodes.RolesTreeNode;
import org.palooca.dialogs.nodes.RuleTreeNode;
import org.palooca.dialogs.nodes.GroupTreeNode;
import org.palooca.dialogs.nodes.UsersTreeNode;
import org.palooca.dialogs.nodes.SubsetTreeNode;
import org.palooca.dialogs.nodes.UserTreeNode;
import org.palooca.dialogs.nodes.GroupsTreeNode;
import org.palooca.dialogs.nodes.DimensionTreeNode;
import org.palooca.dialogs.nodes.CubeTreeNode;
import org.palooca.dialogs.nodes.DatabaseTreeNode;
import org.palooca.dialogs.nodes.ConnectionTreeNode;
import org.palooca.dialogs.nodes.ElementTreeNode;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import com.jedox.palojlib.interfaces.IElement;
import org.palooca.dialogs.nodes.AttributeTreeNode;
import org.palooca.dialogs.nodes.AttributesTreeNode;
import org.palooca.dialogs.nodes.CubeRightsTreeNode;
import org.palooca.dialogs.nodes.CubeRulesTreeNode;
import org.palooca.dialogs.nodes.CubesTreeNode;
import org.palooca.dialogs.nodes.DimensionRightsTreeNode;
import org.palooca.dialogs.nodes.DimensionsTreeNode;
import org.palooca.network.ConnectionState;

/**
 *
 * @author MichaelRaue
 */
public class ModellerOverviewTreeRenderer  extends DefaultTreeCellRenderer {
    boolean isTargetNode;
    boolean isTargetNodeLeaf;
    boolean isLastItem;

    public ModellerOverviewTreeRenderer() {
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


        if (value instanceof ConnectionTreeNode) {
            ConnectionTreeNode  conNode = (ConnectionTreeNode)value;
            if (conNode.getConnection().getState() == ConnectionState.Connected)
                setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/server.png")));
            else
                setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/serverDis.png")));
        } else if (value instanceof DatabaseTreeNode) {
            setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/database.png")));
        } else if (value instanceof DimensionsTreeNode) {
            setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/dimensions.png")));
        } else if (value instanceof DimensionTreeNode) {
            setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/dimension.png")));
        } else if (value instanceof SubsetTreeNode) {
            setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/subset.png")));
        } else if (value instanceof CubeTreeNode) {
            setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/cube.png")));
        } else if (value instanceof CubesTreeNode) {
            setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/cubes.png")));
        } else if (value instanceof CubeRightsTreeNode) {
            setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/cubeRights.png")));
        } else if (value instanceof CubeRulesTreeNode) {
            setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/cubeRules.png")));
        } else if (value instanceof RuleTreeNode) {
            setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/rule.png")));
        } else if (value instanceof UserTreeNode) {
            setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/user.png")));
        } else if (value instanceof RoleTreeNode) {
            setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/role.png")));
        } else if (value instanceof GroupTreeNode) {
            setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/group.png")));
        } else if (value instanceof DimensionRightsTreeNode) {
            setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/dimensionrights.png")));
        } else if (value instanceof AttributeTreeNode) {
            AttributeTreeNode node = (AttributeTreeNode)value;
            if (node.getAttribute().getType() == IElement.ElementType.ELEMENT_NUMERIC) {
                setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/numeric.png")));
            } else if (node.getAttribute().getType() == IElement.ElementType.ELEMENT_STRING) {
                setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/string.png")));
            } else {
                setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/consolidated.png")));
            }
        } else if (value instanceof AttributesTreeNode) {
            setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/attributes.png")));
        } else if (value instanceof ElementTreeNode) {
            IElement element = ((ElementTreeNode)value).getElement();

            if (tree instanceof DnDJTree) {
                DnDJTree  dtree = (DnDJTree)tree;

                isTargetNode = (value == dtree.getDropTargetNode());
                isTargetNodeLeaf = true;//(isTargetNode && ((TreeNode)value).isLeaf());
                //isLastItem = (index == list.getModel().getSize() - 1);
//                boolean showSelected = sel & (dtree.getDropTargetNode() == null);
            }

            if (((ElementTreeNode)value).isEditConsolidationMode()) {
                setFont(getFont().deriveFont(Font.BOLD));
            } else {
                setFont(getFont().deriveFont(Font.PLAIN));
            }

            switch (element.getType()) {
                case ELEMENT_CONSOLIDATED: setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/consolidated.png"))); break;
                case ELEMENT_NUMERIC: setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/numeric.png"))); break;
                case ELEMENT_STRING:  setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/string.png"))); break;
                default: setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/numeric.png")));
            }

        } else if (value instanceof UsersTreeNode) {
            setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/users.png")));
        } else if (value instanceof GroupsTreeNode) {
            setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/groups.png")));
        } else if (value instanceof RolesTreeNode) {
            setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/roles.png")));
        }

        return this;
    }

    @Override
    public void paintComponent(Graphics g)
    {
      super.paintComponent(g);
      if(isTargetNode)
      {
        g.setColor(Color.black);
        if(isTargetNodeLeaf){
          g.drawLine(0,0,getSize().width,0);
        }
        else
          g.drawRect(0,0, getSize().width - 1, getSize().height - 1);

      }
    }
}
