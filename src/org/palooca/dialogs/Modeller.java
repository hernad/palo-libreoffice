/*
 * Modeller.java
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

 */

package org.palooca.dialogs;

import org.palooca.dialogs.nodes.RoleTreeNode;
import org.palooca.dialogs.nodes.RolesTreeNode;
import org.palooca.dialogs.nodes.GroupTreeNode;
import org.palooca.dialogs.nodes.UsersTreeNode;
import org.palooca.dialogs.nodes.UserTreeNode;
import org.palooca.dialogs.nodes.GroupsTreeNode;
import org.palooca.dialogs.nodes.DimensionTreeNode;
import org.palooca.dialogs.nodes.CubeTreeNode;
import org.palooca.dialogs.nodes.DatabaseTreeNode;
import org.palooca.dialogs.nodes.ConnectionTreeNode;
import org.palooca.dialogs.nodes.ElementTreeNode;
import com.sun.star.uno.XComponentContext;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.event.KeyEvent;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.ResourceBundle;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import com.jedox.palojlib.interfaces.IAttribute;
import com.jedox.palojlib.interfaces.IConnection;
import com.jedox.palojlib.interfaces.IConsolidation;
import com.jedox.palojlib.interfaces.ICube;
import com.jedox.palojlib.interfaces.IDatabase;
import com.jedox.palojlib.interfaces.IDimension;
import com.jedox.palojlib.interfaces.IElement;
import com.jedox.palojlib.exceptions.PaloJException;
import com.jedox.palojlib.exceptions.PaloException;
import com.jedox.palojlib.interfaces.IRule;
import org.palooca.PalOOCaManager;
import org.palooca.PalOOCaView;
import org.palooca.dialogs.nodes.AttributeTreeNode;
import org.palooca.dialogs.nodes.AttributesTreeNode;
import org.palooca.dialogs.nodes.CubeRightsTreeNode;
import org.palooca.dialogs.nodes.CubeRulesTreeNode;
import org.palooca.dialogs.nodes.CubesTreeNode;
import org.palooca.dialogs.nodes.DimensionRightsTreeNode;
import org.palooca.dialogs.nodes.DimensionsTreeNode;
import org.palooca.dialogs.nodes.ModellerFolderTreeNode;
import org.palooca.network.ConnectionHandler;
import org.palooca.network.ConnectionInfo;
import org.palooca.network.ConnectionState;
import org.palooca.paloutil.PaloAdministration;
import org.palooca.paloutil.PaloGroup;
import org.palooca.paloutil.PaloRole;
import org.palooca.paloutil.PaloUser;

/**
 *
 * @author MichaelRaue
 */
public class Modeller extends javax.swing.JDialog {

    static private String  overviewSelection[] = null;

    private XComponentContext context;
    private PalOOCaManager manager;
    private ConnectionHandler connectionHandler;
    private DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Root");
    private DefaultTreeModel overviewTreeModel = new DefaultTreeModel(rootNode);

    private ConnectionTreeNode rootNodeDatabases = new ConnectionTreeNode(null);
    private DefaultTreeModel databaseTreeModel = new DefaultTreeModel(rootNodeDatabases);

    private DatabaseTreeNode rootNodeCubes = new DatabaseTreeNode(null,null);
    private DefaultTreeModel cubeTreeModel = new DefaultTreeModel(rootNodeCubes);

    private DatabaseTreeNode rootNodeDimensions = new DatabaseTreeNode(null,null);
    private DefaultTreeModel dimensionTreeModel = new DefaultTreeModel(rootNodeDimensions);

    private DimensionTreeNode rootNodeElements = new DimensionTreeNode(null,null);
    private DefaultTreeModel elementTreeModel = new DefaultTreeModel(rootNodeElements);

    private DimensionTreeNode rootNodeConsolidatedElements = new DimensionTreeNode(null,null);
    private DefaultTreeModel consolidatedElementTreeModel = new DefaultTreeModel(rootNodeConsolidatedElements);

    private DefaultMutableTreeNode rootNodeElementHierarchy = new DefaultMutableTreeNode();
    private DefaultTreeModel elementHierarchyTreeModel = new DefaultTreeModel(rootNodeElementHierarchy);

    private DatabaseTreeNode rootNodeGroups = new DatabaseTreeNode(null,null);
    private DefaultTreeModel groupTreeModel = new DefaultTreeModel(rootNodeGroups);

    private DatabaseTreeNode rootNodeUsers = new DatabaseTreeNode(null,null);
    private DefaultTreeModel userTreeModel = new DefaultTreeModel(rootNodeUsers);

    private DatabaseTreeNode rootNodeRoles = new DatabaseTreeNode(null,null);
    private DefaultTreeModel roleTreeModel = new DefaultTreeModel(rootNodeRoles);

    private DimensionTreeNode rootNodeAttributes = new DimensionTreeNode(null,null);
    private DefaultTreeModel attributesTreeModel = new DefaultTreeModel(rootNodeAttributes);

    private ResourceBundle resourceBundle;
    private DimensionTreeNode editDimNode = null;
    private CubeTreeNode editCubeNode = null;
    private DatabaseTreeNode editDatabaseNode = null;
    private ElementTreeNode editElementNode = null;
    private GroupTreeNode editGroupNode = null;
    private UserTreeNode editUserNode = null;
    private RoleTreeNode editRoleNode = null;
    private AttributeTreeNode editAttributeNode = null;
    private ElementTreeNode editElementConsolidationNode = null;
    private DimensionTreeCellEditor cellEditorDims;
    private DatabaseTreeCellEditor cellEditorDatabases;
    private CubeTreeCellEditor cellEditorCubes;
    private ElementTreeCellEditor cellEditorElements;
    private GroupTreeCellEditor cellEditorGroups;
    private UserTreeCellEditor cellEditorUsers;
    private RoleTreeCellEditor cellEditorRoles;
    private AttributeTreeCellEditor cellEditorAttributes;
    private ElementTreeCellEditor cellEditorConsolidatedElements;

    private GroupRoleTableListener groupRoleTableListener;
    private UserGroupTableListener userGroupTableListener;
    private RoleRightTableListener roleRightTableListener;
    private CubeRightTableListener cubeRightTableListener;

    private boolean editConsolidationMode = false;
    private TreePath oldSelectionPath = null;
    private JComboBox cmbRights = new JComboBox(new javax.swing.DefaultComboBoxModel(
                    new String[] { "S", "D", "W", "R", "N", ""}));
    private TableCellEditor editorRights = new DefaultCellEditor(cmbRights);


    /** Creates new form Modeller */
    public Modeller(java.awt.Dialog parent, boolean modal, XComponentContext context) {
        super(parent, modal);

//        this.setIconImage(new ImageIcon(Modeller.class.getResource("/images/modeler.png")).getImage());

        this.context = context;
        this.manager = PalOOCaManager.getInstance(context);
        this.connectionHandler = manager.getConnectionHandler();
        this.resourceBundle = PalOOCaManager.getInstance(context).getResourceBundle("org/palooca/dialogs/PalOOCaDialogs");

        initComponents();

        treeOverview.setCellRenderer(new ModellerOverviewTreeRenderer());

        treeDatabases.setCellRenderer(new ModellerOverviewTreeRenderer());
        treeDatabases.setEditable(true);
        treeDatabases.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        cellEditorDatabases = new DatabaseTreeCellEditor(treeDatabases, new ModellerOverviewTreeRenderer());
        treeDatabases.setCellEditor(cellEditorDatabases);
        cellEditorDatabases.addCellEditorListener(new DatabaseCellEditorListener(this));

        treeDimensions.setCellRenderer(new ModellerOverviewTreeRenderer());
        treeDimensions.setEditable(true);
        treeDimensions.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        cellEditorDims = new DimensionTreeCellEditor(treeDimensions, new ModellerOverviewTreeRenderer());
        treeDimensions.setCellEditor(cellEditorDims);
        cellEditorDims.addCellEditorListener(new DimensionCellEditorListener(this));

        treeCubes.setCellRenderer(new ModellerOverviewTreeRenderer());
        treeCubes.setEditable(true);
        treeCubes.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        cellEditorCubes = new CubeTreeCellEditor(treeCubes, new ModellerOverviewTreeRenderer());
        treeCubes.setCellEditor(cellEditorCubes);
        cellEditorCubes.addCellEditorListener(new CubeCellEditorListener(this));

        treeElements.setCellRenderer(new ModellerOverviewTreeRenderer());
        treeElements.setEditable(true);
        cellEditorElements = new ElementTreeCellEditor(treeElements, new ModellerOverviewTreeRenderer());
        treeElements.setCellEditor(cellEditorElements);
        cellEditorElements.addCellEditorListener(new ElementCellEditorListener(this));

        treeConsolidation.setCellRenderer(new ModellerOverviewTreeRenderer());
//        treeConsolidation.setEditable(true);
        cellEditorConsolidatedElements = new ElementTreeCellEditor(treeConsolidation, new ModellerOverviewTreeRenderer());
        treeConsolidation.setCellEditor(cellEditorConsolidatedElements);

        treeGroups.setCellRenderer(new ModellerOverviewTreeRenderer());
        treeGroups.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        treeGroups.setEditable(true);
        cellEditorGroups = new GroupTreeCellEditor(treeGroups, new ModellerOverviewTreeRenderer());
        treeGroups.setCellEditor(cellEditorGroups);
        cellEditorGroups.addCellEditorListener(new GroupCellEditorListener(this));

        treeUsers.setCellRenderer(new ModellerOverviewTreeRenderer());
        treeUsers.setEditable(true);
        treeUsers.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        cellEditorUsers = new UserTreeCellEditor(treeUsers, new ModellerOverviewTreeRenderer());
        treeUsers.setCellEditor(cellEditorUsers);
        cellEditorUsers.addCellEditorListener(new UserCellEditorListener(this));

        treeRoles.setCellRenderer(new ModellerOverviewTreeRenderer());
        treeRoles.setEditable(true);
        treeRoles.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        cellEditorRoles = new RoleTreeCellEditor(treeRoles, new ModellerOverviewTreeRenderer());
        treeRoles.setCellEditor(cellEditorRoles);
        cellEditorRoles.addCellEditorListener(new RoleCellEditorListener(this));

        treeAttributes.setCellRenderer(new ModellerOverviewTreeRenderer());
        treeAttributes.setEditable(true);
        treeAttributes.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        cellEditorAttributes = new AttributeTreeCellEditor(treeAttributes, new ModellerOverviewTreeRenderer());
        treeAttributes.setCellEditor(cellEditorAttributes);
        cellEditorAttributes.addCellEditorListener(new AttributeCellEditorListener(this));

        treeHierarchy.setCellRenderer(new ModellerOverviewTreeRenderer());

        buttonGroup2.add(btnSortDown);
        buttonGroup2.add(btnSortUp);
        buttonGroup2.add(btnNoSort);
        btnNoSort.setSelected(true);

        buttonGroupElementType.add(btnElementNumeric);
        buttonGroupElementType.add(btnElementString);

        buttonGroupAttributeType.add(btnAttributeNumeric);
        buttonGroupAttributeType.add(btnAttributeString);

        groupRoleTableListener = new GroupRoleTableListener();
        tableRoles.getModel().addTableModelListener(groupRoleTableListener);

        userGroupTableListener = new UserGroupTableListener();
        tableGroups.getModel().addTableModelListener(userGroupTableListener);

        roleRightTableListener = new RoleRightTableListener();
        tableRights.getModel().addTableModelListener(roleRightTableListener);

        cubeRightTableListener = new CubeRightTableListener();
        tableCubeRights.getModel().addTableModelListener(cubeRightTableListener);

        tableRules.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting())
                    return; // if you don't want to handle intermediate selections

                int selRow = tableRules.getSelectedRow();
                if (selRow == -1)
                    return;

                DefaultMutableTreeNode node = getSelected(treeOverview);
                if (node instanceof CubeRulesTreeNode) {
                    CubeRulesTreeNode cubeNode = (CubeRulesTreeNode)node;
                    IRule rule = cubeNode.getRules().get(selRow);
                    txtRule.setText(rule.getDefinition());
                    txtRule.setCaretPosition(0);
                    txtRuleComment.setText(rule.getComment());
                    txtRuleComment.setCaretPosition(0);
                }
                updateRulesPanelButtonStates();
            }
        });

        buildOverview(true);

        if (treeOverview.getSelectionCount() == 0)
            treeOverview.setSelectionRow(0);

        jSplitPane1.setDividerLocation(0.3);

        setLocationRelativeTo(null);

        updateDimensionPanelButtonStates();
        updateConnectionButtons();
    }

    @Override
    public void show() {
        super.show();
    }

    private class DatabaseCellEditorListener implements CellEditorListener
    {
        Dialog dialog;

        public DatabaseCellEditorListener(Dialog dialog) {
            this.dialog = dialog;
        }

        public void editingCanceled(ChangeEvent event) {
            if (editDatabaseNode != null && editDatabaseNode.getDatabase() == null) {
                rootNodeDatabases.remove(editDatabaseNode);
                databaseTreeModel.nodeStructureChanged(rootNodeDatabases);
            }
            //buildOverview(false);
            //updateServerPanelButtonStates();
        }

        public void editingStopped(ChangeEvent e) {
            if (editDatabaseNode != null && editDatabaseNode.getDatabase() == null) {
                try {
                    String name = (String)cellEditorDatabases.getCellEditorValue();
                    IDatabase db = editDatabaseNode.getConnection().addDatabase(name);
                    editDatabaseNode.setDatabase(db);
                    /* cschw: renaming not cleanly supported by backend. we have to create it here with final name
                    if (editDatabaseNode.getConnection().getDatabaseByName(name) == null && name.length() > 0) {
                        editDatabaseNode.getDatabase().rename(name);
                        editDatabaseNode.getConnection().getDatabases();
                    } else {
                        JOptionPane.showMessageDialog(dialog, java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Invalid_Database_Name."), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
                    }
                     *
                     */
                } catch (PaloException pe) {
                    JOptionPane.showMessageDialog(dialog, pe.getMessage(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
                } catch (PaloJException pe) {
                    JOptionPane.showMessageDialog(dialog, pe.getMessage(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
                }
                buildOverview(false);
                updateServerPanelButtonStates();
            }
        }
    }

    private class DimensionCellEditorListener implements CellEditorListener
    {
        Dialog dialog;

        public DimensionCellEditorListener(Dialog dialog) {
            this.dialog = dialog;
        }

        public void editingCanceled(ChangeEvent event) {
        }

        public void editingStopped(ChangeEvent e) {
            if (editDimNode != null) {
                String name = (String)cellEditorDims.getCellEditorValue();
                try {
                    if (editDimNode.getDimension().getName().equalsIgnoreCase(name) == false) {
                        if (editDimNode.getDatabase().getDimensionByName(name) == null && name.length() > 0) {
                            editDimNode.getDimension().rename(name);
                        } else {
                            JOptionPane.showMessageDialog(dialog, java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Invalid_Dimension_Name."), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (PaloJException pe) {
                    JOptionPane.showMessageDialog(dialog, pe.getMessage(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
                } catch (PaloException pe) {
                    JOptionPane.showMessageDialog(dialog, pe.getDescription() + ", " + pe.getReason(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
                }
                buildOverview(false);
                updateDatabasePanelButtonStates();
            }
        }
    }

    private class CubeCellEditorListener implements CellEditorListener
    {
        Dialog dialog;

        public CubeCellEditorListener(Dialog dialog) {
            this.dialog = dialog;
        }

        public void editingCanceled(ChangeEvent event) {
        }

        public void editingStopped(ChangeEvent e) {
            if (editCubeNode != null) {
                String name = (String)cellEditorCubes.getCellEditorValue();
                try {
                    if (editCubeNode.getCube().getName().equalsIgnoreCase(name) == false) {
                        if (editCubeNode.getDatabase().getCubeByName(name) == null && name.length() > 0) {
                            editCubeNode.getCube().rename(name);
                        } else {
                            JOptionPane.showMessageDialog(dialog, java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Invalid_Cube_Name."), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (PaloJException pe) {
                    JOptionPane.showMessageDialog(dialog, pe.getMessage(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
                } catch (PaloException pe) {
                    JOptionPane.showMessageDialog(dialog, pe.getDescription() + ", " + pe.getReason(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
                }
                buildOverview(false);
                updateDatabasePanelButtonStates();
            }
        }
    }

    private class ElementCellEditorListener implements CellEditorListener
    {
        Dialog dialog;

        public ElementCellEditorListener(Dialog dialog) {
            this.dialog = dialog;
        }

        public void editingCanceled(ChangeEvent event) {
             updateDimensionPanelButtonStates();
        }

        public void editingStopped(ChangeEvent e) {
            if (editElementNode != null) {
                String name = (String)cellEditorElements.getCellEditorValue();
                try {
                    if (editElementNode.getElement().getName().equalsIgnoreCase(name) == false) {
                        if (editElementNode.getDimension().getElementByName(name,false) == null && name.length() > 0) {
                            editElementNode.getElement().rename(name);
                        } else {
                            JOptionPane.showMessageDialog(dialog, java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Invalid_Element_Name."), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (PaloJException pe) {
                    JOptionPane.showMessageDialog(dialog, pe.getMessage(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
                } catch (PaloException pe) {
                    JOptionPane.showMessageDialog(dialog, pe.getDescription() + ", " + pe.getReason(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
                }
                TreePath path = treeOverview.getSelectionPath();
                if (path != null) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
                    if (node instanceof AttributesTreeNode) {
                        buildOverview(false);
                    }
                }
                updateDimensionPanelButtonStates();
            }
        }
    }

    private class GroupCellEditorListener implements CellEditorListener
    {
        Dialog dialog;

        public GroupCellEditorListener(Dialog dialog) {
            this.dialog = dialog;
        }

        public void editingCanceled(ChangeEvent event) {
             updateGroupsPanelButtonStates();
        }

        public void editingStopped(ChangeEvent e) {
            if (editGroupNode != null) {
                String name = (String)cellEditorGroups.getCellEditorValue();
                try {
                    if (editGroupNode.getGroup().getElement().getName().equalsIgnoreCase(name) == false) {
                        if (editGroupNode.getDimension().getElementByName(name,false) == null && name.length() > 0) {
                            editGroupNode.getGroup().getElement().rename(name);
                        } else {
                            JOptionPane.showMessageDialog(dialog, java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Invalid_Group_Name."), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (PaloJException pe) {
                    JOptionPane.showMessageDialog(dialog, pe.getMessage(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
                } catch (PaloException pe) {
                    JOptionPane.showMessageDialog(dialog, pe.getDescription() + ", " + pe.getReason(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
                }
                buildOverview(false);
                updateGroupsPanelButtonStates();
            }
        }
    }

    private class UserCellEditorListener implements CellEditorListener
    {
        Dialog dialog;

        public UserCellEditorListener(Dialog dialog) {
            this.dialog = dialog;
        }

        public void editingCanceled(ChangeEvent event) {
             updateUsersPanelButtonStates();
        }

        public void editingStopped(ChangeEvent e) {
            if (editUserNode != null) {
                String name = (String)cellEditorUsers.getCellEditorValue();
                try {
                    if (editUserNode.getUser().getElement().getName().equalsIgnoreCase(name) == false) {
                        if (editUserNode.getDimension().getElementByName(name,false) == null &&
                                name.length() > 0 && name.contains(" ") == false) {
                            editUserNode.getUser().getElement().rename(name);
                        } else {
                            JOptionPane.showMessageDialog(dialog, java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Invalid_User_Name."), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (PaloJException pe) {
                    JOptionPane.showMessageDialog(dialog, pe.getMessage(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
                } catch (PaloException pe) {
                    JOptionPane.showMessageDialog(dialog, pe.getDescription() + ", " + pe.getReason(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
                }
                buildOverview(false);
                updateUsersPanelButtonStates();
            }
        }
    }

    private class RoleCellEditorListener implements CellEditorListener
    {
        Dialog dialog;

        public RoleCellEditorListener(Dialog dialog) {
            this.dialog = dialog;
        }

        public void editingCanceled(ChangeEvent event) {
             updateRolesPanelButtonStates();
        }

        public void editingStopped(ChangeEvent e) {
            if (editRoleNode != null) {
                String name = (String)cellEditorRoles.getCellEditorValue();
                try {
                    if (editRoleNode.getRole().getElement().getName().equalsIgnoreCase(name) == false) {
                        if (editRoleNode.getDimension().getElementByName(name,false) == null && name.length() > 0) {
                            editRoleNode.getRole().getElement().rename(name);
                        } else {
                            JOptionPane.showMessageDialog(dialog, java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Invalid_Role_Name."), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (PaloJException pe) {
                    JOptionPane.showMessageDialog(dialog, pe.getMessage(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
                } catch (PaloException pe) {
                    JOptionPane.showMessageDialog(dialog, pe.getDescription() + ", " + pe.getReason(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
                }
                buildOverview(false);
                updateRolesPanelButtonStates();
            }
        }
    }

    private class AttributeCellEditorListener implements CellEditorListener
    {
        Dialog dialog;

        public AttributeCellEditorListener(Dialog dialog) {
            this.dialog = dialog;
        }

        public void editingCanceled(ChangeEvent event) {
             updateAttributesPanelButtonStates();
        }

        public void editingStopped(ChangeEvent e) {
            if (editAttributeNode != null) {
                String name = (String)cellEditorAttributes.getCellEditorValue();
                try {
                    editAttributeNode.getAttribute().rename(name);
                } catch (PaloJException pe) {
                    JOptionPane.showMessageDialog(dialog, pe.getMessage(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
                } catch (PaloException pe) {
                    JOptionPane.showMessageDialog(dialog, pe.getDescription() + ", " + pe.getReason(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
                }
                buildOverview(false);
                updateAttributesPanelButtonStates();
            }
        }
    }

    private void buildOverview(boolean initial)
    {
        long time = System.currentTimeMillis();
        ArrayList<ConnectionInfo>  info = connectionHandler.getConnections();
        TreePath selPath = treeOverview.getSelectionPath();

        Enumeration state;

        state = treeOverview.getExpandedDescendants(new TreePath(rootNode));

        rootNode.removeAllChildren();

        for (int i = 0; i < info.size(); i++) {
            ConnectionTreeNode cNode = new ConnectionTreeNode(info.get(i));
            rootNode.add(cNode);
            if (initial == true) {
                if (info.get(i).isAutoLogin()) {
                    info.get(i).connect(this,2000);
                }
            }
            if (info.get(i).getState() == ConnectionState.Connected) {
                IConnection connection =  info.get(i).getConnection();
                if (connection != null) {
                    IDatabase[] dbs = connection.getDatabases();
                    for (int j = 0; j < dbs.length; j++) {
                        //dbs[j].setValidationEnabled(false);
                        if (dbs[j].getType().equals(IDatabase.DatabaseType.DATABASE_NORMAL)) {
                            DatabaseTreeNode dbNode = new DatabaseTreeNode(dbs[j],connection);
                            cNode.add(dbNode);
                            System.err.println("Pre Dimension: "+(System.currentTimeMillis()-time));
                            addDimensions(dbNode);
                            System.err.println("Pre Cube: "+(System.currentTimeMillis()-time));
                            addCubes(dbNode);
                        }
                    }
                    IDatabase systemDB = connection.getDatabaseByName("System");
                    System.err.println("Pre Groups: "+(System.currentTimeMillis()-time));
                    addGroups(cNode, systemDB);
                    System.err.println("Pre Users: "+(System.currentTimeMillis()-time));
                    addUsers(cNode, systemDB);
                    System.err.println("Pre Roles: "+(System.currentTimeMillis()-time));
                    addRoles(cNode, systemDB);
                }
            }
        }

        overviewTreeModel.reload();
        
        if (state != null) {
            TreePath treePathNew = null;
            while (state.hasMoreElements()) {
                TreePath treePathOld = (TreePath) state.nextElement();
                int pathCount = treePathOld.getPathCount();
                String  names[] = new String[pathCount];

                for (int i = 0; i < pathCount; i++) {
                    names[i] = treePathOld.getPathComponent(i).toString();
                }
                        
                treePathNew = findByName(treeOverview, names);
                if (treePathNew != null) {
                    treeOverview.expandPath(treePathNew);
                }
            }
        } else {
            expandToLevel(treeOverview, 1);
        }

        if (initial) {
            if (overviewSelection != null) {
                TreePath treePathNew = findByName(treeOverview, overviewSelection);
                if (treePathNew != null) {
                    treeOverview.scrollPathToVisible(treePathNew);
                    treeOverview.setSelectionPath(treePathNew);
                }
            }
        } else if (selPath != null) {
            int pathCount = selPath.getPathCount();
            overviewSelection = new String[pathCount];

            for (int i = 0; i < pathCount; i++) {
                overviewSelection[i] = selPath.getPathComponent(i).toString();
            }

            TreePath treePathNew = findByName(treeOverview, overviewSelection);
            if (treePathNew != null)
                treeOverview.setSelectionPath(treePathNew);
        }

        treeOverview.repaint();
        System.err.println("finished: "+(System.currentTimeMillis()-time));
    }

    private void addGroups(ConnectionTreeNode cNode, IDatabase sysDB)
    {
        if (sysDB == null)              // not enough rights
            return;

        GroupsTreeNode nodeGroups = new GroupsTreeNode(sysDB, resourceBundle.getString("Groups")); // NOI18N);
        cNode.add(nodeGroups);

        PaloAdministration  admin = new PaloAdministration(sysDB);
        rootNodeGroups.setDatabase(sysDB);

        PaloGroup[] groups = admin.getGroups();
        if (groups == null)
            return;

        for (int i = 0; i < groups.length; i++) {
            GroupTreeNode groupNode = new GroupTreeNode(groups[i],admin.getDimensionByName(PaloAdministration.DIMENSION_GROUP));
            nodeGroups.add(groupNode);
        }
    }

    private void addUsers(ConnectionTreeNode cNode, IDatabase sysDB)
    {
        if (sysDB == null)              // not enough rights
            return;

        UsersTreeNode nodeUsers = new UsersTreeNode(sysDB, resourceBundle.getString("Users")); // NOI18N);
        cNode.add(nodeUsers);

        PaloAdministration  admin = new PaloAdministration(sysDB);
        rootNodeUsers.setDatabase(sysDB);

        PaloUser[] users = admin.getUsers();
        if (users == null)
            return;

        for (int i = 0; i < users.length; i++) {
            UserTreeNode userNode = new UserTreeNode(users[i],admin.getDimensionByName(PaloAdministration.DIMENSION_USER));
            nodeUsers.add(userNode);
        }
    }

    private void addRoles(ConnectionTreeNode cNode, IDatabase sysDB)
    {
        if (sysDB == null)              // not enough rights
            return;

        RolesTreeNode nodeRoles = new RolesTreeNode(sysDB, resourceBundle.getString("Roles")); // NOI18N);
        cNode.add(nodeRoles);

        PaloAdministration  admin = new PaloAdministration(sysDB);
        rootNodeRoles.setDatabase(sysDB);

        PaloRole[] roles = admin.getRoles();
        if (roles == null)
            return;

        for (int i = 0; i < roles.length; i++) {
            RoleTreeNode roleNode = new RoleTreeNode(roles[i],admin.getDimensionByName(PaloAdministration.DIMENSION_ROLE));
            nodeRoles.add(roleNode);
        }
    }

// Finds the path in tree as specified by the array of names. The  names array is a
// sequence of names where names[0] is the root and names[i] is a child of names[i-1].
// Comparison is done using String.equals(). Returns null if not found.
    public TreePath findByName(JTree tree, String[] names) {
        TreeNode root = (TreeNode) tree.getModel().getRoot();
        return find2(tree, new TreePath(root), names, 0, true);
    }

    private TreePath find2(JTree tree, TreePath parent, Object[] nodes,
            int depth, boolean byName) {
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        Object o = node;

        if (o == null)
            return null;

        // If by name, convert node to a string
        if (byName) {
            o = o.toString();
        }

        if (o == null)
            return null;

        // If equal, go down the branch
        if (o.equals(nodes[depth])) {
            // If at end, return match
            if (depth == nodes.length - 1) {
                return parent;
            }

            // Traverse children
            if (node.getChildCount() >= 0) {
                for (Enumeration e = node.children();
                        e.hasMoreElements();) {
                    TreeNode n = (TreeNode) e.nextElement();
                    TreePath path = parent.pathByAddingChild(n);
                    TreePath result = find2(tree, path, nodes, depth + 1,
                            byName);
                    // Found a match
                    if (result != null) {
                        return result;
                    }
                }
            }
        }

        // No match at this branch
        return null;
    }

    private boolean addDimensions(IDatabase db, DefaultMutableTreeNode node, IDimension.DimensionType type, IDimension[] dimensions, boolean subsets) {
        long time = System.currentTimeMillis();
        boolean found = false;
        if (dimensions == null)
            return false;
        for (int i = 0; i < dimensions.length; i++) {
                if (dimensions[i].getType().equals(type) || node instanceof CubeTreeNode) {
                    DimensionTreeNode dimensionNode = new DimensionTreeNode(dimensions[i],db);
                    node.add(dimensionNode);
                    if (subsets) {
                        addSubsets(dimensionNode);
                        if (!(node instanceof CubeTreeNode)) {
                            AttributesTreeNode attributesNode = new AttributesTreeNode(dimensionNode.getDimension(), resourceBundle.getString("Attributes"), dimensionNode.getDatabase()); // NOI18N);
                            dimensionNode.add(attributesNode);
                            DimensionRightsTreeNode rnode = new DimensionRightsTreeNode(dimensionNode.getDimension(), resourceBundle.getString("Rights")); // NOI18N);
                            dimensionNode.add(rnode);
                        }
                    }
                    found = true;
                }
        }

        return found;
    }

    private void addDimensions(DatabaseTreeNode dbNode) {
        IDatabase db = dbNode.getDatabase();

        DimensionsTreeNode node = new DimensionsTreeNode(db, resourceBundle.getString("Dimensions")); // NOI18N);
        dbNode.add(node);

        IDimension[] dims = db.getDimensions();
        if (dims == null)
            return;
        addDimensions(db, node, IDimension.DimensionType.DIMENSION_NORMAL, dims, true);
    }

    private void addSubsets(DimensionTreeNode dimNode) {

        /* deactivated work in progress
        ConnectionTreeNode connNode = getConnectionNode(dimNode);
        if (connNode != null && connNode.getConnection().getType() == ConnectionInfo.TYPE_HTTP) {
            ModellerFolderTreeNode node = new ModellerFolderTreeNode(resourceBundle.getString("Subsets")); // NOI18N);
            dimNode.add(node);
        }
         *
         */
        /*
        Subset2[] subs = dimNode.getDimension().getSubsetHandler().getSubsets();

        if (subs != null) {
            for (int i = 0; i < subs.length; i++) {
                SubsetTreeNode subNode = new SubsetTreeNode(subs[i]);
                node.add(subNode);
            }
        }
         *
         */
    }

    private void addAttributes(AttributesTreeNode attributesNode) {

        DimensionTreeNode dimNode = (DimensionTreeNode)attributesNode.getParent();

        IDimension dim = dimNode.getDimension();
        IAttribute[]  attributes = dim.getAttributes();

        if (attributes != null) {
            for (int i = 0; i < attributes.length; i++) {
                AttributeTreeNode attrNode = new AttributeTreeNode(attributes[i],dim);
                attributesNode.add(attrNode);
            }
        }
    }

    private boolean addCubes(IDatabase db, DefaultMutableTreeNode node, ICube.CubeType type, boolean addDims) {
        boolean found = false;
        long time = System.currentTimeMillis();
        ICube[] cubes = db.getCubes();
        if (cubes == null)
            return false;

        for (int i = 0; i < cubes.length; i++) {
            if (cubes[i].getType().equals(type)) {
                    CubeTreeNode cubeNode = new CubeTreeNode(cubes[i],db);
                    node.add(cubeNode);
                    if (addDims) {
                        addDimensions(db, cubeNode, IDimension.DimensionType.DIMENSION_NORMAL, cubes[i].getDimensions(), false);
                    } else {
                        CubeRulesTreeNode nodeRules = new CubeRulesTreeNode(cubes[i], resourceBundle.getString("RulesNode")); // NOI18N);
                        cubeNode.add(nodeRules);
                        CubeRightsTreeNode nodeRights = new CubeRightsTreeNode(cubes[i], resourceBundle.getString("Rights"),db); // NOI18N);
                        cubeNode.add(nodeRights);
                    }
                    found = true;
                }
        }

        return found;
    }

    private void addCubes(DatabaseTreeNode dbNode) {
        CubesTreeNode node = new CubesTreeNode(dbNode.getDatabase(), resourceBundle.getString("Cubes")); // NOI18N);
        dbNode.add(node);

        addCubes(dbNode.getDatabase(), node, ICube.CubeType.CUBE_NORMAL, false);
    }

    private void fillDatabaseTree(ConnectionTreeNode conNode) {
        ConnectionInfo con = conNode.getConnection();
        rootNodeDatabases.removeAllChildren();
        rootNodeDatabases.setConnection(con);

        IConnection connection =  con.getConnection();
        if (connection != null) {
            IDatabase[] dbs = connection.getDatabases();

            for (int j = 0; j < dbs.length; j++) {
                if (dbs[j].getType().equals(IDatabase.DatabaseType.DATABASE_NORMAL)) {
                    DatabaseTreeNode dbNode = new DatabaseTreeNode(dbs[j],con.getConnection());
                    rootNodeDatabases.add(dbNode);
                }
            }
        }
        databaseTreeModel.reload();
    }

    private void fillCubeTree(DatabaseTreeNode dbNode, ICube.CubeType type) {
        IDatabase db = dbNode.getDatabase();

        rootNodeCubes.removeAllChildren();
        rootNodeCubes.setDatabase(db);
        addCubes(db, rootNodeCubes, type, true);
        cubeTreeModel.reload();
    }

    private void fillDimensionTree(DatabaseTreeNode dbNode, IDimension.DimensionType type) {
        IDatabase db = dbNode.getDatabase();
        IDimension[] dims = db.getDimensions();
        if (dims == null)
            return;

        rootNodeDimensions.removeAllChildren();
        rootNodeDimensions.setDatabase(db);
        addDimensions(db, rootNodeDimensions, type, dims, false);
        dimensionTreeModel.reload();
    }

    private boolean fillElementList(IDimension dim) {
        rootNodeElements.removeAllChildren();
        rootNodeElements.setDimension(dim);

        IElement[] elems = dim.getElements(false);
        if (elems == null)
            return false;

        for (int i = 0; i < elems.length; i++) {
            ElementTreeNode elemNode = new ElementTreeNode(elems[i],dim);
            rootNodeElements.add(elemNode);
        }

        elementTreeModel.reload();

        return true;
    }

     protected void visitElement(DefaultMutableTreeNode parentNode, IElement element, IDimension dimension) {
        DefaultMutableTreeNode childNode = new ElementTreeNode(element,dimension);
        parentNode.add(childNode);
        for (IElement c : element.getChildren()) {
            visitElement(childNode,c,dimension);
        }
    }

    private boolean fillElementHierarchy(IDimension dim) {
        rootNodeElementHierarchy.removeAllChildren();
        rootNode.removeAllChildren();

        if (dim != null) {

            for (IElement root : dim.getRootElements(false)) {
                visitElement(rootNode,root,dim);
            }
            elementHierarchyTreeModel.setRoot(rootNode);
            elementHierarchyTreeModel.reload();
        }

        return true;
    }

    private boolean fillConsolidatedElementList(ElementTreeNode elemNode) {
        IElement elem = elemNode.getElement();

        rootNodeConsolidatedElements.removeAllChildren();
        rootNodeConsolidatedElements.setDimension(elemNode.getDimension());

        IElement[] elems = elem.getChildren();
        if (elems == null)
            return false;

        for (int i = 0; i < elems.length; i++) {
            ElementTreeNode node = new ElementTreeNode(elems[i],elemNode.getDimension());
            rootNodeConsolidatedElements.add(node);
        }

        consolidatedElementTreeModel.reload();

        return true;
    }

    private DefaultMutableTreeNode getSelected(JTree tree) {
        TreePath path = tree.getSelectionPath();

        if (path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
            return node;
        }
        return null;
    }

    private void deleteSelectedDimension()
    {
        DefaultMutableTreeNode node = getSelected(treeDimensions);
        if (node instanceof DimensionTreeNode) {
            try {
                DimensionTreeNode dimNode = (DimensionTreeNode)node;
                dimNode.getDatabase().removeDimension(dimNode.getDimension());
                dimensionTreeModel.removeNodeFromParent(dimNode);
                dimensionTreeModel.nodeStructureChanged(rootNodeDimensions);
            } catch (PaloJException pe) {
                JOptionPane.showMessageDialog(this, pe.getMessage(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
            } catch (PaloException pe) {
                JOptionPane.showMessageDialog(this, pe.getDescription() + ", " + pe.getReason(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
            }
        }
        updateDatabasePanelButtonStates();
        buildOverview(false);
    }

    private void createDimension()
    {
        DefaultMutableTreeNode node = getSelected(treeOverview);
        DatabaseTreeNode dbNode = getDBNode(node);
        if (dbNode != null) {
            try {
                IDatabase db = dbNode.getDatabase();
                String name = java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("NewDimension");
                int i =  1;
                while (db.getDimensionByName(name + i) != null) {
                    i++;
                }
                IDimension dim = db.addDimension(name + i);
                if (dim != null) {
                    editDimNode = new DimensionTreeNode(dim,db);
                    rootNodeDimensions.add(editDimNode);
                    dimensionTreeModel.nodeStructureChanged(rootNodeDimensions);
                    TreePath selection = new TreePath(((DefaultMutableTreeNode)editDimNode).getPath());
                    treeDimensions.setSelectionPath(selection);
                    treeDimensions.scrollPathToVisible(selection);
                    treeDimensions.startEditingAtPath(selection);
                }
            } catch (PaloJException pe) {
                JOptionPane.showMessageDialog(this, pe.getMessage(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
            } catch (PaloException pe) {
                JOptionPane.showMessageDialog(this, pe.getDescription() + ", " + pe.getReason(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
            }
            updateDatabasePanelButtonStates();
        }
    }

    private void createDatabase() {

        DefaultMutableTreeNode node = getSelected(treeOverview);
        if (node instanceof ConnectionTreeNode) {
            try  {
                ConnectionTreeNode conNode = (ConnectionTreeNode)node;
                ConnectionInfo con = conNode.getConnection();

                IConnection connection =  con.getConnection();
                if (connection != null) {
                    String name = java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("NewDatabase");
                    int i =  1;
                    while (connection.getDatabaseByName(name + i) != null) {
                        i++;
                    }

                   //cschw: since we cannot rename databases (backend does not support it cleanly), we create it after we new the name
                    editDatabaseNode = new DatabaseTreeNode(null,connection);
                    rootNodeDatabases.add(editDatabaseNode);
                    databaseTreeModel.nodeStructureChanged(rootNodeDatabases);
                    TreePath selection = new TreePath(((DefaultMutableTreeNode)editDatabaseNode).getPath());
                    treeDatabases.setSelectionPath(selection);
                    treeDatabases.scrollPathToVisible(selection);
                    treeDatabases.startEditingAtPath(selection);
                   
                }
            } catch (PaloJException pe) {
                JOptionPane.showMessageDialog(this, pe.getMessage(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
            } catch (PaloException pe) {
                JOptionPane.showMessageDialog(this, pe.getDescription() + ", " + pe.getReason(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
            }
            updateServerPanelButtonStates();
        }
    }

    private void deleteSelectedDatabase() {
        if (JOptionPane.showConfirmDialog(this, java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Delete_selected_Database?"), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Confirmation"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION)
            return;

        DefaultMutableTreeNode node = getSelected(treeDatabases);
        if (node instanceof DatabaseTreeNode) {
            try {
                DatabaseTreeNode dbNode = (DatabaseTreeNode)node;
                dbNode.getConnection().removeDatabase(dbNode.getConnection().getDatabaseByName(dbNode.getDatabase().getName()));
                databaseTreeModel.removeNodeFromParent(dbNode);
                databaseTreeModel.nodeStructureChanged(rootNodeDatabases);
            } catch (PaloJException pe) {
                JOptionPane.showMessageDialog(this, pe.getMessage(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
            } catch (PaloException pe) {
                JOptionPane.showMessageDialog(this, pe.getDescription() + ", " + pe.getReason(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
            }
            updateServerPanelButtonStates();
            buildOverview(false);
        }
    }

    private void deleteSelectedElement()
    {
        TreePath    paths[] = treeElements.getSelectionPaths();

        int ret = JOptionPane.showConfirmDialog(this,
                java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Delete_selected_Element?"),
                java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Confirmation"), JOptionPane.OK_CANCEL_OPTION);

        if (ret == JOptionPane.CANCEL_OPTION || ret == JOptionPane.CLOSED_OPTION)
            return;

        try {
            for (int i = 0; i < paths.length; i++) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)paths[i].getLastPathComponent();
                if (node instanceof ElementTreeNode) {
                    ElementTreeNode elemNode = (ElementTreeNode)node;
                    IElement elem = elemNode.getElement();
                    elemNode.getDimension().removeElements(new IElement[]{elem});
                    elementTreeModel.removeNodeFromParent(elemNode);
                    if (elemNode.getDimension().getType().equals(IDimension.DimensionType.DIMENSION_ATTRIBUTE)) {
                        buildOverview(false);
                    }
                }
            }
        } catch (PaloJException pe) {
            JOptionPane.showMessageDialog(this, pe.getMessage(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
        } catch (PaloException pe) {
            JOptionPane.showMessageDialog(this, pe.getDescription() + ", " + pe.getReason(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
        }
        elementTreeModel.nodeStructureChanged(rootNodeElements);
    }

    private void createElement()
    {
        try {
            IDimension dim = rootNodeElements.getDimension();
            String name = java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("NewElement");
            int i =  1;

            while (dim.getElementByName(name + i,false) != null) {
                i++;
            }

            dim.addElements(new String[]{name + i}, new IElement.ElementType[]{IElement.ElementType.ELEMENT_NUMERIC});
            IElement elem = dim.getElementByName(name+i, false);
            if (elem == null)
                return;

            editElementNode = new ElementTreeNode(elem,dim);

            DefaultMutableTreeNode node = getSelected(treeElements);
            if (node == null) {
                rootNodeElements.add(editElementNode);
            } else {
                int selRows[] = treeElements.getSelectionRows();
                if (selRows.length == 0)
                    return;

                elem.move(selRows[0] + 1);
                rootNodeElements.insert(editElementNode, selRows[0] + 1);
            }

            elementTreeModel.nodeStructureChanged(rootNodeElements);

            TreePath selection = new TreePath(((DefaultMutableTreeNode)editElementNode).getPath());
            treeElements.setSelectionPath(selection);
            treeElements.scrollPathToVisible(selection);
            treeElements.startEditingAtPath(selection);

        } catch (PaloJException pe) {
            JOptionPane.showMessageDialog(this, pe.getMessage(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
        } catch (PaloException pe) {
            JOptionPane.showMessageDialog(this, pe.getDescription() + ", " + pe.getReason(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
        }

        updateDimensionPanelButtonStates();
    }

    private void editDimensionName()
    {
        DefaultMutableTreeNode node = getSelected(treeDimensions);
        if (node instanceof DimensionTreeNode) {
            try {
                editDimNode = (DimensionTreeNode)node;
                TreePath selection = new TreePath(((DefaultMutableTreeNode)editDimNode).getPath());
                treeDimensions.setSelectionPath(selection);
                treeDimensions.scrollPathToVisible(selection);
                treeDimensions.startEditingAtPath(selection);
            } catch (PaloJException pe) {
                JOptionPane.showMessageDialog(this, pe.getMessage(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
            } catch (PaloException pe) {
                JOptionPane.showMessageDialog(this, pe.getDescription() + ", " + pe.getReason(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
            }
            updateDatabasePanelButtonStates();
        }
    }

    private void editCubeName()
    {
        DefaultMutableTreeNode node = getSelected(treeCubes);
        if (node instanceof CubeTreeNode) {
            try {
                editCubeNode = (CubeTreeNode)node;
                TreePath selection = new TreePath(((DefaultMutableTreeNode)editCubeNode).getPath());
                treeCubes.setSelectionPath(selection);
                treeCubes.scrollPathToVisible(selection);
                treeCubes.startEditingAtPath(selection);
            } catch (PaloJException pe) {
                JOptionPane.showMessageDialog(this, pe.getMessage(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
            } catch (PaloException pe) {
                JOptionPane.showMessageDialog(this, pe.getDescription() + ", " + pe.getReason(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
            }
            updateDatabasePanelButtonStates();
        }
    }

    private void editElementName()
    {
        DefaultMutableTreeNode node = getSelected(treeElements);
        if (node instanceof ElementTreeNode) {
            try {
                editElementNode = (ElementTreeNode)node;
                TreePath selection = new TreePath(((DefaultMutableTreeNode)editElementNode).getPath());
                treeElements.setSelectionPath(selection);
                treeElements.scrollPathToVisible(selection);
                treeElements.startEditingAtPath(selection);
            } catch (PaloJException pe) {
                JOptionPane.showMessageDialog(this, pe.getMessage(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
            } catch (PaloException pe) {
                JOptionPane.showMessageDialog(this, pe.getDescription() + ", " + pe.getReason(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteSelectedCube()
    {
        if (JOptionPane.showConfirmDialog(this, java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Delete_selected_Cube?"), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Confirmation"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION)
            return;

        DefaultMutableTreeNode node = getSelected(treeCubes);
        if (node instanceof CubeTreeNode) {
            try {
                CubeTreeNode cubeNode = (CubeTreeNode)node;
                cubeNode.getDatabase().removeCube(cubeNode.getCube());
                cubeTreeModel.removeNodeFromParent(cubeNode);
                cubeTreeModel.nodeStructureChanged(rootNodeCubes);
            } catch (PaloJException pe) {
                JOptionPane.showMessageDialog(this, pe.getMessage(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
            } catch (PaloException pe) {
                JOptionPane.showMessageDialog(this, pe.getDescription() + ", " + pe.getReason(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
            }
            updateDatabasePanelButtonStates();
            buildOverview(false);
        }
    }

    private void createGroup()
    {
        try {
            IDatabase db = rootNodeGroups.getDatabase();
            IDimension dim = db.getDimensionByName(PaloAdministration.DIMENSION_GROUP);
            String name = java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("NewGroup");
            int i =  1;

            while (dim.getElementByName(name + i,false) != null) {
                i++;
            }

            dim.addElements(new String[]{name + i}, new IElement.ElementType[]{IElement.ElementType.ELEMENT_STRING});
            IElement elem = dim.getElementByName(name+i, false);
            if (elem == null)
                return;

            editGroupNode = new GroupTreeNode(new PaloGroup(elem, db),dim);

            DefaultMutableTreeNode node = getSelected(treeGroups);
            if (node == null) {
                rootNodeGroups.add(editGroupNode);
            } else {
                int selRows[] = treeGroups.getSelectionRows();
                if (selRows.length == 0)
                    return;

                elem.move(selRows[0] + 1);
                rootNodeGroups.insert(editGroupNode, selRows[0] + 1);
            }

            groupTreeModel.nodeStructureChanged(rootNodeGroups);

            TreePath selection = new TreePath(((DefaultMutableTreeNode)editGroupNode).getPath());
            treeGroups.setSelectionPath(selection);
            treeGroups.scrollPathToVisible(selection);
            treeGroups.startEditingAtPath(selection);
        } catch (PaloJException pe) {
            JOptionPane.showMessageDialog(this, pe.getMessage(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
        } catch (PaloException pe) {
            JOptionPane.showMessageDialog(this, pe.getDescription() + ", " + pe.getReason(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
        }

        updateGroupsPanelButtonStates();
    }

    private void editGroupName()
    {
        DefaultMutableTreeNode node = getSelected(treeGroups);
        if (node instanceof GroupTreeNode) {
            try {
                editGroupNode = (GroupTreeNode)node;
                TreePath selection = new TreePath(((DefaultMutableTreeNode)editGroupNode).getPath());
                treeGroups.setSelectionPath(selection);
                treeGroups.scrollPathToVisible(selection);
                treeGroups.startEditingAtPath(selection);
            } catch (PaloJException pe) {
                JOptionPane.showMessageDialog(this, pe.getMessage(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
            } catch (PaloException pe) {
                JOptionPane.showMessageDialog(this, pe.getDescription() + ", " + pe.getReason(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
            }
            updateGroupsPanelButtonStates();
        }
    }

    private void deleteSelectedGroup()
    {
        DefaultMutableTreeNode node = getSelected(treeGroups);
        if (node instanceof GroupTreeNode) {
            try {
                GroupTreeNode groupNode = (GroupTreeNode)node;
                IElement elem = groupNode.getGroup().getElement();
                groupNode.getDimension().removeElements(new IElement[]{elem});
                groupTreeModel.removeNodeFromParent(groupNode);
                groupTreeModel.nodeStructureChanged(rootNodeGroups);
            } catch (PaloJException pe) {
                JOptionPane.showMessageDialog(this, pe.getMessage(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
            } catch (PaloException pe) {
                JOptionPane.showMessageDialog(this, pe.getDescription() + ", " + pe.getReason(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
            }
            updateGroupsPanelButtonStates();
            buildOverview(false);
        }
    }

    private void editUserName()
    {
        DefaultMutableTreeNode node = getSelected(treeUsers);
        if (node instanceof UserTreeNode) {
            try {
                editUserNode = (UserTreeNode)node;
                TreePath selection = new TreePath(((DefaultMutableTreeNode)editUserNode).getPath());
                treeUsers.setSelectionPath(selection);
                treeUsers.scrollPathToVisible(selection);
                treeUsers.startEditingAtPath(selection);
            } catch (PaloJException pe) {
                JOptionPane.showMessageDialog(this, pe.getMessage(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
            } catch (PaloException pe) {
                JOptionPane.showMessageDialog(this, pe.getDescription() + ", " + pe.getReason(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
            }
            updateUsersPanelButtonStates();
        }
    }

    private void deleteSelectedUser()
    {
        DefaultMutableTreeNode node = getSelected(treeUsers);
        if (node instanceof UserTreeNode) {
            try {
                UserTreeNode userNode = (UserTreeNode)node;
                IElement elem = userNode.getUser().getElement();
                userNode.getDimension().removeElements(new IElement[]{elem});
                userTreeModel.removeNodeFromParent(userNode);
                userTreeModel.nodeStructureChanged(rootNodeUsers);
            } catch (PaloJException pe) {
                JOptionPane.showMessageDialog(this, pe.getMessage(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
            } catch (PaloException pe) {
                JOptionPane.showMessageDialog(this, pe.getDescription() + ", " + pe.getReason(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
            }
            updateUsersPanelButtonStates();
            buildOverview(false);
        }
    }

    private void createUser()
    {
        try {
            IDatabase db = rootNodeUsers.getDatabase();
            IDimension dim = db.getDimensionByName(PaloAdministration.DIMENSION_USER);
            String name = java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("NewUser");
            int i =  1;

            while (dim.getElementByName(name + i,false) != null) {
                i++;
            }

            dim.addElements(new String[]{name + i}, new IElement.ElementType[]{IElement.ElementType.ELEMENT_STRING});
            IElement elem = dim.getElementByName(name+i, false);
            if (elem == null)
                return;

            editUserNode = new UserTreeNode(new PaloUser(elem, db),dim);

            DefaultMutableTreeNode node = getSelected(treeUsers);
            if (node == null) {
                rootNodeUsers.add(editUserNode);
            } else {
                int selRows[] = treeUsers.getSelectionRows();
                if (selRows.length == 0)
                    return;

                elem.move(selRows[0] + 1);
                rootNodeUsers.insert(editUserNode, selRows[0] + 1);
            }

            userTreeModel.nodeStructureChanged(rootNodeUsers);

            TreePath selection = new TreePath(((DefaultMutableTreeNode)editUserNode).getPath());
            treeUsers.setSelectionPath(selection);
            treeUsers.scrollPathToVisible(selection);
            treeUsers.startEditingAtPath(selection);

        } catch (PaloJException pe) {
            JOptionPane.showMessageDialog(this, pe.getMessage(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
        } catch (PaloException pe) {
            JOptionPane.showMessageDialog(this, pe.getDescription() + ", " + pe.getReason(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
        }

        updateUsersPanelButtonStates();
    }

    private void editRoleName()
    {
        DefaultMutableTreeNode node = getSelected(treeRoles);
        if (node instanceof RoleTreeNode) {
            try {
                editRoleNode = (RoleTreeNode)node;
                TreePath selection = new TreePath(((DefaultMutableTreeNode)editRoleNode).getPath());
                treeRoles.setSelectionPath(selection);
                treeRoles.scrollPathToVisible(selection);
                treeRoles.startEditingAtPath(selection);
            } catch (PaloJException pe) {
                JOptionPane.showMessageDialog(this, pe.getMessage(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
            } catch (PaloException pe) {
                JOptionPane.showMessageDialog(this, pe.getDescription() + ", " + pe.getReason(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
            }
            updateRolesPanelButtonStates();
        }
    }

    private void deleteSelectedRole()
    {
        DefaultMutableTreeNode node = getSelected(treeRoles);
        if (node instanceof RoleTreeNode) {
            try {
                RoleTreeNode roleNode = (RoleTreeNode)node;
                IElement elem = roleNode.getRole().getElement();
                roleNode.getDimension().removeElements(new IElement[]{elem});
                roleTreeModel.removeNodeFromParent(roleNode);
                roleTreeModel.nodeStructureChanged(rootNodeRoles);
            } catch (PaloJException pe) {
                JOptionPane.showMessageDialog(this, pe.getMessage(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
            } catch (PaloException pe) {
                JOptionPane.showMessageDialog(this, pe.getDescription() + ", " + pe.getReason(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
            }
            updateRolesPanelButtonStates();
            buildOverview(false);
        }
    }

    private void createRole()
    {
        try {
            IDatabase db = rootNodeRoles.getDatabase();
            IDimension dim = db.getDimensionByName(PaloAdministration.DIMENSION_ROLE);
            String name = java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("NewRole");
            int i =  1;

            while (dim.getElementByName(name + i,false) != null) {
                i++;
            }

            dim.addElements(new String[]{name + i}, new IElement.ElementType[]{IElement.ElementType.ELEMENT_STRING});
            IElement elem = dim.getElementByName(name + i, false);
            if (elem == null)
                return;

            editRoleNode = new RoleTreeNode(new PaloRole(elem, db),dim);

            DefaultMutableTreeNode node = getSelected(treeRoles);
            if (node == null) {
                rootNodeRoles.add(editRoleNode);
            } else {
                int selRows[] = treeRoles.getSelectionRows();
                if (selRows.length == 0)
                    return;

                elem.move(selRows[0] + 1);
                rootNodeRoles.insert(editRoleNode, selRows[0] + 1);
            }

            roleTreeModel.nodeStructureChanged(rootNodeRoles);

            TreePath selection = new TreePath(((DefaultMutableTreeNode)editRoleNode).getPath());
            treeRoles.setSelectionPath(selection);
            treeRoles.scrollPathToVisible(selection);
            treeRoles.startEditingAtPath(selection);

        } catch (PaloJException pe) {
            JOptionPane.showMessageDialog(this, pe.getMessage(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
        } catch (PaloException pe) {
            JOptionPane.showMessageDialog(this, pe.getDescription() + ", " + pe.getReason(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
        }

        updateRolesPanelButtonStates();
    }

    private void editAttributeName()
    {
        DefaultMutableTreeNode node = getSelected(treeAttributes);
        if (node instanceof AttributeTreeNode) {
            try {
                editAttributeNode = (AttributeTreeNode)node;
                TreePath selection = new TreePath(((DefaultMutableTreeNode)editAttributeNode).getPath());
                treeAttributes.setSelectionPath(selection);
                treeAttributes.scrollPathToVisible(selection);
                treeAttributes.startEditingAtPath(selection);
            } catch (PaloJException pe) {
                JOptionPane.showMessageDialog(this, pe.getMessage(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
            } catch (PaloException pe) {
                JOptionPane.showMessageDialog(this, pe.getDescription() + ", " + pe.getReason(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
            }
            updateAttributesPanelButtonStates();
        }
    }

    private void deleteSelectedAttribute()
    {
        DefaultMutableTreeNode node = getSelected(treeAttributes);
        if (node instanceof AttributeTreeNode) {
            try {
                AttributeTreeNode attributeNode = (AttributeTreeNode)node;
                IDimension dim = rootNodeAttributes.getDimension();
                dim.removeAttributes(new IAttribute[]{attributeNode.getAttribute()});
                attributesTreeModel.removeNodeFromParent(attributeNode);
                attributesTreeModel.nodeStructureChanged(rootNodeAttributes);
            } catch (PaloJException pe) {
                JOptionPane.showMessageDialog(this, pe.getMessage(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
            } catch (PaloException pe) {
                JOptionPane.showMessageDialog(this, pe.getDescription() + ", " + pe.getReason(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
            }
            updateAttributesPanelButtonStates();
            buildOverview(false);
        }
    }

    private void createAttribute()
    {
        try {
            IDimension dim = rootNodeAttributes.getDimension();
            String name = java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("NewAttribute");
            int i =  1;

            while (dim.getAttributeByName(name + i) != null) {
                i++;
            }

            dim.addAttributes(new String[]{name+i}, new IElement.ElementType[]{IElement.ElementType.ELEMENT_STRING});
            IAttribute attr = dim.getAttributeByName(name + i);
            if (attr == null)
                return;

            editAttributeNode = new AttributeTreeNode(attr,dim);

            rootNodeAttributes.add(editAttributeNode);

            attributesTreeModel.nodeStructureChanged(rootNodeAttributes);

            TreePath selection = new TreePath(((DefaultMutableTreeNode)editAttributeNode).getPath());
            treeAttributes.setSelectionPath(selection);
            treeAttributes.scrollPathToVisible(selection);
            treeAttributes.startEditingAtPath(selection);
        } catch (PaloJException pe) {
            JOptionPane.showMessageDialog(this, pe.getMessage(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
        } catch (PaloException pe) {
            JOptionPane.showMessageDialog(this, pe.getDescription() + ", " + pe.getReason(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
        }

        updateAttributesPanelButtonStates();
    }

    private void addElementsToConsolidated() {
        TreePath    paths[] = treeElements.getSelectionPaths();

        for (int i = 0; i < paths.length; i++) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)paths[i].getLastPathComponent();
            if (node instanceof ElementTreeNode) {
                ElementTreeNode elemNode = (ElementTreeNode)node;
                if (elemNode != editElementConsolidationNode) {
                    for (int j = 0; j < rootNodeConsolidatedElements.getChildCount(); j++) {
                        ElementTreeNode consElemNode = (ElementTreeNode)rootNodeConsolidatedElements.getChildAt(j);
                        if (consElemNode.getElement() == elemNode.getElement()) {
                            return;
                        }
                    }

                    ElementTreeNode newNode = new ElementTreeNode(elemNode.getElement(),elemNode.getDimension());
                    rootNodeConsolidatedElements.add(newNode);
                }
            }
        }

        consolidatedElementTreeModel.nodeStructureChanged(rootNodeConsolidatedElements);
        treeElements.repaint();
    }

    private void activateConsolidationMode()
    {
        TreePath path = treeElements.getSelectionPath();
        if (path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
            if (node instanceof ElementTreeNode) {
                editElementNode = (ElementTreeNode)node;
                editElementConsolidationNode = editElementNode;
                editElementNode.setEditConsolidationMode(true);
                editConsolidationMode = true;
                ((DefaultTreeModel)treeElements.getModel()).nodeChanged(editElementNode);
                treeElements.repaint();
                treeElements.setEditable(false);
            }
        }
        updateDimensionPanelButtonStates();
    }

    private void deactivateConsolidationMode()
    {
        editConsolidationMode = false;
        if (editElementConsolidationNode != null) {
            editElementConsolidationNode.setEditConsolidationMode(false);
            ((DefaultTreeModel)treeElements.getModel()).nodeChanged(editElementConsolidationNode);
        }
        treeElements.repaint();
        treeElements.setEditable(true);
    }

    private void removeSelectedFromConsolidation()
    {
        TreePath    paths[] = treeConsolidation.getSelectionPaths();

        for (int i = 0; i < paths.length; i++) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)paths[i].getLastPathComponent();
            if (node instanceof ElementTreeNode) {
                consolidatedElementTreeModel.removeNodeFromParent(node);
            }
        }

        consolidatedElementTreeModel.nodeStructureChanged(rootNodeConsolidatedElements);
        treeConsolidation.repaint();
    }

    protected void sortList(boolean ascending) {
        ElementTreeNode tmp[] = new ElementTreeNode[elementTreeModel.getChildCount(rootNodeElements)];

        for (int i = 0; i < elementTreeModel.getChildCount(rootNodeElements); i++) {
            tmp[i] = (ElementTreeNode) elementTreeModel.getChild(rootNodeElements, i);
        }

        Arrays.sort(tmp, new ElementComparator());

        rootNodeElements.removeAllChildren();

        if (ascending) {
            for (int i = 0; i < tmp.length; i++) {
                rootNodeElements.add(tmp[i]);
            }
        } else {
            for (int i = tmp.length - 1; i >= 0; i--) {
                rootNodeElements.add(tmp[i]);
            }
        }

        elementTreeModel.nodeStructureChanged(rootNodeConsolidatedElements);
        treeElements.repaint();
    }

    protected class ElementComparator implements Comparator {

        public int compare(Object obj1, Object obj2) {
            ElementTreeNode el1, el2;

            el1 = (ElementTreeNode) obj1;
            el2 = (ElementTreeNode) obj2;

            return el1.getElement().getName().compareTo(el2.getElement().getName());
        }

        public boolean equals(Object obj) {
            return false;
        }
    }

    // find out the lowest level that is collapsed (hidden root is zero)
    protected int getCollapsedLevel(JTree tree) {
        int level = 0;
        int cnt = tree.getRowCount();

        for (int i = 0; i < cnt; i++) {
            TreePath path = tree.getPathForRow(i);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            level = Math.max(level, node.getLevel());
        }

        return level;
    }

    // find out the lowest level that is collapsed (hidden root is zero)
    protected int getExpandedLevel(JTree tree) {
        int level = Integer.MAX_VALUE;
        int cnt = tree.getRowCount();

        if (cnt == 0)
            return 0;

        for (int i = 0; i < cnt; i++) {
            TreePath path = tree.getPathForRow(i);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            if (node.getChildCount() > 0 && tree.isCollapsed(path)) {
                level = Math.min(level, node.getLevel());
            }
        }

        return level;
    }

    protected void expandAll(JTree tree, boolean expand) {
        TreeNode root = (TreeNode) tree.getModel().getRoot();

        // Traverse tree from root
        expandAll(tree, new TreePath(root), expand);
    }

    protected void expandAll(JTree tree, TreePath parent, boolean expand) {
        // Traverse children
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) parent.getLastPathComponent();

        if (node.getChildCount() >= 0) {
            for (Enumeration e = node.children(); e.hasMoreElements();) {
                TreeNode n = (TreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAll(tree, path, expand);
            }
        }

        // Expansion or collapse must be done bottom-up
        if (expand) {
            tree.expandPath(parent);
        } else {
            TreeNode root = (TreeNode) tree.getModel().getRoot();
            if (root != node) {
                tree.collapsePath(parent);
            }
        }
    }

    protected void collapse(JTree tree)
    {
        int level = getCollapsedLevel(tree);
        collapseToLevel(tree, level - 1);
    }

    protected void collapseToLevel(JTree tree, int level)
    {
        int cnt = tree.getRowCount();

        for (int i = cnt - 1; i >= 0; i--) {
            TreePath path = tree.getPathForRow(i);
            ElementTreeNode node = (ElementTreeNode) path.getLastPathComponent();
            if (level == node.getLevel()) {
                tree.collapsePath(path);
                cnt = tree.getRowCount();
            }
        }
    }

    protected void expand(JTree tree) {
        int level = getExpandedLevel(tree);
        expandToLevel(tree, level);
        collapseToLevel(tree, level + 1);
    }

    protected void expandToLevel(JTree tree, int level) {
        int cnt = tree.getRowCount();

        for (int i = 0; i < cnt; i++) {
            TreePath path = tree.getPathForRow(i);
            if (path != null) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                int lv = node.getLevel();
                if (level >= lv) {
                    tree.expandPath(path);
                    cnt = tree.getRowCount();
                }
            }
        }
    }

    private boolean isReadOnly() {
        TreePath path = treeOverview.getSelectionPath();
        if (path != null && path.getPathCount() > 1) {
            ConnectionTreeNode cnode = (ConnectionTreeNode) treeOverview.getSelectionPath().getPathComponent(1);
            return (cnode.getConnection().getType() == ConnectionInfo.TYPE_XMLA);
        }
        return false;
    }

    private void updateDatabasePanelButtonStates() {
        if (isReadOnly()) {
            btnCreateCube.setEnabled(false);
            btnDeleteCube.setEnabled(false);
            btnRenameCube.setEnabled(false);
            btnCreateDimension.setEnabled(false);
            btnDeleteDimension.setEnabled(false);
            btnRenameDimension.setEnabled(false);
        }
        else {
            int selRowsCubes[] = treeCubes.getSelectionRows();

            boolean enable = (selRowsCubes != null) && treeCubes.isEditing() == false;

            btnCreateCube.setEnabled(!treeCubes.isEditing());
            btnDeleteCube.setEnabled(enable);
            btnRenameCube.setEnabled(enable);

            int selRowsDims[] = treeDimensions.getSelectionRows();

            enable = (selRowsDims != null) && treeDimensions.isEditing() == false;

            btnCreateDimension.setEnabled(!treeDimensions.isEditing());
            btnDeleteDimension.setEnabled(enable);
            btnRenameDimension.setEnabled(enable);
        }
    }

    private void updateRulesPanelButtonStates() {
        if (isReadOnly()) {
            btnNewRule.setEnabled(false);
            btnDeleteRule.setEnabled(false);
            btnUpdateRule.setEnabled(false);
            btnCheckRule.setEnabled(false);
            btnRuleUp.setEnabled(false);
            btnRuleDown.setEnabled(false);
        }
        else {
            int selRowsRules[] = tableRules.getSelectedRows();

            boolean enable = (selRowsRules != null);

            btnNewRule.setEnabled(true);
            btnDeleteRule.setEnabled(enable);
            btnUpdateRule.setEnabled(enable);
            btnCheckRule.setEnabled(enable);

            if (selRowsRules != null && selRowsRules.length > 0) {
                btnRuleUp.setEnabled(selRowsRules[0] > 0);
                btnRuleUp.setEnabled(selRowsRules[0] < tableRules.getRowCount() - 1);
            } else {
                btnRuleUp.setEnabled(false);
                btnRuleDown.setEnabled(false);
            }
        }
   }

    private void updateServerPanelButtonStates() {
        DefaultMutableTreeNode node = getSelected(treeOverview);
        if (node instanceof ConnectionTreeNode) {
            ConnectionTreeNode conNode = (ConnectionTreeNode)node;
            ConnectionInfo con = conNode.getConnection();
            if (con.getState() == ConnectionState.Connected && con.getType() != ConnectionInfo.TYPE_XMLA) {
                int selRowsDbs[] = treeDatabases.getSelectionRows();
                boolean enable = (selRowsDbs != null) && treeDatabases.isEditing() == false;
                btnDatabaseNew.setEnabled(!treeDatabases.isEditing());
                btnDatabaseDelete.setEnabled(enable);
            } else {
                btnDatabaseNew.setEnabled(false);
                btnDatabaseDelete.setEnabled(false);
            }
        }
    }

    private void updateGroupsPanelButtonStates() {
        if (isReadOnly()) {
           btnGroupNew.setEnabled(false);
           btnGroupEdit.setEnabled(false);
           btnGroupDelete.setEnabled(false);
        } else {
            DefaultMutableTreeNode node = getSelected(treeOverview);
            if (node instanceof GroupsTreeNode) {
                int selRowsDbs[] = treeGroups.getSelectionRows();
                boolean enable = (selRowsDbs != null) && treeGroups.isEditing() == false;
                btnGroupNew.setEnabled(!treeGroups.isEditing());
                btnGroupEdit.setEnabled(enable);
                btnGroupDelete.setEnabled(enable);
            }
        }
    }

    private void updateUsersPanelButtonStates() {
        if (isReadOnly()) {
           btnUserNew.setEnabled(false);
           btnUserEdit.setEnabled(false);
           btnUserDelete.setEnabled(false);
        } else {
            DefaultMutableTreeNode node = getSelected(treeOverview);
            if (node instanceof UsersTreeNode) {
                int selRowsDbs[] = treeUsers.getSelectionRows();
                boolean enable = (selRowsDbs != null) && treeUsers.isEditing() == false;
                btnUserNew.setEnabled(!treeUsers.isEditing());
                btnUserEdit.setEnabled(enable);
                btnUserDelete.setEnabled(enable);
            }
        }
    }

    private void updateRolesPanelButtonStates() {
        if (isReadOnly()) {
           btnRoleNew.setEnabled(false);
           btnRoleEdit.setEnabled(false);
           btnRoleDelete.setEnabled(false);
        } else {
            DefaultMutableTreeNode node = getSelected(treeOverview);
            if (node instanceof RolesTreeNode) {
                int selRowsDbs[] = treeRoles.getSelectionRows();
                boolean enable = (selRowsDbs != null) && treeRoles.isEditing() == false;
                btnRoleNew.setEnabled(!treeRoles.isEditing());
                btnRoleEdit.setEnabled(enable);
                btnRoleDelete.setEnabled(enable);
            }
        }
    }

    private void updateAttributesPanelButtonStates() {
        if (isReadOnly()) {
           btnAttributeNew.setEnabled(false);
           btnAttributeEdit.setEnabled(false);
           btnAttributeDelete.setEnabled(false);
           btnAttributeNumeric.setEnabled(false);
           btnAttributeString.setEnabled(false);
        } else {
            DefaultMutableTreeNode node = getSelected(treeOverview);
            if (node instanceof AttributesTreeNode) {
                int selRowsAttrs[] = treeAttributes.getSelectionRows();
                boolean enable = (selRowsAttrs != null) && treeAttributes.isEditing() == false;
                btnAttributeNew.setEnabled(!treeAttributes.isEditing());
                btnAttributeEdit.setEnabled(enable);
                btnAttributeDelete.setEnabled(enable);
                btnAttributeNumeric.setEnabled(enable);
                btnAttributeString.setEnabled(enable);

                if (selRowsAttrs != null) {
                    IElement.ElementType type = IElement.ElementType.ELEMENT_NUMERIC;
                    for (int i = 0; i < selRowsAttrs.length; i++) {
                        AttributeTreeNode attrNode = (AttributeTreeNode)attributesTreeModel.getChild(rootNodeAttributes, selRowsAttrs[i]);
                        if (i == 0)
                            type = attrNode.getAttribute().getType();
                        else {
                            if (!type.equals(attrNode.getAttribute().getType())) {
                                type = null;
                                break;
                            }
                        }
                    }
                    if (type.equals(IElement.ElementType.ELEMENT_NUMERIC))
                        btnAttributeNumeric.setSelected(true);
                    else if (type.equals(IElement.ElementType.ELEMENT_STRING))
                        btnAttributeString.setSelected(true);
                    else {
                        btnAttributeString.setSelected(false);
                        btnAttributeNumeric.setSelected(false);
    //                    buttonGroupAttributeType.clearSelection();
                        type = null;
                    }
                    btnAttributeNumeric.setEnabled(type != null);
                    btnAttributeString.setEnabled(type != null);
                } else {
                    btnAttributeNumeric.setEnabled(false);
                    btnAttributeString.setEnabled(false);
                }
            }
        }
    }

    private void updateDimensionPanelButtonStates() {
        if (isReadOnly()) {
            btnElementRename.setEnabled(false);
            mnuElementRename.setEnabled(false);
            btnElementConsolidate.setEnabled(false);
            mnuElementConsolidate.setEnabled(false);
            btnElementNew.setEnabled(false);
            mnuElementAdd.setEnabled(false);
            btnElementDelete.setEnabled(false);
            mnuElementDelete.setEnabled(false);
            btnElementNumeric.setEnabled(false);
            mnuElementNumeric.setEnabled(false);
            btnElementString.setEnabled(false);
            mnuElementString.setEnabled(false);
            mnuElementMoveFirst.setEnabled(false);
            mnuElementMoveLast.setEnabled(false);
            mnuElementMoveFirst.setEnabled(false);
            mnuElementMoveLast.setEnabled(false);
            btnHierarchy.setEnabled(true);
            btnMoveDown.setEnabled(false);
            btnMoveLeft.setEnabled(false);
            btnMoveUp.setEnabled(false);
            btnMoveRight.setEnabled(false);
        } else {
            btnConsolidationCancel.setEnabled(editConsolidationMode);
            btnConsolidationOK.setEnabled(editConsolidationMode);

            int selRowsElems[] = treeElements.getSelectionRows();

            if (selRowsElems == null || editConsolidationMode == true) {
                btnElementRename.setEnabled(false);
                mnuElementRename.setEnabled(false);
                btnElementConsolidate.setEnabled(false);
                mnuElementConsolidate.setEnabled(false);
                btnElementNew.setEnabled(!editConsolidationMode);
                mnuElementAdd.setEnabled(!editConsolidationMode);
                btnElementDelete.setEnabled(false);
                mnuElementDelete.setEnabled(false);
                btnElementNumeric.setEnabled(false);
                mnuElementNumeric.setEnabled(false);
                btnElementString.setEnabled(false);
                mnuElementString.setEnabled(false);
                mnuElementMoveFirst.setEnabled(false);
                mnuElementMoveLast.setEnabled(false);
            } else {
                btnElementRename.setEnabled(selRowsElems.length == 1 && treeElements.isEditing() == false);
                mnuElementRename.setEnabled(selRowsElems.length == 1 && treeElements.isEditing() == false);
                btnElementConsolidate.setEnabled(selRowsElems.length == 1 && treeElements.isEditing() == false);
                mnuElementConsolidate.setEnabled(selRowsElems.length == 1 && treeElements.isEditing() == false);
                btnElementNew.setEnabled(treeElements.isEditing() == false);
                mnuElementAdd.setEnabled(treeElements.isEditing() == false);
                btnElementDelete.setEnabled(selRowsElems.length > 0 && treeElements.isEditing() == false);
                mnuElementDelete.setEnabled(selRowsElems.length > 0 && treeElements.isEditing() == false);
                if (selRowsElems != null) {
                    IElement.ElementType type = IElement.ElementType.ELEMENT_NUMERIC;
                    for (int i = 0; i < selRowsElems.length; i++) {
                        ElementTreeNode node = (ElementTreeNode)elementTreeModel.getChild(rootNodeElements, selRowsElems[i]);
                        if (i == 0)
                            type = node.getElement().getType();
                        else {
                            if (!type.equals(node.getElement().getType())) {
                                type = null;
                                break;
                            }
                        }
                    }
                    if (type != null && type.equals(IElement.ElementType.ELEMENT_NUMERIC)) {
                        btnElementNumeric.setSelected(true);
                        mnuElementNumeric.setSelected(true);
                        mnuElementString.setSelected(false);
                    } else if (type != null && type.equals(IElement.ElementType.ELEMENT_STRING)) {
                        btnElementString.setSelected(true);
                        mnuElementString.setSelected(true);
                        mnuElementNumeric.setSelected(false);
                    } else {
                        mnuElementString.setSelected(false);
                        mnuElementNumeric.setSelected(false);
                        btnElementNumeric.setSelected(false);
                        btnElementString.setSelected(false);
    //                    buttonGroupElementType.clearSelection();
                        type = null;
                    }
                    btnElementNumeric.setEnabled(type != null);
                    mnuElementNumeric.setEnabled(type != null);
                    btnElementString.setEnabled(type != null);
                    mnuElementString.setEnabled(type != null);
                } else {
                    btnElementNumeric.setEnabled(false);
                    mnuElementNumeric.setEnabled(false);
                    mnuElementNumeric.setSelected(false);
                    btnElementString.setEnabled(false);
                    mnuElementString.setEnabled(false);
                    mnuElementString.setSelected(false);
                }
            }

            if (editConsolidationMode) {
                int selRows[] = treeConsolidation.getSelectionRows();

                mnuElementMoveFirst.setEnabled(false);
                mnuElementMoveLast.setEnabled(false);
                btnHierarchy.setEnabled(false);

                if (selRows == null || selRows.length == 0) {
                    btnMoveDown.setEnabled(false);
                    btnMoveLeft.setEnabled(false);
                    btnMoveUp.setEnabled(false);
                } else {
                    if (selRows[0] == 0) {
                        btnMoveUp.setEnabled(false);
                    } else {
                        btnMoveUp.setEnabled(true);
                    }
                    if (selRows[selRows.length - 1] == consolidatedElementTreeModel.getChildCount(rootNodeConsolidatedElements) - 1) {
                        btnMoveDown.setEnabled(false);
                    } else {
                        btnMoveDown.setEnabled(true);
                    }

                    btnMoveLeft.setEnabled(true);
                }
                if (selRowsElems == null || selRowsElems.length == 0) {
                    btnMoveRight.setEnabled(false);
                } else {
                    btnMoveRight.setEnabled(true);
                }
            } else {
                btnHierarchy.setEnabled(true);
                btnMoveLeft.setEnabled(false);
                if (selRowsElems == null || selRowsElems.length == 0) {
                    btnMoveDown.setEnabled(false);
                    btnMoveUp.setEnabled(false);
                    btnMoveRight.setEnabled(false);
                    mnuElementMoveFirst.setEnabled(false);
                    mnuElementMoveLast.setEnabled(false);
                } else {

                    btnMoveRight.setEnabled(false);

                    if (selRowsElems[0] == 0) {
                        btnMoveUp.setEnabled(false);
                        mnuElementMoveFirst.setEnabled(false);
                    } else {
                        btnMoveUp.setEnabled(true);
                        mnuElementMoveFirst.setEnabled(true);
                    }
                    if (selRowsElems[selRowsElems.length - 1] == elementTreeModel.getChildCount(rootNodeElements) - 1) {
                        btnMoveDown.setEnabled(false);
                        mnuElementMoveLast.setEnabled(false);
                    } else {
                        btnMoveDown.setEnabled(true);
                        mnuElementMoveLast.setEnabled(true);
                    }
                }
            }
        }
    }

    private void updateConnectionButtons() {
        DefaultMutableTreeNode node = getSelected(treeOverview);

        if (node instanceof ConnectionTreeNode) {
            ConnectionTreeNode conNode = (ConnectionTreeNode)node;
            if (conNode.getConnection().getState() == ConnectionState.Connected) {
                btnConnect.setEnabled(false);
                btnDisconnect.setEnabled(true);
            } else {
                btnConnect.setEnabled(true);
                btnDisconnect.setEnabled(false);
            }
            btnConnectionDelete.setEnabled(true);
            btnConnectionEdit.setEnabled(true);
            btnConnectionNew.setEnabled(true);
        } else {
            btnConnect.setEnabled(false);
            btnConnectionDelete.setEnabled(false);
            btnConnectionEdit.setEnabled(false);
            btnConnectionNew.setEnabled(node == null);
            btnDisconnect.setEnabled(false);
        }
    }

    private void showCubePanel(CubeTreeNode node) {
        jSplitPane1.setRightComponent(panelCube);

        ICube cube = node.getCube();

        lblCellCount.setText(cube.getNumberOfCells().toString());
        lblFilledCellCount.setText(cube.getNumberOfFilledCells().toString());


        lblStatus.setText(resourceBundle.getString("CubeStatusLoaded"));
        btnDeleteCubeValues.setEnabled(!isReadOnly());
        /* TODO

        switch (info.getStatus()) {
            case CubeInfo.STATUS_UNLOADED:
                lblStatus.setText(resourceBundle.getString("CubeStatusUnloaded"));
                break;
            case CubeInfo.STATUS_LOADED:
                lblStatus.setText(resourceBundle.getString("CubeStatusLoaded"));
                break;
            case CubeInfo.STATUS_CHANGED:
                lblStatus.setText(resourceBundle.getString("CubeStatusChanged"));
                break;
        }
         *
         */

        String  str = "";
        IDimension[]  dims = cube.getDimensions();

        for (int i = 0; i < dims.length; i++) {
            IDimension dim = dims[i];
            if (dim != null) {
                str += dim.getName();
                if (i < dims.length - 1)
                    str += ", ";
            }
        }
        txtAreaDimensions.setText(str);
    }

    private ConnectionTreeNode getConnectionNode(DefaultMutableTreeNode node) {
        while (!(node instanceof ConnectionTreeNode) && node != null) {
            node = (DefaultMutableTreeNode)node.getParent();
            if (node instanceof ConnectionTreeNode) {
                return (ConnectionTreeNode) node;
            }
        }
        return null;
    }

    private void showCubeRightsPanel(CubeRightsTreeNode node) {

        jSplitPane1.setRightComponent(panelCubeRights);

        rootNodeGroups.removeAllChildren();

        ConnectionTreeNode conNode = getConnectionNode(node);
        IDatabase   db =  conNode.getConnection().getConnection().getDatabaseByName("System");
        if (db == null)
            return;
        PaloAdministration  admin = new PaloAdministration(db);

        cubeRightTableListener.setCube(node.getCube());
        cubeRightTableListener.setDatabase(node.getDatabase());

        PaloGroup[] groups = admin.getGroups();
        if (groups == null)
            return;

        DefaultTableModel model = (DefaultTableModel)tableCubeRights.getModel();
        model.setRowCount(0);

        for (int i = 0; i < groups.length; i++) {
            Object row[] = new Object[] {
                    (Object)groups[i].getName(),
                    (Object)groups[i].getCubeRight(node.getDatabase(),node.getCube())};
            model.addRow(row);
        }
    }

    private class CubeRightTableListener implements TableModelListener {

        private ICube cube = null;
        private IDatabase database = null;

        public CubeRightTableListener() {
        }

        public void setCube(ICube cube) {
            this.cube = cube;
        }

        public void setDatabase(IDatabase database) {
            this.database = database;
        }

        public void tableChanged(TableModelEvent e) {
            if (e.getType() != e.UPDATE)
                return;

            if (cube == null)
                return;

            int row = e.getFirstRow();
            TableModel model = (TableModel)e.getSource();
            String group = (String) model.getValueAt(row, 0);
            String value = (String) model.getValueAt(row, 1);
            String data;

            data = value.toString();

            PaloGroup.setCubeRight(database,cube, group, data);
        }
    }

    private void showRulesPanel(CubeRulesTreeNode node) {
        jSplitPane1.setRightComponent(panelRules);

        ICube cube = node.getCube();
        IRule  rules[] = cube.getRules();

        DefaultTableModel model = (DefaultTableModel)tableRules.getModel();
        model.setRowCount(0);

        for (int i = 0; i < rules.length; i++) {
            Date lastChange = new Date(rules[i].getTimestamp());

            Object row[] = new Object[] {
                    (Object)rules[i].getDefinition(),
                    (Object)rules[i].getComment(),
                    (Object)DateFormat.getInstance().format(lastChange)};
            model.addRow(row);
        }

        txtRule.setText("");
        txtRuleComment.setText("");

        updateRulesPanelButtonStates();
    }

    private void showGroupsPanel()
    {
        jSplitPane1.setRightComponent(panelGroups);

        rootNodeGroups.removeAllChildren();

        TreePath path = treeOverview.getSelectionPath();
        if (path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
            if (node instanceof GroupsTreeNode) {
                GroupsTreeNode  groupsNode = (GroupsTreeNode)node;
                PaloAdministration  admin = new PaloAdministration(groupsNode.getSystemDatabase());

                PaloGroup[] groups = admin.getGroups();
                if (groups == null)
                    return;

                for (int i = 0; i < groups.length; i++) {
                    GroupTreeNode groupNode = new GroupTreeNode(groups[i],admin.getDimensionByName(PaloAdministration.DIMENSION_GROUP));
                    rootNodeGroups.add(groupNode);
                }
            }
        }

        groupTreeModel.reload();

        updateGroupsPanelButtonStates();
    }

    private void showGroupPanel()
    {
        jSplitPane1.setRightComponent(panelGroup);

        TreePath path = treeOverview.getSelectionPath();
        if (path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
            if (node instanceof GroupTreeNode) {
                PaloGroup  group = ((GroupTreeNode)node).getGroup();
                PaloAdministration  admin = new PaloAdministration(group.getSystemDatabase());

                groupRoleTableListener.setGroup(group);

                PaloRole[] roles = admin.getRoles();
                if (roles == null)
                    return;

                DefaultTableModel model = (DefaultTableModel)tableRoles.getModel();
                model.setRowCount(0);

                for (int i = 0; i < roles.length; i++) {
                    Object row[] = new Object[] {
                            (Object)roles[i].getName(),
                            (Object)new Boolean(group.hasRole(roles[i].getName()))};
                    model.addRow(row);
                }

            }
        }
    }

    private class GroupRoleTableListener implements TableModelListener {

        private PaloGroup   group = null;

        public GroupRoleTableListener() {
        }

        public void setGroup(PaloGroup group) {
            this.group = group;
        }

        public void tableChanged(TableModelEvent e) {
            if (e.getType() != e.UPDATE)
                return;

            if (group == null)
                return;

            try {
                int row = e.getFirstRow();
                TableModel model = (TableModel)e.getSource();
                String role = (String) model.getValueAt(row, 0);
                Boolean value = (Boolean) model.getValueAt(row, 1);
                String data;

                if (value.booleanValue() == true)
                    data = "1";
                else
                    data = "";

                group.setRole(role, data);
            } catch (PaloJException pe) {
                JOptionPane.showMessageDialog(null, pe.getMessage(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
            } catch (PaloException pe) {
                JOptionPane.showMessageDialog(null, pe.getDescription() + ", " + pe.getReason(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showUsersPanel()
    {
        jSplitPane1.setRightComponent(panelUsers);

        rootNodeUsers.removeAllChildren();

        TreePath path = treeOverview.getSelectionPath();
        if (path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
            if (node instanceof UsersTreeNode) {
                UsersTreeNode  usersNode = (UsersTreeNode)node;
                PaloAdministration  admin = new PaloAdministration(usersNode.getSystemDatabase());

                PaloUser[] users = admin.getUsers();
                if (users == null)
                    return;

                for (int i = 0; i < users.length; i++) {
                    UserTreeNode userNode = new UserTreeNode(users[i],admin.getDimensionByName(PaloAdministration.DIMENSION_USER));
                    rootNodeUsers.add(userNode);
                }
            }
        }

        userTreeModel.reload();

        updateUsersPanelButtonStates();
    }

    private void showUserPanel()
    {
        jSplitPane1.setRightComponent(panelUser);

        TreePath path = treeOverview.getSelectionPath();
        if (path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
            if (node instanceof UserTreeNode) {
                PaloUser  user = ((UserTreeNode)node).getUser();
                PaloAdministration  admin = new PaloAdministration(user.getSystemDatabase());

                userGroupTableListener.setUser(user);

                PaloGroup[] groups = admin.getGroups();
                if (groups == null)
                    return;

                DefaultTableModel model = (DefaultTableModel)tableGroups.getModel();
                model.setRowCount(0);

                for (int i = 0; i < groups.length; i++) {
                    Object row[] = new Object[] {
                            (Object)groups[i].getName(),
                            (Object)new Boolean(user.isMemberOfGroup(groups[i].getName()))};
                    model.addRow(row);
                }
            }
        }
    }

    private class UserGroupTableListener implements TableModelListener {

        private PaloUser   user = null;

        public UserGroupTableListener() {
        }

        public void setUser(PaloUser user) {
            this.user = user;
        }

        public void tableChanged(TableModelEvent e) {
            if (e.getType() != e.UPDATE)
                return;

            if (user == null)
                return;

            try {
                int row = e.getFirstRow();
                TableModel model = (TableModel)e.getSource();
                String group = (String) model.getValueAt(row, 0);
                Boolean value = (Boolean) model.getValueAt(row, 1);
                String data;

                if (value.booleanValue() == true)
                    data = "1";
                else
                    data = "";

                user.setGroup(group, data);
            } catch (PaloJException pe) {
                JOptionPane.showMessageDialog(null, pe.getMessage(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
            } catch (PaloException pe) {
                JOptionPane.showMessageDialog(null, pe.getDescription() + ", " + pe.getReason(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showRolesPanel()
    {
        jSplitPane1.setRightComponent(panelRoles);

        rootNodeRoles.removeAllChildren();

        TreePath path = treeOverview.getSelectionPath();
        if (path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
            if (node instanceof RolesTreeNode) {
                RolesTreeNode  rolesNode = (RolesTreeNode)node;
                PaloAdministration  admin = new PaloAdministration(rolesNode.getSystemDatabase());

                PaloRole[] roles = admin.getRoles();
                if (roles == null)
                    return;

                for (int i = 0; i < roles.length; i++) {
                    RoleTreeNode roleNode = new RoleTreeNode(roles[i],admin.getDimensionByName(PaloAdministration.DIMENSION_ROLE));
                    rootNodeRoles.add(roleNode);
                }
            }
        }

        roleTreeModel.reload();

        updateRolesPanelButtonStates();
    }

    private void showRolePanel()
    {
        jSplitPane1.setRightComponent(panelRole);

        TreePath path = treeOverview.getSelectionPath();
        if (path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
            if (node instanceof RoleTreeNode) {
                PaloRole  role = ((RoleTreeNode)node).getRole();
                PaloAdministration  admin = new PaloAdministration(role.getSystemDatabase());

                if ("admin".equals(role.getName())) {
                    cmbRights.setEnabled(false);
                }
                else {
                    cmbRights.setEnabled(true);
                }

                roleRightTableListener.setRole(role);

                String[] rights = admin.getRights();
                if (rights == null)
                    return;

                DefaultTableModel model = (DefaultTableModel)tableRights.getModel();
                model.setRowCount(0);

                for (int i = 0; i < rights.length; i++) {
                    Object row[] = new Object[] {
                            (Object)rights[i],
                            (Object)role.getRightAssignment(rights[i])};
                    model.addRow(row);
                }
            }
        }
    }

    private class RoleRightTableListener implements TableModelListener {

        private PaloRole   role = null;

        public RoleRightTableListener() {
        }

        public void setRole(PaloRole role) {
            this.role = role;
        }

        public void tableChanged(TableModelEvent e) {
            if (e.getType() != e.UPDATE)
                return;

            if (role == null)
                return;

            try {
                if (cmbRights.isEnabled()) {
                    int row = e.getFirstRow();
                    TableModel model = (TableModel)e.getSource();
                    String right = (String) model.getValueAt(row, 0);
                    String value = (String) model.getValueAt(row, 1);
                    String data;

                    data = value.toString();

                    role.setRightAssignment(right, data);
                }
            } catch (PaloJException pe) {
                JOptionPane.showMessageDialog(Modeller.this, pe.getMessage(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
                int     pos = jSplitPane1.getDividerLocation();
                showRolePanel();
                jSplitPane1.setDividerLocation(pos);
            } catch (PaloException pe) {
                JOptionPane.showMessageDialog(Modeller.this, pe.getDescription() + ", " + pe.getReason(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showAttributesPanel()
    {
        jSplitPane1.setRightComponent(panelAttributes);

        rootNodeAttributes.removeAllChildren();

        TreePath path = treeOverview.getSelectionPath();
        if (path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
            if (node instanceof AttributesTreeNode) {
                AttributesTreeNode  attributesNode = (AttributesTreeNode)node;
                rootNodeAttributes.setDimension(attributesNode.getDimension());

                IAttribute[] attributes = attributesNode.getDimension().getAttributes();
                if (attributes == null)
                    return;

                for (int i = 0; i < attributes.length; i++) {
                    AttributeTreeNode attributeNode = new AttributeTreeNode(attributes[i],attributesNode.getDimension());
                    rootNodeAttributes.add(attributeNode);

                }
            }
        }

        attributesTreeModel.reload();

        updateAttributesPanelButtonStates();
    }

    private void showAttributePanel()
    {
        jSplitPane1.setRightComponent(panelAttribute);
        if (isReadOnly()) {
            btnEditAttributes.setEnabled(false);
        } else {
            btnEditAttributes.setEnabled(true);
        }
    }

    private void showDimensionRightsPanel()
    {
        jSplitPane1.setRightComponent(panelDimensionRights);
        btnEditDimensionRights.setEnabled(!isReadOnly());
    }

    private void showDimensionPanelFlat() {
        int     pos = jSplitPane1.getDividerLocation();

        jSplitPane1.setRightComponent(panelDimension);

        TreePath path = treeOverview.getSelectionPath();
        if (path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
            if (node instanceof DimensionTreeNode) {
                fillElementList(((DimensionTreeNode)node).getDimension());
            } else if (node instanceof AttributesTreeNode) {
                fillElementList(getAttributeDimension((AttributesTreeNode)node));
            }
            rootNodeConsolidatedElements.removeAllChildren();
            updateDimensionPanelButtonStates();
        }

        jSplitPane1.setDividerLocation(pos);
    }

    private void showDimensionPanelFlatHierarchy() {
        int     pos = jSplitPane1.getDividerLocation();

        jSplitPane1.setRightComponent(panelDimensionHierarchy);

        TreePath path = treeOverview.getSelectionPath();
        if (path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
            if (node instanceof DimensionTreeNode) {
                fillElementHierarchy(((DimensionTreeNode) node).getDimension());
            } else if (node instanceof AttributesTreeNode) {
                fillElementHierarchy(getAttributeDimension((AttributesTreeNode) node));
            }
        }

        jSplitPane1.setDividerLocation(pos);
        btnWeight.setEnabled(!isReadOnly());
    }

    private void setElementType(IElement.ElementType type) {
        int selRowsElems[] = treeElements.getSelectionRows();

        try  {
            if (selRowsElems != null) {
                for (int i = 0; i < selRowsElems.length; i++) {
                    ElementTreeNode node = (ElementTreeNode)elementTreeModel.getChild(rootNodeElements, selRowsElems[i]);
                    node.getDimension().updateElementsType(new IElement[]{node.getElement()}, type);
                }
                treeElements.repaint();
            }
        } catch (PaloJException pe) {
            JOptionPane.showMessageDialog(this, pe.getMessage(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
        } catch (PaloException pe) {
            JOptionPane.showMessageDialog(this, pe.getDescription() + ", " + pe.getReason(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setAttributeType(IElement.ElementType type) {
        int selRowsAttrs[] = treeAttributes.getSelectionRows();

        try {
            if (selRowsAttrs != null) {
                for (int i = 0; i < selRowsAttrs.length; i++) {
                    AttributeTreeNode node = (AttributeTreeNode)attributesTreeModel.getChild(rootNodeAttributes, selRowsAttrs[i]);
                    //node.getAttribute().setType(type); //TODO implement API call?
                }
                treeAttributes.repaint();
            }
        } catch (PaloJException pe) {
            JOptionPane.showMessageDialog(this, pe.getMessage(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
        } catch (PaloException pe) {
            JOptionPane.showMessageDialog(this, pe.getDescription() + ", " + pe.getReason(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private DatabaseTreeNode getDBNode(DefaultMutableTreeNode node) {
        if (node instanceof DimensionsTreeNode || node instanceof CubesTreeNode) {
            return (DatabaseTreeNode)node.getParent();
        } else if (node instanceof DatabaseTreeNode) {
            return (DatabaseTreeNode)node;
        } else {
            return null;
        }
    }

    private void moveRule(boolean up) {

        DefaultTableModel model = (DefaultTableModel)tableRules.getModel();
        int selRow = tableRules.getSelectedRow();
        if (selRow == -1)
            return;

        DefaultMutableTreeNode node = getSelected(treeOverview);
        if (node instanceof CubeRulesTreeNode) {
            CubeRulesTreeNode cubeNode = (CubeRulesTreeNode)node;
            CubeRulesTreeNode.RuleProxy ruleSrc = cubeNode.getRules().remove(selRow);
            if (up) {
                cubeNode.getRules().add(selRow-1,ruleSrc);
            } else {
                cubeNode.getRules().add(selRow+1, ruleSrc);
            }
            

              //TODO check for solution to move rules
            /*
            IRule ruleDest;
            if (up) {
                ruleDest = cubeNode.getCube().getRules()[selRow - 1];
            } else {
                ruleDest = cubeNode.getCube().getRules()[selRow + 1];
            }


            String r1DefSrc = ruleSrc.getDefinition();
            String r1EISrc  = ruleSrc.getExternalIdentifier();
            String r1CoSrc  = ruleSrc.getComment();
            boolean r1AcSrc  = ruleSrc.isActive();

            String r1DefDest = ruleDest.getDefinition();
            String r1EIDest  = ruleDest.getExternalIdentifier();
            String r1CoDest  = ruleDest.getComment();
            boolean r1AcDest  = ruleDest.isActive();

          
            ruleDest.setDefinition(r1DefSrc);
            ruleDest.setComment(r1CoSrc);
            ruleDest.setExternalIdentifier(r1EISrc);
            ruleDest.setActive(r1AcSrc);

            ruleSrc.setDefinition(r1DefDest);
            ruleSrc.setComment(r1CoDest);
            ruleSrc.setExternalIdentifier(r1EIDest);
            ruleSrc.setActive(r1AcDest);
             *
             */
            if (up) {
                model.moveRow(selRow, selRow, selRow - 1);
                tableRules.setRowSelectionInterval(selRow - 1, selRow - 1);
            } else {
                model.moveRow(selRow, selRow, selRow + 1);
                tableRules.setRowSelectionInterval(selRow + 1, selRow + 1);
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroupElementType = new javax.swing.ButtonGroup();
        buttonGroupAttributeType = new javax.swing.ButtonGroup();
        pmElementsFlat = new javax.swing.JPopupMenu();
        mnuElementAdd = new javax.swing.JMenuItem();
        mnuElementDelete = new javax.swing.JMenuItem();
        mnuElementRename = new javax.swing.JMenuItem();
        mnuElementConsolidate = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        mnuElementNumeric = new javax.swing.JCheckBoxMenuItem();
        mnuElementString = new javax.swing.JCheckBoxMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        mnuElementSelectAll = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JSeparator();
        mnuElementMoveFirst = new javax.swing.JMenuItem();
        mnuElementMoveLast = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JSeparator();
        mnuElementCount = new javax.swing.JMenuItem();
        mnuElementSearch = new javax.swing.JMenuItem();
        jToolBar1 = new javax.swing.JToolBar();
        btnConnectionNew = new javax.swing.JButton();
        btnConnectionDelete = new javax.swing.JButton();
        btnConnectionEdit = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        btnConnect = new javax.swing.JButton();
        btnDisconnect = new javax.swing.JButton();
        btnOK = new javax.swing.JButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        treeOverview = new javax.swing.JTree();
        panelAttribute = new javax.swing.JPanel();
        btnEditAttributes = new javax.swing.JButton();
        jLabel37 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        panelDatabase = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        btnCreateDimension = new javax.swing.JButton();
        btnDeleteDimension = new javax.swing.JButton();
        btnRenameDimension = new javax.swing.JButton();
        btnCreateCube = new javax.swing.JButton();
        btnDeleteCube = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        treeCubes = new javax.swing.JTree();
        jScrollPane4 = new javax.swing.JScrollPane();
        treeDimensions = new javax.swing.JTree();
        btnRenameCube = new javax.swing.JButton();
        panelDimension = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        treeElements = new javax.swing.JTree();
        btnSortDown = new javax.swing.JToggleButton();
        btnSortUp = new javax.swing.JToggleButton();
        btnNoSort = new javax.swing.JToggleButton();
        jScrollPane5 = new javax.swing.JScrollPane();
        treeConsolidation = new javax.swing.JTree();
        jLabel12 = new javax.swing.JLabel();
        btnMoveUp = new javax.swing.JButton();
        btnMoveRight = new javax.swing.JButton();
        btnMoveLeft = new javax.swing.JButton();
        btnMoveDown = new javax.swing.JButton();
        btnElementNew = new javax.swing.JButton();
        btnElementDelete = new javax.swing.JButton();
        btnElementRename = new javax.swing.JButton();
        btnHierarchy = new javax.swing.JButton();
        btnConsolidationCancel = new javax.swing.JButton();
        btnConsolidationOK = new javax.swing.JButton();
        btnElementConsolidate = new javax.swing.JButton();
        btnElementString = new javax.swing.JToggleButton();
        btnElementNumeric = new javax.swing.JToggleButton();
        panelDimensionHierarchy = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        treeHierarchy = new DnDJTree();
        btnFlat = new javax.swing.JButton();
        btnExpand = new javax.swing.JButton();
        btnCollapse = new javax.swing.JButton();
        btnCollapseAll = new javax.swing.JButton();
        btnExpandAll = new javax.swing.JButton();
        btnLevel1 = new javax.swing.JButton();
        btnLevel2 = new javax.swing.JButton();
        btnLevel3 = new javax.swing.JButton();
        btnLevel4 = new javax.swing.JButton();
        btnLevel5 = new javax.swing.JButton();
        btnLevel6 = new javax.swing.JButton();
        btnLevel7 = new javax.swing.JButton();
        btnLevel8 = new javax.swing.JButton();
        btnWeight = new javax.swing.JButton();
        panelRules = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jScrollPane10 = new javax.swing.JScrollPane();
        txtRule = new javax.swing.JTextArea();
        btnNewRule = new javax.swing.JButton();
        btnDeleteRule = new javax.swing.JButton();
        btnUpdateRule = new javax.swing.JButton();
        btnRuleUp = new javax.swing.JButton();
        btnRuleDown = new javax.swing.JButton();
        jLabel15 = new javax.swing.JLabel();
        jScrollPane11 = new javax.swing.JScrollPane();
        txtRuleComment = new javax.swing.JTextArea();
        txtError = new javax.swing.JLabel();
        btnCheckRule = new javax.swing.JButton();
        jScrollPane6 = new javax.swing.JScrollPane();
        tableRules = new javax.swing.JTable();
        panelServer = new javax.swing.JPanel();
        jScrollPane8 = new javax.swing.JScrollPane();
        treeDatabases = new javax.swing.JTree();
        jLabel2 = new javax.swing.JLabel();
        btnDatabaseNew = new javax.swing.JButton();
        btnDatabaseDelete = new javax.swing.JButton();
        panelGroups = new javax.swing.JPanel();
        jScrollPane12 = new javax.swing.JScrollPane();
        treeGroups = new javax.swing.JTree();
        jLabel16 = new javax.swing.JLabel();
        btnGroupNew = new javax.swing.JButton();
        btnGroupDelete = new javax.swing.JButton();
        btnGroupEdit = new javax.swing.JButton();
        panelUsers = new javax.swing.JPanel();
        jScrollPane13 = new javax.swing.JScrollPane();
        treeUsers = new javax.swing.JTree();
        jLabel17 = new javax.swing.JLabel();
        btnUserNew = new javax.swing.JButton();
        btnUserDelete = new javax.swing.JButton();
        btnUserEdit = new javax.swing.JButton();
        panelRoles = new javax.swing.JPanel();
        jScrollPane14 = new javax.swing.JScrollPane();
        treeRoles = new javax.swing.JTree();
        jLabel18 = new javax.swing.JLabel();
        btnRoleNew = new javax.swing.JButton();
        btnRoleDelete = new javax.swing.JButton();
        btnRoleEdit = new javax.swing.JButton();
        panelGroup = new javax.swing.JPanel();
        jScrollPane16 = new javax.swing.JScrollPane();
        tableRoles = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        panelUser = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane18 = new javax.swing.JScrollPane();
        tableGroups = new javax.swing.JTable();
        btnPassword = new javax.swing.JButton();
        panelRole = new javax.swing.JPanel();
        jScrollPane17 = new javax.swing.JScrollPane();
        tableRights = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        panelAttributes = new javax.swing.JPanel();
        jScrollPane15 = new javax.swing.JScrollPane();
        treeAttributes = new javax.swing.JTree();
        jLabel22 = new javax.swing.JLabel();
        btnAttributeNew = new javax.swing.JButton();
        btnAttributeDelete = new javax.swing.JButton();
        btnAttributeEdit = new javax.swing.JButton();
        btnAttributeNumeric = new javax.swing.JToggleButton();
        btnAttributeString = new javax.swing.JToggleButton();
        panelDimensionRights = new javax.swing.JPanel();
        btnEditDimensionRights = new javax.swing.JButton();
        jLabel34 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        panelCubeRights = new javax.swing.JPanel();
        jScrollPane19 = new javax.swing.JScrollPane();
        tableCubeRights = new javax.swing.JTable();
        jLabel10 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        panelCube = new javax.swing.JPanel();
        jLabel29 = new javax.swing.JLabel();
        lblFilledCellCount = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        lblCellCount = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        lblStatus = new javax.swing.JLabel();
        btnDeleteCubeValues = new javax.swing.JButton();
        jLabel35 = new javax.swing.JLabel();
        jScrollPane9 = new javax.swing.JScrollPane();
        txtAreaDimensions = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();

        pmElementsFlat.setName("pmElementsFlat"); // NOI18N

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs"); // NOI18N
        mnuElementAdd.setText(bundle.getString("MenuElementAdd")); // NOI18N
        mnuElementAdd.setName("mnuElementAdd"); // NOI18N
        mnuElementAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuElementAddActionPerformed(evt);
            }
        });
        pmElementsFlat.add(mnuElementAdd);

        mnuElementDelete.setText(bundle.getString("MenuDeleteElement")); // NOI18N
        mnuElementDelete.setName("mnuElementDelete"); // NOI18N
        mnuElementDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuElementDeleteActionPerformed(evt);
            }
        });
        pmElementsFlat.add(mnuElementDelete);

        mnuElementRename.setText(bundle.getString("MenuRenameElement")); // NOI18N
        mnuElementRename.setName("mnuElementRename"); // NOI18N
        mnuElementRename.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuElementRenameActionPerformed(evt);
            }
        });
        pmElementsFlat.add(mnuElementRename);

        mnuElementConsolidate.setText(bundle.getString("MenuConsolidateElement")); // NOI18N
        mnuElementConsolidate.setName("mnuElementConsolidate"); // NOI18N
        mnuElementConsolidate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuElementConsolidateActionPerformed(evt);
            }
        });
        pmElementsFlat.add(mnuElementConsolidate);

        jSeparator2.setName("jSeparator2"); // NOI18N
        pmElementsFlat.add(jSeparator2);

        mnuElementNumeric.setSelected(true);
        mnuElementNumeric.setText(bundle.getString("MenuElementNumeric")); // NOI18N
        mnuElementNumeric.setName("mnuElementNumeric"); // NOI18N
        mnuElementNumeric.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuElementNumericActionPerformed(evt);
            }
        });
        pmElementsFlat.add(mnuElementNumeric);

        mnuElementString.setSelected(true);
        mnuElementString.setText(bundle.getString("MenuElementString")); // NOI18N
        mnuElementString.setName("mnuElementString"); // NOI18N
        mnuElementString.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuElementStringActionPerformed(evt);
            }
        });
        pmElementsFlat.add(mnuElementString);

        jSeparator3.setName("jSeparator3"); // NOI18N
        pmElementsFlat.add(jSeparator3);

        mnuElementSelectAll.setText(bundle.getString("MenuSelectAll")); // NOI18N
        mnuElementSelectAll.setName("mnuElementSelectAll"); // NOI18N
        mnuElementSelectAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuElementSelectAllActionPerformed(evt);
            }
        });
        pmElementsFlat.add(mnuElementSelectAll);

        jSeparator4.setName("jSeparator4"); // NOI18N
        pmElementsFlat.add(jSeparator4);

        mnuElementMoveFirst.setText(bundle.getString("MenuElementMoveToFirst")); // NOI18N
        mnuElementMoveFirst.setName("mnuElementMoveFirst"); // NOI18N
        mnuElementMoveFirst.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuElementMoveFirstActionPerformed(evt);
            }
        });
        pmElementsFlat.add(mnuElementMoveFirst);

        mnuElementMoveLast.setText(bundle.getString("MenuElementLast")); // NOI18N
        mnuElementMoveLast.setName("mnuElementMoveLast"); // NOI18N
        mnuElementMoveLast.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuElementMoveLastActionPerformed(evt);
            }
        });
        pmElementsFlat.add(mnuElementMoveLast);

        jSeparator5.setName("jSeparator5"); // NOI18N
        pmElementsFlat.add(jSeparator5);

        mnuElementCount.setText(bundle.getString("MenuElementCount")); // NOI18N
        mnuElementCount.setName("mnuElementCount"); // NOI18N
        mnuElementCount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuElementCountActionPerformed(evt);
            }
        });
        pmElementsFlat.add(mnuElementCount);

        mnuElementSearch.setText(bundle.getString("MenuElementSearch")); // NOI18N
        mnuElementSearch.setName("mnuElementSearch"); // NOI18N
        mnuElementSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuElementSearchActionPerformed(evt);
            }
        });
        pmElementsFlat.add(mnuElementSearch);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(bundle.getString("ModelerCaption")); // NOI18N
        setAlwaysOnTop(true);
        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jToolBar1.setRollover(true);
        jToolBar1.setName("jToolBar1"); // NOI18N

        btnConnectionNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/serverNew.png"))); // NOI18N
        btnConnectionNew.setToolTipText(bundle.getString("NewConnection")); // NOI18N
        btnConnectionNew.setFocusable(false);
        btnConnectionNew.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnConnectionNew.setName("btnConnectionNew"); // NOI18N
        btnConnectionNew.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnConnectionNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConnectionNewActionPerformed(evt);
            }
        });
        jToolBar1.add(btnConnectionNew);

        btnConnectionDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/serverDelete.png"))); // NOI18N
        btnConnectionDelete.setToolTipText(bundle.getString("RemoveConnection")); // NOI18N
        btnConnectionDelete.setFocusable(false);
        btnConnectionDelete.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnConnectionDelete.setName("btnConnectionDelete"); // NOI18N
        btnConnectionDelete.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnConnectionDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConnectionDeleteActionPerformed(evt);
            }
        });
        jToolBar1.add(btnConnectionDelete);

        btnConnectionEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/serverEdit.png"))); // NOI18N
        btnConnectionEdit.setToolTipText(bundle.getString("EditConnection")); // NOI18N
        btnConnectionEdit.setFocusable(false);
        btnConnectionEdit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnConnectionEdit.setName("btnConnectionEdit"); // NOI18N
        btnConnectionEdit.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnConnectionEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConnectionEditActionPerformed(evt);
            }
        });
        jToolBar1.add(btnConnectionEdit);

        jSeparator1.setName("jSeparator1"); // NOI18N
        jSeparator1.setSeparatorSize(new java.awt.Dimension(5, 0));
        jToolBar1.add(jSeparator1);

        btnConnect.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/serverConnect.png"))); // NOI18N
        btnConnect.setToolTipText(bundle.getString("ConnectToServer")); // NOI18N
        btnConnect.setFocusable(false);
        btnConnect.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnConnect.setName("btnConnect"); // NOI18N
        btnConnect.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConnectActionPerformed(evt);
            }
        });
        jToolBar1.add(btnConnect);

        btnDisconnect.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/serverDisconnect.png"))); // NOI18N
        btnDisconnect.setToolTipText(bundle.getString("Disconnect Server")); // NOI18N
        btnDisconnect.setFocusable(false);
        btnDisconnect.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnDisconnect.setName("btnDisconnect"); // NOI18N
        btnDisconnect.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnDisconnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDisconnectActionPerformed(evt);
            }
        });
        jToolBar1.add(btnDisconnect);

        getContentPane().add(jToolBar1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, -1));

        btnOK.setText(bundle.getString("Close")); // NOI18N
        btnOK.setName("btnOK"); // NOI18N
        btnOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOKActionPerformed(evt);
            }
        });
        getContentPane().add(btnOK, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 470, 80, -1));

        jSplitPane1.setName("jSplitPane1"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        treeOverview.setModel(overviewTreeModel);
        treeOverview.setName("treeOverview"); // NOI18N
        treeOverview.setRootVisible(false);
        treeOverview.setShowsRootHandles(true);
        treeOverview.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                treeOverviewMouseClicked(evt);
            }
        });
        treeOverview.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                treeOverviewValueChanged(evt);
            }
        });
        treeOverview.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                treeOverviewKeyPressed(evt);
            }
        });
        jScrollPane1.setViewportView(treeOverview);

        jSplitPane1.setLeftComponent(jScrollPane1);

        panelAttribute.setName("panelAttribute"); // NOI18N
        panelAttribute.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnEditAttributes.setText(bundle.getString("EditAttributeTable")); // NOI18N
        btnEditAttributes.setName("btnEditAttributes"); // NOI18N
        btnEditAttributes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditAttributesActionPerformed(evt);
            }
        });
        panelAttribute.add(btnEditAttributes, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, 170, -1));

        jLabel37.setText(bundle.getString("EditDimRightsInfo2")); // NOI18N
        jLabel37.setName("jLabel37"); // NOI18N
        panelAttribute.add(jLabel37, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, -1, -1));

        jLabel38.setText(bundle.getString("EditAttributeValuesInfo")); // NOI18N
        jLabel38.setName("jLabel38"); // NOI18N
        panelAttribute.add(jLabel38, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 570, 20));

        jSplitPane1.setRightComponent(panelAttribute);

        panelDatabase.setName("panelDatabase"); // NOI18N
        panelDatabase.setPreferredSize(new java.awt.Dimension(550, 395));
        panelDatabase.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel8.setText(bundle.getString("Dimensions")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N
        panelDatabase.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, -1));

        jLabel9.setText(bundle.getString("Cubes")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N
        panelDatabase.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 10, -1, -1));

        btnCreateDimension.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/dimensionNew.png"))); // NOI18N
        btnCreateDimension.setToolTipText(bundle.getString("CreateDimension")); // NOI18N
        btnCreateDimension.setName("btnCreateDimension"); // NOI18N
        btnCreateDimension.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCreateDimensionActionPerformed(evt);
            }
        });
        panelDatabase.add(btnCreateDimension, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 370, 25, -1));

        btnDeleteDimension.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/dimensionDelete.png"))); // NOI18N
        btnDeleteDimension.setToolTipText(bundle.getString("Delete Dimension")); // NOI18N
        btnDeleteDimension.setName("btnDeleteDimension"); // NOI18N
        btnDeleteDimension.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteDimensionActionPerformed(evt);
            }
        });
        panelDatabase.add(btnDeleteDimension, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 370, 25, -1));

        btnRenameDimension.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/dimensionRename.png"))); // NOI18N
        btnRenameDimension.setToolTipText(bundle.getString("RenameDimension")); // NOI18N
        btnRenameDimension.setName("btnRenameDimension"); // NOI18N
        btnRenameDimension.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRenameDimensionActionPerformed(evt);
            }
        });
        panelDatabase.add(btnRenameDimension, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 370, 25, -1));

        btnCreateCube.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/cubeNew.png"))); // NOI18N
        btnCreateCube.setToolTipText(bundle.getString("CreateCube")); // NOI18N
        btnCreateCube.setName("btnCreateCube"); // NOI18N
        btnCreateCube.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCreateCubeActionPerformed(evt);
            }
        });
        panelDatabase.add(btnCreateCube, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 370, 25, -1));

        btnDeleteCube.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/cubeDelete.png"))); // NOI18N
        btnDeleteCube.setToolTipText(bundle.getString("DeleteCube")); // NOI18N
        btnDeleteCube.setName("btnDeleteCube"); // NOI18N
        btnDeleteCube.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteCubeActionPerformed(evt);
            }
        });
        panelDatabase.add(btnDeleteCube, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 370, 25, -1));

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        treeCubes.setModel(cubeTreeModel);
        treeCubes.setName("treeCubes"); // NOI18N
        treeCubes.setRootVisible(false);
        treeCubes.setShowsRootHandles(true);
        treeCubes.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                treeCubesValueChanged(evt);
            }
        });
        treeCubes.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                treeCubesFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                treeCubesFocusLost(evt);
            }
        });
        treeCubes.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                treeCubesKeyPressed(evt);
            }
        });
        jScrollPane3.setViewportView(treeCubes);

        panelDatabase.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 30, 250, 330));

        jScrollPane4.setName("jScrollPane4"); // NOI18N

        treeDimensions.setModel(dimensionTreeModel);
        treeDimensions.setEditable(true);
        treeDimensions.setName("treeDimensions"); // NOI18N
        treeDimensions.setRootVisible(false);
        treeDimensions.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                treeDimensionsValueChanged(evt);
            }
        });
        treeDimensions.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                treeDimensionsFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                treeDimensionsFocusLost(evt);
            }
        });
        treeDimensions.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                treeDimensionsKeyPressed(evt);
            }
        });
        jScrollPane4.setViewportView(treeDimensions);

        panelDatabase.add(jScrollPane4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 30, 230, 330));

        btnRenameCube.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/cubeRename.png"))); // NOI18N
        btnRenameCube.setToolTipText(bundle.getString("EditCube")); // NOI18N
        btnRenameCube.setName("btnRenameCube"); // NOI18N
        btnRenameCube.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRenameCubeActionPerformed(evt);
            }
        });
        panelDatabase.add(btnRenameCube, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 370, 25, -1));

        jSplitPane1.setRightComponent(panelDatabase);

        panelDimension.setName("panelDimension"); // NOI18N
        panelDimension.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel11.setText(bundle.getString("Elements")); // NOI18N
        jLabel11.setName("jLabel11"); // NOI18N
        panelDimension.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, -1, -1));

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        treeElements.setModel(elementTreeModel);
        treeElements.setComponentPopupMenu(pmElementsFlat);
        treeElements.setDragEnabled(true);
        treeElements.setEditable(true);
        treeElements.setName("treeElements"); // NOI18N
        treeElements.setRootVisible(false);
        treeElements.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                treeElementsMouseClicked(evt);
            }
        });
        treeElements.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                treeElementsValueChanged(evt);
            }
        });
        treeElements.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                treeElementsFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                treeElementsFocusLost(evt);
            }
        });
        treeElements.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                treeElementsKeyPressed(evt);
            }
        });
        jScrollPane2.setViewportView(treeElements);

        panelDimension.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 40, 235, -1));

        btnSortDown.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/sortDown.PNG"))); // NOI18N
        btnSortDown.setToolTipText(bundle.getString("SortUp")); // NOI18N
        btnSortDown.setName("btnSortDown"); // NOI18N
        btnSortDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSortDownActionPerformed(evt);
            }
        });
        panelDimension.add(btnSortDown, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 10, 25, -1));

        btnSortUp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/sortUp.PNG"))); // NOI18N
        btnSortUp.setToolTipText(bundle.getString("SortDown")); // NOI18N
        btnSortUp.setName("btnSortUp"); // NOI18N
        btnSortUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSortUpActionPerformed(evt);
            }
        });
        panelDimension.add(btnSortUp, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 10, 25, -1));

        btnNoSort.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/sortNone.PNG"))); // NOI18N
        btnNoSort.setToolTipText(bundle.getString("NoSort")); // NOI18N
        btnNoSort.setName("btnNoSort"); // NOI18N
        btnNoSort.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNoSortActionPerformed(evt);
            }
        });
        panelDimension.add(btnNoSort, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 10, 25, -1));

        jScrollPane5.setName("jScrollPane5"); // NOI18N

        treeConsolidation.setModel(consolidatedElementTreeModel);
        treeConsolidation.setName("treeConsolidation"); // NOI18N
        treeConsolidation.setRootVisible(false);
        treeConsolidation.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                treeConsolidationMouseClicked(evt);
            }
        });
        treeConsolidation.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                treeConsolidationFocusGained(evt);
            }
        });
        treeConsolidation.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                treeConsolidationKeyPressed(evt);
            }
        });
        jScrollPane5.setViewportView(treeConsolidation);

        panelDimension.add(jScrollPane5, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 40, 230, -1));

        jLabel12.setText(bundle.getString("ConsolidatedElements")); // NOI18N
        jLabel12.setName("jLabel12"); // NOI18N
        panelDimension.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 20, -1, -1));

        btnMoveUp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/MoveUp.png"))); // NOI18N
        btnMoveUp.setToolTipText(bundle.getString("MoveUp")); // NOI18N
        btnMoveUp.setName("btnMoveUp"); // NOI18N
        btnMoveUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMoveUpActionPerformed(evt);
            }
        });
        panelDimension.add(btnMoveUp, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 130, 25, -1));

        btnMoveRight.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/MoveNext.png"))); // NOI18N
        btnMoveRight.setToolTipText(bundle.getString("AddConsolidation")); // NOI18N
        btnMoveRight.setName("btnMoveRight"); // NOI18N
        btnMoveRight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMoveRightActionPerformed(evt);
            }
        });
        panelDimension.add(btnMoveRight, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 210, 25, -1));

        btnMoveLeft.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/MovePrevious.png"))); // NOI18N
        btnMoveLeft.setToolTipText(bundle.getString("RemoveConsolidation")); // NOI18N
        btnMoveLeft.setName("btnMoveLeft"); // NOI18N
        btnMoveLeft.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMoveLeftActionPerformed(evt);
            }
        });
        panelDimension.add(btnMoveLeft, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 240, 25, -1));

        btnMoveDown.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/MoveDown.PNG"))); // NOI18N
        btnMoveDown.setToolTipText(bundle.getString("MoveDown")); // NOI18N
        btnMoveDown.setName("btnMoveDown"); // NOI18N
        btnMoveDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMoveDownActionPerformed(evt);
            }
        });
        panelDimension.add(btnMoveDown, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 160, 25, -1));

        btnElementNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/ElementNew.PNG"))); // NOI18N
        btnElementNew.setToolTipText(bundle.getString("ElementNew")); // NOI18N
        btnElementNew.setName("btnElementNew"); // NOI18N
        btnElementNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnElementNewActionPerformed(evt);
            }
        });
        panelDimension.add(btnElementNew, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 370, 25, -1));

        btnElementDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/ElementDelete.PNG"))); // NOI18N
        btnElementDelete.setToolTipText(bundle.getString("ElementDelete")); // NOI18N
        btnElementDelete.setName("btnElementDelete"); // NOI18N
        btnElementDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnElementDeleteActionPerformed(evt);
            }
        });
        panelDimension.add(btnElementDelete, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 370, 25, -1));

        btnElementRename.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/ElementRename.PNG"))); // NOI18N
        btnElementRename.setToolTipText(bundle.getString("ElementRename")); // NOI18N
        btnElementRename.setName("btnElementRename"); // NOI18N
        btnElementRename.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnElementRenameActionPerformed(evt);
            }
        });
        panelDimension.add(btnElementRename, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 370, 25, -1));

        btnHierarchy.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/showFlat.png"))); // NOI18N
        btnHierarchy.setToolTipText(bundle.getString("HierarchicalMode")); // NOI18N
        btnHierarchy.setName("btnHierarchy"); // NOI18N
        btnHierarchy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHierarchyActionPerformed(evt);
            }
        });
        panelDimension.add(btnHierarchy, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 10, 25, -1));

        btnConsolidationCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/cancel.PNG"))); // NOI18N
        btnConsolidationCancel.setToolTipText(bundle.getString("CancelConsolidation")); // NOI18N
        btnConsolidationCancel.setName("btnConsolidationCancel"); // NOI18N
        btnConsolidationCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConsolidationCancelActionPerformed(evt);
            }
        });
        panelDimension.add(btnConsolidationCancel, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 370, 25, -1));

        btnConsolidationOK.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/ok.PNG"))); // NOI18N
        btnConsolidationOK.setToolTipText(bundle.getString("SetConsolidation")); // NOI18N
        btnConsolidationOK.setName("btnConsolidationOK"); // NOI18N
        btnConsolidationOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConsolidationOKActionPerformed(evt);
            }
        });
        panelDimension.add(btnConsolidationOK, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 370, 25, -1));

        btnElementConsolidate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/ElementConsolidated.png"))); // NOI18N
        btnElementConsolidate.setToolTipText(bundle.getString("ConsolidationEdit")); // NOI18N
        btnElementConsolidate.setName("btnElementConsolidate"); // NOI18N
        btnElementConsolidate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnElementConsolidateActionPerformed(evt);
            }
        });
        panelDimension.add(btnElementConsolidate, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 370, 25, -1));

        btnElementString.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/string.png"))); // NOI18N
        btnElementString.setToolTipText(bundle.getString("ElementString")); // NOI18N
        btnElementString.setName("btnElementString"); // NOI18N
        btnElementString.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnElementStringActionPerformed(evt);
            }
        });
        panelDimension.add(btnElementString, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 370, 25, -1));

        btnElementNumeric.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/numeric.png"))); // NOI18N
        btnElementNumeric.setToolTipText(bundle.getString("ElementNumerci")); // NOI18N
        btnElementNumeric.setName("btnElementNumeric"); // NOI18N
        btnElementNumeric.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnElementNumericActionPerformed(evt);
            }
        });
        panelDimension.add(btnElementNumeric, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 370, 25, -1));

        jSplitPane1.setRightComponent(panelDimension);

        panelDimensionHierarchy.setMinimumSize(new java.awt.Dimension(0, 0));
        panelDimensionHierarchy.setName("panelDimensionHierarchy"); // NOI18N
        panelDimensionHierarchy.setPreferredSize(new java.awt.Dimension(550, 395));
        panelDimensionHierarchy.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jScrollPane7.setName("jScrollPane7"); // NOI18N

        treeHierarchy.setModel(elementHierarchyTreeModel);
        treeHierarchy.setName("treeHierarchy"); // NOI18N
        treeHierarchy.setRootVisible(false);
        treeHierarchy.setShowsRootHandles(true);
        treeHierarchy.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                treeHierarchyMouseClicked(evt);
            }
        });
        treeHierarchy.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                treeHierarchyValueChanged(evt);
            }
        });
        jScrollPane7.setViewportView(treeHierarchy);

        panelDimensionHierarchy.add(jScrollPane7, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 42, 480, 350));

        btnFlat.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/showHierarchy.png"))); // NOI18N
        btnFlat.setToolTipText(bundle.getString("ShowElementList")); // NOI18N
        btnFlat.setName("btnFlat"); // NOI18N
        btnFlat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFlatActionPerformed(evt);
            }
        });
        panelDimensionHierarchy.add(btnFlat, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 10, 30, -1));

        btnExpand.setText("+");
        btnExpand.setToolTipText(bundle.getString("ExpandTooltip")); // NOI18N
        btnExpand.setMargin(new java.awt.Insets(2, 2, 2, 2));
        btnExpand.setName("btnExpand"); // NOI18N
        btnExpand.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExpandActionPerformed(evt);
            }
        });
        panelDimensionHierarchy.add(btnExpand, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 50, 25, -1));

        btnCollapse.setText("-");
        btnCollapse.setToolTipText(bundle.getString("CollapseTooltip")); // NOI18N
        btnCollapse.setMargin(new java.awt.Insets(2, 2, 2, 2));
        btnCollapse.setName("btnCollapse"); // NOI18N
        btnCollapse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCollapseActionPerformed(evt);
            }
        });
        panelDimensionHierarchy.add(btnCollapse, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 80, 25, -1));

        btnCollapseAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/collapseAll.png"))); // NOI18N
        btnCollapseAll.setToolTipText(bundle.getString("CollapseAllTooltip")); // NOI18N
        btnCollapseAll.setName("btnCollapseAll"); // NOI18N
        btnCollapseAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCollapseAllActionPerformed(evt);
            }
        });
        panelDimensionHierarchy.add(btnCollapseAll, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 150, 25, -1));

        btnExpandAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/expandAll.png"))); // NOI18N
        btnExpandAll.setToolTipText(bundle.getString("ExpandAllTooltip")); // NOI18N
        btnExpandAll.setName("btnExpandAll"); // NOI18N
        btnExpandAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExpandAllActionPerformed(evt);
            }
        });
        panelDimensionHierarchy.add(btnExpandAll, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 120, 25, -1));

        btnLevel1.setText("1");
        btnLevel1.setToolTipText(bundle.getString("ShowLevel")); // NOI18N
        btnLevel1.setMargin(new java.awt.Insets(2, 2, 2, 2));
        btnLevel1.setName("btnLevel1"); // NOI18N
        btnLevel1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLevel1ActionPerformed(evt);
            }
        });
        panelDimensionHierarchy.add(btnLevel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 10, 25, -1));

        btnLevel2.setText("2");
        btnLevel2.setToolTipText(bundle.getString("ShowLevel")); // NOI18N
        btnLevel2.setMargin(new java.awt.Insets(2, 2, 2, 2));
        btnLevel2.setName("btnLevel2"); // NOI18N
        btnLevel2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLevel2ActionPerformed(evt);
            }
        });
        panelDimensionHierarchy.add(btnLevel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 10, 25, -1));

        btnLevel3.setText("3");
        btnLevel3.setToolTipText(bundle.getString("ShowLevel")); // NOI18N
        btnLevel3.setMargin(new java.awt.Insets(2, 2, 2, 2));
        btnLevel3.setName("btnLevel3"); // NOI18N
        btnLevel3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLevel3ActionPerformed(evt);
            }
        });
        panelDimensionHierarchy.add(btnLevel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 10, 25, -1));

        btnLevel4.setText("4");
        btnLevel4.setToolTipText(bundle.getString("ShowLevel")); // NOI18N
        btnLevel4.setMargin(new java.awt.Insets(2, 2, 2, 2));
        btnLevel4.setName("btnLevel4"); // NOI18N
        btnLevel4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLevel4ActionPerformed(evt);
            }
        });
        panelDimensionHierarchy.add(btnLevel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 10, 25, -1));

        btnLevel5.setText("5");
        btnLevel5.setToolTipText(bundle.getString("ShowLevel")); // NOI18N
        btnLevel5.setMargin(new java.awt.Insets(2, 2, 2, 2));
        btnLevel5.setName("btnLevel5"); // NOI18N
        btnLevel5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLevel5ActionPerformed(evt);
            }
        });
        panelDimensionHierarchy.add(btnLevel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 10, 25, -1));

        btnLevel6.setText("6");
        btnLevel6.setToolTipText(bundle.getString("ShowLevel")); // NOI18N
        btnLevel6.setMargin(new java.awt.Insets(2, 2, 2, 2));
        btnLevel6.setName("btnLevel6"); // NOI18N
        btnLevel6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLevel6ActionPerformed(evt);
            }
        });
        panelDimensionHierarchy.add(btnLevel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 10, 25, -1));

        btnLevel7.setText("7");
        btnLevel7.setToolTipText(bundle.getString("ShowLevel")); // NOI18N
        btnLevel7.setMargin(new java.awt.Insets(2, 2, 2, 2));
        btnLevel7.setName("btnLevel7"); // NOI18N
        btnLevel7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLevel7ActionPerformed(evt);
            }
        });
        panelDimensionHierarchy.add(btnLevel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 10, 25, -1));

        btnLevel8.setText("8");
        btnLevel8.setToolTipText(bundle.getString("ShowLevel")); // NOI18N
        btnLevel8.setMargin(new java.awt.Insets(2, 2, 2, 2));
        btnLevel8.setName("btnLevel8"); // NOI18N
        btnLevel8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLevel8ActionPerformed(evt);
            }
        });
        panelDimensionHierarchy.add(btnLevel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 10, 25, -1));

        btnWeight.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/consolidationWeight.png"))); // NOI18N
        btnWeight.setToolTipText(bundle.getString("Weight")); // NOI18N
        btnWeight.setMargin(new java.awt.Insets(2, 2, 2, 2));
        btnWeight.setName("btnWeight"); // NOI18N
        btnWeight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnWeightActionPerformed(evt);
            }
        });
        panelDimensionHierarchy.add(btnWeight, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 200, 25, -1));

        jSplitPane1.setRightComponent(panelDimensionHierarchy);

        panelRules.setName("panelRules"); // NOI18N
        panelRules.setPreferredSize(new java.awt.Dimension(550, 395));
        panelRules.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel13.setText(bundle.getString("Rules")); // NOI18N
        jLabel13.setName("jLabel13"); // NOI18N
        panelRules.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, -1));

        jLabel14.setText(bundle.getString("SelectedRule")); // NOI18N
        jLabel14.setName("jLabel14"); // NOI18N
        panelRules.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 210, -1, -1));

        jScrollPane10.setName("jScrollPane10"); // NOI18N

        txtRule.setColumns(20);
        txtRule.setFont(new java.awt.Font("Tahoma", 0, 11));
        txtRule.setLineWrap(true);
        txtRule.setRows(5);
        txtRule.setWrapStyleWord(true);
        txtRule.setName("txtRule"); // NOI18N
        jScrollPane10.setViewportView(txtRule);

        panelRules.add(jScrollPane10, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 230, 530, 50));

        btnNewRule.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/ruleNew.png"))); // NOI18N
        btnNewRule.setText(bundle.getString("New")); // NOI18N
        btnNewRule.setToolTipText(bundle.getString("Create Rule")); // NOI18N
        btnNewRule.setName("btnNewRule"); // NOI18N
        btnNewRule.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewRuleActionPerformed(evt);
            }
        });
        panelRules.add(btnNewRule, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 370, 25, -1));

        btnDeleteRule.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/ruleDelete.png"))); // NOI18N
        btnDeleteRule.setText(bundle.getString("Delete")); // NOI18N
        btnDeleteRule.setToolTipText(bundle.getString("DeleteRule")); // NOI18N
        btnDeleteRule.setName("btnDeleteRule"); // NOI18N
        btnDeleteRule.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteRuleActionPerformed(evt);
            }
        });
        panelRules.add(btnDeleteRule, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 370, 25, -1));

        btnUpdateRule.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/ruleUpdate.png"))); // NOI18N
        btnUpdateRule.setToolTipText(bundle.getString("UpdateRule")); // NOI18N
        btnUpdateRule.setName("btnUpdateRule"); // NOI18N
        btnUpdateRule.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateRuleActionPerformed(evt);
            }
        });
        panelRules.add(btnUpdateRule, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 370, 25, -1));

        btnRuleUp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/MoveUp.png"))); // NOI18N
        btnRuleUp.setName("btnRuleUp"); // NOI18N
        btnRuleUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRuleUpActionPerformed(evt);
            }
        });
        panelRules.add(btnRuleUp, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 30, 25, -1));

        btnRuleDown.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/MoveDown.PNG"))); // NOI18N
        btnRuleDown.setName("btnRuleDown"); // NOI18N
        btnRuleDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRuleDownActionPerformed(evt);
            }
        });
        panelRules.add(btnRuleDown, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 60, 25, -1));

        jLabel15.setText(bundle.getString("RuleComment")); // NOI18N
        jLabel15.setName("jLabel15"); // NOI18N
        panelRules.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 290, -1, -1));

        jScrollPane11.setName("jScrollPane11"); // NOI18N

        txtRuleComment.setColumns(20);
        txtRuleComment.setFont(new java.awt.Font("Tahoma", 0, 11));
        txtRuleComment.setLineWrap(true);
        txtRuleComment.setRows(5);
        txtRuleComment.setWrapStyleWord(true);
        txtRuleComment.setName("txtRuleComment"); // NOI18N
        jScrollPane11.setViewportView(txtRuleComment);

        panelRules.add(jScrollPane11, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 310, 530, 50));

        txtError.setForeground(new java.awt.Color(255, 0, 51));
        txtError.setName("txtError"); // NOI18N
        panelRules.add(txtError, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 370, 390, 30));

        btnCheckRule.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/ruleValidate.png"))); // NOI18N
        btnCheckRule.setText(bundle.getString("Check")); // NOI18N
        btnCheckRule.setToolTipText(bundle.getString("CheckRule")); // NOI18N
        btnCheckRule.setName("btnCheckRule"); // NOI18N
        btnCheckRule.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCheckRuleActionPerformed(evt);
            }
        });
        panelRules.add(btnCheckRule, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 370, 25, -1));

        jScrollPane6.setName("jScrollPane6"); // NOI18N

        tableRules.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Rule", "Comment", "Last Change"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tableRules.setGridColor(new java.awt.Color(204, 204, 204));
        tableRules.setName("tableRules"); // NOI18N
        jScrollPane6.setViewportView(tableRules);
        tableRules.getColumnModel().getColumn(0).setPreferredWidth(180);
        tableRules.getColumnModel().getColumn(0).setHeaderValue(bundle.getString("Rule")); // NOI18N
        tableRules.getColumnModel().getColumn(1).setHeaderValue(bundle.getString("Comment")); // NOI18N
        tableRules.getColumnModel().getColumn(2).setPreferredWidth(50);
        tableRules.getColumnModel().getColumn(2).setHeaderValue(bundle.getString("Changed")); // NOI18N

        panelRules.add(jScrollPane6, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 30, 490, 170));

        jSplitPane1.setRightComponent(panelRules);

        panelServer.setName("panelServer"); // NOI18N
        panelServer.setPreferredSize(new java.awt.Dimension(550, 395));
        panelServer.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jScrollPane8.setName("jScrollPane8"); // NOI18N

        treeDatabases.setModel(databaseTreeModel);
        treeDatabases.setName("treeDatabases"); // NOI18N
        treeDatabases.setRootVisible(false);
        treeDatabases.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                treeDatabasesValueChanged(evt);
            }
        });
        treeDatabases.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                treeDatabasesFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                treeDatabasesFocusLost(evt);
            }
        });
        treeDatabases.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                treeDatabasesKeyPressed(evt);
            }
        });
        jScrollPane8.setViewportView(treeDatabases);

        panelServer.add(jScrollPane8, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 30, 230, 330));

        jLabel2.setText(bundle.getString("Databases")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N
        panelServer.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, -1));

        btnDatabaseNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/databaseNew.PNG"))); // NOI18N
        btnDatabaseNew.setToolTipText(bundle.getString("CreateDatabase")); // NOI18N
        btnDatabaseNew.setName("btnDatabaseNew"); // NOI18N
        btnDatabaseNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDatabaseNewActionPerformed(evt);
            }
        });
        panelServer.add(btnDatabaseNew, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 370, 25, -1));

        btnDatabaseDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/databaseDelete.PNG"))); // NOI18N
        btnDatabaseDelete.setToolTipText(bundle.getString("DatabaseDelete")); // NOI18N
        btnDatabaseDelete.setName("btnDatabaseDelete"); // NOI18N
        btnDatabaseDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDatabaseDeleteActionPerformed(evt);
            }
        });
        panelServer.add(btnDatabaseDelete, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 370, 25, -1));

        jSplitPane1.setRightComponent(panelServer);

        panelGroups.setName("panelGroups"); // NOI18N
        panelGroups.setPreferredSize(new java.awt.Dimension(550, 395));
        panelGroups.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jScrollPane12.setName("jScrollPane12"); // NOI18N

        treeGroups.setModel(groupTreeModel);
        treeGroups.setName("treeGroups"); // NOI18N
        treeGroups.setRootVisible(false);
        treeGroups.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                treeGroupsValueChanged(evt);
            }
        });
        treeGroups.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                treeGroupsFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                treeGroupsFocusLost(evt);
            }
        });
        treeGroups.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                treeGroupsKeyPressed(evt);
            }
        });
        jScrollPane12.setViewportView(treeGroups);

        panelGroups.add(jScrollPane12, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 30, 230, 330));

        jLabel16.setText(bundle.getString("Groups")); // NOI18N
        jLabel16.setName("jLabel16"); // NOI18N
        panelGroups.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, -1));

        btnGroupNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/groupNew.png"))); // NOI18N
        btnGroupNew.setToolTipText(bundle.getString("NewGroup")); // NOI18N
        btnGroupNew.setName("btnGroupNew"); // NOI18N
        btnGroupNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGroupNewActionPerformed(evt);
            }
        });
        panelGroups.add(btnGroupNew, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 370, 25, -1));

        btnGroupDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/groupDelete.png"))); // NOI18N
        btnGroupDelete.setToolTipText(bundle.getString("GroupDelete")); // NOI18N
        btnGroupDelete.setName("btnGroupDelete"); // NOI18N
        btnGroupDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGroupDeleteActionPerformed(evt);
            }
        });
        panelGroups.add(btnGroupDelete, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 370, 25, -1));

        btnGroupEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/groupEdit.png"))); // NOI18N
        btnGroupEdit.setToolTipText(bundle.getString("GroupDelete")); // NOI18N
        btnGroupEdit.setName("btnGroupEdit"); // NOI18N
        btnGroupEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGroupEditActionPerformed(evt);
            }
        });
        panelGroups.add(btnGroupEdit, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 370, 25, -1));

        jSplitPane1.setRightComponent(panelGroups);

        panelUsers.setName("panelUsers"); // NOI18N
        panelUsers.setPreferredSize(new java.awt.Dimension(550, 395));
        panelUsers.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jScrollPane13.setName("jScrollPane13"); // NOI18N

        treeUsers.setModel(userTreeModel);
        treeUsers.setName("treeUsers"); // NOI18N
        treeUsers.setRootVisible(false);
        treeUsers.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                treeUsersValueChanged(evt);
            }
        });
        treeUsers.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                treeUsersFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                treeUsersFocusLost(evt);
            }
        });
        treeUsers.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                treeUsersKeyPressed(evt);
            }
        });
        jScrollPane13.setViewportView(treeUsers);

        panelUsers.add(jScrollPane13, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 30, 230, 330));

        jLabel17.setText(bundle.getString("Users")); // NOI18N
        jLabel17.setName("jLabel17"); // NOI18N
        panelUsers.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, -1));

        btnUserNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/userNew.png"))); // NOI18N
        btnUserNew.setToolTipText(bundle.getString("NewGroup")); // NOI18N
        btnUserNew.setName("btnUserNew"); // NOI18N
        btnUserNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUserNewActionPerformed(evt);
            }
        });
        panelUsers.add(btnUserNew, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 370, 25, -1));

        btnUserDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/userDelete.png"))); // NOI18N
        btnUserDelete.setToolTipText(bundle.getString("GroupDelete")); // NOI18N
        btnUserDelete.setName("btnUserDelete"); // NOI18N
        btnUserDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUserDeleteActionPerformed(evt);
            }
        });
        panelUsers.add(btnUserDelete, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 370, 25, -1));

        btnUserEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/userEdit.png"))); // NOI18N
        btnUserEdit.setToolTipText(bundle.getString("GroupDelete")); // NOI18N
        btnUserEdit.setName("btnUserEdit"); // NOI18N
        btnUserEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUserEditActionPerformed(evt);
            }
        });
        panelUsers.add(btnUserEdit, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 370, 25, -1));

        jSplitPane1.setRightComponent(panelUsers);

        panelRoles.setName("panelRoles"); // NOI18N
        panelRoles.setPreferredSize(new java.awt.Dimension(550, 395));
        panelRoles.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jScrollPane14.setName("jScrollPane14"); // NOI18N

        treeRoles.setModel(roleTreeModel);
        treeRoles.setName("treeRoles"); // NOI18N
        treeRoles.setRootVisible(false);
        treeRoles.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                treeRolesValueChanged(evt);
            }
        });
        treeRoles.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                treeRolesFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                treeRolesFocusLost(evt);
            }
        });
        treeRoles.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                treeRolesKeyPressed(evt);
            }
        });
        jScrollPane14.setViewportView(treeRoles);

        panelRoles.add(jScrollPane14, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 30, 230, 330));

        jLabel18.setText(bundle.getString("Roles")); // NOI18N
        jLabel18.setName("jLabel18"); // NOI18N
        panelRoles.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, -1));

        btnRoleNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/roleNew.png"))); // NOI18N
        btnRoleNew.setToolTipText(bundle.getString("NewGroup")); // NOI18N
        btnRoleNew.setName("btnRoleNew"); // NOI18N
        btnRoleNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRoleNewActionPerformed(evt);
            }
        });
        panelRoles.add(btnRoleNew, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 370, 25, -1));

        btnRoleDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/roleDelete.png"))); // NOI18N
        btnRoleDelete.setToolTipText(bundle.getString("GroupDelete")); // NOI18N
        btnRoleDelete.setName("btnRoleDelete"); // NOI18N
        btnRoleDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRoleDeleteActionPerformed(evt);
            }
        });
        panelRoles.add(btnRoleDelete, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 370, 25, -1));

        btnRoleEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/roleEdit.png"))); // NOI18N
        btnRoleEdit.setToolTipText(bundle.getString("GroupDelete")); // NOI18N
        btnRoleEdit.setName("btnRoleEdit"); // NOI18N
        btnRoleEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRoleEditActionPerformed(evt);
            }
        });
        panelRoles.add(btnRoleEdit, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 370, 25, -1));

        jSplitPane1.setRightComponent(panelRoles);

        panelGroup.setMaximumSize(new java.awt.Dimension(550, 395));
        panelGroup.setMinimumSize(new java.awt.Dimension(550, 395));
        panelGroup.setName("panelGroup"); // NOI18N
        panelGroup.setPreferredSize(new java.awt.Dimension(550, 395));
        panelGroup.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jScrollPane16.setName("jScrollPane16"); // NOI18N

        tableRoles.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Role", "Active"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tableRoles.setName("tableRoles"); // NOI18N
        tableRoles.getTableHeader().setReorderingAllowed(false);
        jScrollPane16.setViewportView(tableRoles);
        tableRoles.getColumnModel().getColumn(0).setHeaderValue(bundle.getString("Role")); // NOI18N
        tableRoles.getColumnModel().getColumn(1).setMinWidth(60);
        tableRoles.getColumnModel().getColumn(1).setPreferredWidth(60);
        tableRoles.getColumnModel().getColumn(1).setMaxWidth(60);
        tableRoles.getColumnModel().getColumn(1).setHeaderValue(bundle.getString("Active")); // NOI18N

        panelGroup.add(jScrollPane16, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 30, 330, 360));

        jLabel1.setText(bundle.getString("GroupRole")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N
        panelGroup.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, -1));

        jSplitPane1.setRightComponent(panelGroup);

        panelUser.setMaximumSize(new java.awt.Dimension(550, 395));
        panelUser.setMinimumSize(new java.awt.Dimension(550, 395));
        panelUser.setName("panelUser"); // NOI18N
        panelUser.setPreferredSize(new java.awt.Dimension(550, 395));
        panelUser.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel4.setText(bundle.getString("UserGroup")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N
        panelUser.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, -1));

        jScrollPane18.setName("jScrollPane18"); // NOI18N

        tableGroups.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Group", "Member"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tableGroups.setName("tableGroups"); // NOI18N
        tableGroups.getTableHeader().setReorderingAllowed(false);
        jScrollPane18.setViewportView(tableGroups);
        tableGroups.getColumnModel().getColumn(0).setResizable(false);
        tableGroups.getColumnModel().getColumn(0).setHeaderValue(bundle.getString("Group")); // NOI18N
        tableGroups.getColumnModel().getColumn(1).setMinWidth(60);
        tableGroups.getColumnModel().getColumn(1).setPreferredWidth(60);
        tableGroups.getColumnModel().getColumn(1).setMaxWidth(60);
        tableGroups.getColumnModel().getColumn(1).setHeaderValue(bundle.getString("Member")); // NOI18N

        panelUser.add(jScrollPane18, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 30, 330, 360));

        btnPassword.setText(bundle.getString("ChangePassword")); // NOI18N
        btnPassword.setName("btnPassword"); // NOI18N
        btnPassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPasswordActionPerformed(evt);
            }
        });
        panelUser.add(btnPassword, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 30, 130, -1));

        jSplitPane1.setRightComponent(panelUser);

        panelRole.setMaximumSize(new java.awt.Dimension(550, 395));
        panelRole.setMinimumSize(new java.awt.Dimension(550, 395));
        panelRole.setName("panelRole"); // NOI18N
        panelRole.setPreferredSize(new java.awt.Dimension(550, 395));
        panelRole.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jScrollPane17.setName("jScrollPane17"); // NOI18N

        tableRights.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Right Object", "Right"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tableRights.setName("tableRights"); // NOI18N
        tableRights.getTableHeader().setReorderingAllowed(false);
        jScrollPane17.setViewportView(tableRights);
        tableRights.getColumnModel().getColumn(0).setHeaderValue(bundle.getString("RightObject")); // NOI18N
        tableRights.getColumnModel().getColumn(1).setMinWidth(60);
        tableRights.getColumnModel().getColumn(1).setPreferredWidth(60);
        tableRights.getColumnModel().getColumn(1).setMaxWidth(60);
        tableRights.getColumnModel().getColumn(1).setHeaderValue(bundle.getString("Right")); // NOI18N
        tableRights.getColumnModel().getColumn(1).setCellEditor(editorRights);

        panelRole.add(jScrollPane17, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 30, 330, 360));

        jLabel3.setText(bundle.getString("RoleGroup")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N
        panelRole.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, -1));

        jLabel5.setText("Undefined: No Text"); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N
        panelRole.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 160, -1, -1));

        jLabel6.setText("Splash      : S"); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N
        panelRole.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 60, -1, -1));

        jLabel7.setText("Delete      : D"); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N
        panelRole.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 80, -1, -1));

        jLabel19.setText("Write        : W"); // NOI18N
        jLabel19.setName("jLabel19"); // NOI18N
        panelRole.add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 100, -1, -1));

        jLabel20.setText("Read        : R"); // NOI18N
        jLabel20.setName("jLabel20"); // NOI18N
        panelRole.add(jLabel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 120, -1, -1));

        jLabel21.setText(bundle.getString("RightOptions")); // NOI18N
        jLabel21.setName("jLabel21"); // NOI18N
        panelRole.add(jLabel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 30, -1, 20));

        jLabel30.setText("None        : N"); // NOI18N
        jLabel30.setName("jLabel30"); // NOI18N
        panelRole.add(jLabel30, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 140, -1, -1));

        jSplitPane1.setRightComponent(panelRole);

        panelAttributes.setName("panelAttributes"); // NOI18N
        panelAttributes.setPreferredSize(new java.awt.Dimension(550, 395));
        panelAttributes.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jScrollPane15.setName("jScrollPane15"); // NOI18N

        treeAttributes.setModel(attributesTreeModel);
        treeAttributes.setName("treeAttributes"); // NOI18N
        treeAttributes.setRootVisible(false);
        treeAttributes.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                treeAttributesValueChanged(evt);
            }
        });
        treeAttributes.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                treeAttributesFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                treeAttributesFocusLost(evt);
            }
        });
        treeAttributes.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                treeAttributesKeyPressed(evt);
            }
        });
        jScrollPane15.setViewportView(treeAttributes);

        panelAttributes.add(jScrollPane15, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 30, 230, 330));

        jLabel22.setText(bundle.getString("Attributes")); // NOI18N
        jLabel22.setName("jLabel22"); // NOI18N
        panelAttributes.add(jLabel22, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, -1));

        btnAttributeNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/userNew.png"))); // NOI18N
        btnAttributeNew.setToolTipText(bundle.getString("NewGroup")); // NOI18N
        btnAttributeNew.setName("btnAttributeNew"); // NOI18N
        btnAttributeNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAttributeNewActionPerformed(evt);
            }
        });
        panelAttributes.add(btnAttributeNew, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 370, 25, -1));

        btnAttributeDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/userDelete.png"))); // NOI18N
        btnAttributeDelete.setToolTipText(bundle.getString("GroupDelete")); // NOI18N
        btnAttributeDelete.setName("btnAttributeDelete"); // NOI18N
        btnAttributeDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAttributeDeleteActionPerformed(evt);
            }
        });
        panelAttributes.add(btnAttributeDelete, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 370, 25, -1));

        btnAttributeEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/userEdit.png"))); // NOI18N
        btnAttributeEdit.setToolTipText(bundle.getString("GroupDelete")); // NOI18N
        btnAttributeEdit.setName("btnAttributeEdit"); // NOI18N
        btnAttributeEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAttributeEditActionPerformed(evt);
            }
        });
        panelAttributes.add(btnAttributeEdit, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 370, 25, -1));

        btnAttributeNumeric.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/numeric.png"))); // NOI18N
        btnAttributeNumeric.setToolTipText(bundle.getString("ElementNumerci")); // NOI18N
        btnAttributeNumeric.setName("btnAttributeNumeric"); // NOI18N
        btnAttributeNumeric.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAttributeNumericActionPerformed(evt);
            }
        });
        panelAttributes.add(btnAttributeNumeric, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 370, 25, -1));

        btnAttributeString.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/string.png"))); // NOI18N
        btnAttributeString.setToolTipText(bundle.getString("ElementString")); // NOI18N
        btnAttributeString.setName("btnAttributeString"); // NOI18N
        btnAttributeString.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAttributeStringActionPerformed(evt);
            }
        });
        panelAttributes.add(btnAttributeString, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 370, 25, -1));

        jSplitPane1.setRightComponent(panelAttributes);

        panelDimensionRights.setName("panelDimensionRights"); // NOI18N
        panelDimensionRights.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnEditDimensionRights.setText(bundle.getString("EditDimensionRights")); // NOI18N
        btnEditDimensionRights.setName("btnEditDimensionRights"); // NOI18N
        btnEditDimensionRights.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditDimensionRightsActionPerformed(evt);
            }
        });
        panelDimensionRights.add(btnEditDimensionRights, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, 190, -1));

        jLabel34.setText(bundle.getString("EditDimRights")); // NOI18N
        jLabel34.setName("jLabel34"); // NOI18N
        panelDimensionRights.add(jLabel34, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, 570, 20));

        jLabel36.setText(bundle.getString("EditDimRightsInfo2")); // NOI18N
        jLabel36.setName("jLabel36"); // NOI18N
        panelDimensionRights.add(jLabel36, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, -1, -1));

        jSplitPane1.setRightComponent(panelDimensionRights);

        panelCubeRights.setMaximumSize(new java.awt.Dimension(550, 395));
        panelCubeRights.setMinimumSize(new java.awt.Dimension(550, 395));
        panelCubeRights.setName("panelCubeRights"); // NOI18N
        panelCubeRights.setPreferredSize(new java.awt.Dimension(550, 395));
        panelCubeRights.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jScrollPane19.setName("jScrollPane19"); // NOI18N

        tableCubeRights.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Group", "Right"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tableCubeRights.setName("tableCubeRights"); // NOI18N
        tableCubeRights.getTableHeader().setReorderingAllowed(false);
        jScrollPane19.setViewportView(tableCubeRights);
        tableCubeRights.getColumnModel().getColumn(0).setHeaderValue(bundle.getString("Group")); // NOI18N
        tableCubeRights.getColumnModel().getColumn(1).setMinWidth(60);
        tableCubeRights.getColumnModel().getColumn(1).setPreferredWidth(60);
        tableCubeRights.getColumnModel().getColumn(1).setMaxWidth(60);
        tableCubeRights.getColumnModel().getColumn(1).setHeaderValue(bundle.getString("Right")); // NOI18N
        tableCubeRights.getColumnModel().getColumn(1).setCellEditor(editorRights);

        panelCubeRights.add(jScrollPane19, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 30, 330, 360));

        jLabel10.setText(bundle.getString("CubeRights")); // NOI18N
        jLabel10.setName("jLabel10"); // NOI18N
        panelCubeRights.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, -1));

        jLabel28.setText(bundle.getString("RightOptions")); // NOI18N
        jLabel28.setName("jLabel28"); // NOI18N
        panelCubeRights.add(jLabel28, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 30, -1, 20));

        jLabel23.setText("Undefined: No Text"); // NOI18N
        jLabel23.setName("jLabel23"); // NOI18N
        panelCubeRights.add(jLabel23, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 160, -1, -1));

        jLabel24.setText("Splash      : S"); // NOI18N
        jLabel24.setName("jLabel24"); // NOI18N
        panelCubeRights.add(jLabel24, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 60, -1, -1));

        jLabel25.setText("Delete      : D"); // NOI18N
        jLabel25.setName("jLabel25"); // NOI18N
        panelCubeRights.add(jLabel25, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 80, -1, -1));

        jLabel26.setText("Write        : W"); // NOI18N
        jLabel26.setName("jLabel26"); // NOI18N
        panelCubeRights.add(jLabel26, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 100, -1, -1));

        jLabel27.setText("Read        : R"); // NOI18N
        jLabel27.setName("jLabel27"); // NOI18N
        panelCubeRights.add(jLabel27, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 120, -1, -1));

        jLabel32.setText("None        : N"); // NOI18N
        jLabel32.setName("jLabel32"); // NOI18N
        panelCubeRights.add(jLabel32, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 140, -1, -1));

        jSplitPane1.setRightComponent(panelCubeRights);

        panelCube.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        panelCube.setName("panelCube"); // NOI18N
        panelCube.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel29.setText(bundle.getString("FilledCellCount")); // NOI18N
        jLabel29.setName("jLabel29"); // NOI18N
        panelCube.add(jLabel29, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 50, -1, -1));

        lblFilledCellCount.setText("jLabel30");
        lblFilledCellCount.setName("lblFilledCellCount"); // NOI18N
        panelCube.add(lblFilledCellCount, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 50, -1, -1));

        jLabel31.setText(bundle.getString("CellCount")); // NOI18N
        jLabel31.setName("jLabel31"); // NOI18N
        panelCube.add(jLabel31, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, -1, -1));

        lblCellCount.setText("jLabel30");
        lblCellCount.setName("lblCellCount"); // NOI18N
        panelCube.add(lblCellCount, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 20, -1, -1));

        jLabel33.setText(bundle.getString("Status")); // NOI18N
        jLabel33.setName("jLabel33"); // NOI18N
        panelCube.add(jLabel33, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, -1, -1));

        lblStatus.setText("jLabel30");
        lblStatus.setName("lblStatus"); // NOI18N
        panelCube.add(lblStatus, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 80, -1, -1));

        btnDeleteCubeValues.setText(bundle.getString("CubeEmpty")); // NOI18N
        btnDeleteCubeValues.setName("btnDeleteCubeValues"); // NOI18N
        btnDeleteCubeValues.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteCubeValuesActionPerformed(evt);
            }
        });
        panelCube.add(btnDeleteCubeValues, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 210, 220, -1));

        jLabel35.setText(bundle.getString("DimensionList")); // NOI18N
        jLabel35.setToolTipText(bundle.getString("DimensionList")); // NOI18N
        jLabel35.setName("jLabel35"); // NOI18N
        panelCube.add(jLabel35, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 110, -1, -1));

        jScrollPane9.setBorder(null);
        jScrollPane9.setName("jScrollPane9"); // NOI18N

        txtAreaDimensions.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        txtAreaDimensions.setColumns(20);
        txtAreaDimensions.setEditable(false);
        txtAreaDimensions.setFont(new java.awt.Font("Tahoma", 0, 11));
        txtAreaDimensions.setLineWrap(true);
        txtAreaDimensions.setRows(3);
        txtAreaDimensions.setWrapStyleWord(true);
        txtAreaDimensions.setBorder(null);
        txtAreaDimensions.setName("txtAreaDimensions"); // NOI18N
        jScrollPane9.setViewportView(txtAreaDimensions);

        panelCube.add(jScrollPane9, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 110, 270, 80));

        jSplitPane1.setRightComponent(panelCube);

        getContentPane().add(jSplitPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(17, 50, 790, 410));

        jPanel1.setName("jPanel1"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 42, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 14, Short.MAX_VALUE)
        );

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(780, 490, 42, 14));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOKActionPerformed
        TreePath selPath = treeOverview.getSelectionPath();
        int pathCount = selPath.getPathCount();
        overviewSelection = new String[pathCount];

        for (int i = 0; i < pathCount; i++) {
            overviewSelection[i] = selPath.getPathComponent(i).toString();
        }

        setVisible(false);
        dispose();
    }//GEN-LAST:event_btnOKActionPerformed

    private void treeOverviewKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_treeOverviewKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER && treeOverview.isEditing() == false) {
//
//            DefaultMutableTreeNode node = new DefaultMutableTreeNode();
//            node.setUserObject(new String("DatabaseNew"));
//            rootNode.add(node);
//            overviewTreeModel.nodeStructureChanged(rootNode);
//            TreePath selection = new TreePath(((DefaultMutableTreeNode) node).getPath());
//            treeOverview.setSelectionPath(selection);
//            treeOverview.scrollPathToVisible(selection);
//            treeOverview.startEditingAtPath(selection);
        }
    }//GEN-LAST:event_treeOverviewKeyPressed

    private void treeOverviewMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_treeOverviewMouseClicked
        TreePath path = treeOverview.getSelectionPath();
        if (path != null) {
             DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
             if (node instanceof AttributesTreeNode) {
                 AttributesTreeNode aNode = (AttributesTreeNode) node;
                 if (!aNode.isInitialized()) {
                     addAttributes(aNode);
                     aNode.setInitialized(true);
                 }
             } else if (node instanceof ModellerFolderTreeNode && node.getUserObject().equals("Subsets")) {
                 SubsetModeller subsetModeller = new SubsetModeller(this, true, context);
                 ConnectionTreeNode connection = (ConnectionTreeNode)path.getPathComponent(1);
                 DatabaseTreeNode database = (DatabaseTreeNode)path.getPathComponent(2);
                 subsetModeller.setDatabase(connection.getConnection(), database.getDatabase(), null);
                 subsetModeller.setDimension(((DimensionTreeNode)node.getParent()).getDimension());
                 subsetModeller.setAlwaysOnTop(true);
                 subsetModeller.setVisible(true);  
            } else if (node instanceof ConnectionTreeNode) {
                ConnectionTreeNode connNode = (ConnectionTreeNode) node;
                if (connNode.getConnection().getConnection() == null || !connNode.getConnection().getConnection().isConnected()) {
                   btnConnectActionPerformed(null);
                }
            }
        }
    }//GEN-LAST:event_treeOverviewMouseClicked

    private void treeOverviewValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_treeOverviewValueChanged

        int     pos = jSplitPane1.getDividerLocation();

        TreePath path = treeOverview.getSelectionPath();
        if (path != null && oldSelectionPath != null) {
            if (path.equals(oldSelectionPath) && (!(path.getLastPathComponent() instanceof ModellerFolderTreeNode)))
                return;
        }
        oldSelectionPath = path;
        if (path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
            if (node instanceof ConnectionTreeNode) {
                jSplitPane1.setRightComponent(panelServer);
                fillDatabaseTree((ConnectionTreeNode)node);
                updateServerPanelButtonStates();
            } else if (node instanceof DatabaseTreeNode ||
                       node instanceof DimensionsTreeNode||
                       node instanceof CubesTreeNode) {
                jSplitPane1.setRightComponent(panelDatabase);
                DatabaseTreeNode dbNode = null;
                dbNode = getDBNode(node);
                if (dbNode != null) {
                    fillCubeTree(dbNode, ICube.CubeType.CUBE_NORMAL);
                    fillDimensionTree(dbNode, IDimension.DimensionType.DIMENSION_NORMAL);
                    updateDatabasePanelButtonStates();
                }
            } else if (node instanceof CubeTreeNode) {
                showCubePanel((CubeTreeNode)node);
            } else if (node instanceof CubeRulesTreeNode) {
                showRulesPanel((CubeRulesTreeNode)node);
            } else if (node instanceof CubeRightsTreeNode) {
                showCubeRightsPanel((CubeRightsTreeNode)node);
            } else if (node instanceof DimensionTreeNode) {
                showDimensionPanelFlat();
            } else if (node instanceof UsersTreeNode) {
                showUsersPanel();
            } else if (node instanceof UserTreeNode) {
                showUserPanel();
            } else if (node instanceof GroupsTreeNode) {
                showGroupsPanel();
            } else if (node instanceof GroupTreeNode) {
                showGroupPanel();
            } else if (node instanceof RolesTreeNode) {
                showRolesPanel();
            } else if (node instanceof RoleTreeNode) {
                showRolePanel();
            } else if (node instanceof AttributesTreeNode) {
                showDimensionPanelFlat();
//                showAttributesPanel();
            } else if (node instanceof AttributeTreeNode) {
                showAttributePanel();
            } else if (node instanceof DimensionRightsTreeNode) {
                showDimensionRightsPanel();
            } 
        }

        updateConnectionButtons();

        jSplitPane1.setDividerLocation(pos);

        //cschw: clear consolidation view
        rootNodeConsolidatedElements.removeAllChildren();
        //consolidatedElementTreeModel.nodeStructureChanged(rootNodeConsolidatedElements);
        deactivateConsolidationMode();
        treeConsolidation.removeAll();
        consolidatedElementTreeModel.reload();
        treeElements.repaint();
        updateDimensionPanelButtonStates();

    }//GEN-LAST:event_treeOverviewValueChanged

    private void treeDimensionsKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_treeDimensionsKeyPressed
        if (treeDimensions.isEditing() == false) {
            if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                createDimension();
            } else if (evt.getKeyCode() == KeyEvent.VK_DELETE) {
                deleteSelectedDimension();
            }
            updateDatabasePanelButtonStates();
        }
    }//GEN-LAST:event_treeDimensionsKeyPressed

    private void treeDimensionsValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_treeDimensionsValueChanged
        TreePath path = treeDimensions.getSelectionPath();
        if (path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
            if (node instanceof DimensionTreeNode) {
                editDimNode = (DimensionTreeNode)node;
            }
            updateDatabasePanelButtonStates();
        }
    }//GEN-LAST:event_treeDimensionsValueChanged

    private void btnCreateCubeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCreateCubeActionPerformed
        DefaultMutableTreeNode node = getSelected(treeOverview);
        DatabaseTreeNode dbNode = getDBNode(node);
        if (dbNode != null) {
            IDatabase db = dbNode.getDatabase();
            CreateCubeDialog createCubeDialog = new CreateCubeDialog(this,
                    true, context, db);
            createCubeDialog.setVisible(true);
            if (createCubeDialog.getModalResult() == JOptionPane.OK_OPTION) {

                try {
                    DefaultListModel listCubeDimensions = createCubeDialog.getCubeDimensionModel();
                    IDimension[] cubeDimensions = new IDimension[listCubeDimensions.getSize()];

                    for (int i = 0; i < listCubeDimensions.getSize(); i++) {
                        cubeDimensions[i] = (IDimension)listCubeDimensions.get(i);
                    }
                    db.addCube(createCubeDialog.getCubeName(), cubeDimensions);
                    fillCubeTree(dbNode, ICube.CubeType.CUBE_NORMAL);
                } catch (PaloJException pe) {
                    JOptionPane.showMessageDialog(this, pe.getMessage(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
                } catch (PaloException pe) {
                    JOptionPane.showMessageDialog(this, pe.getDescription() + ", " + pe.getReason(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
                }
                buildOverview(false);
            }
            updateDatabasePanelButtonStates();
        }
    }//GEN-LAST:event_btnCreateCubeActionPerformed

    private void btnCreateDimensionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCreateDimensionActionPerformed
        createDimension();
    }//GEN-LAST:event_btnCreateDimensionActionPerformed

    private void btnDeleteDimensionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteDimensionActionPerformed
        deleteSelectedDimension();
    }//GEN-LAST:event_btnDeleteDimensionActionPerformed

    private void btnRenameDimensionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRenameDimensionActionPerformed
        editDimensionName();
    }//GEN-LAST:event_btnRenameDimensionActionPerformed

    private void btnDeleteCubeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteCubeActionPerformed
        deleteSelectedCube();
    }//GEN-LAST:event_btnDeleteCubeActionPerformed

    private void treeCubesValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_treeCubesValueChanged
        TreePath path = treeCubes.getSelectionPath();
        if (path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
            if (node instanceof CubeTreeNode) {
                editCubeNode = (CubeTreeNode)node;
            }
        }
        updateDatabasePanelButtonStates();
    }//GEN-LAST:event_treeCubesValueChanged

    private void treeCubesKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_treeCubesKeyPressed
        if (treeCubes.isEditing() == false) {
            if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            } else if (evt.getKeyCode() == KeyEvent.VK_DELETE) {
                deleteSelectedCube();
            }
            updateDatabasePanelButtonStates();
        }
    }//GEN-LAST:event_treeCubesKeyPressed

    private void btnRenameCubeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRenameCubeActionPerformed
        editCubeName();
}//GEN-LAST:event_btnRenameCubeActionPerformed

    private void btnMoveUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMoveUpActionPerformed
        moveElementUp(false);
    }

    private void moveElementUp(boolean toFirst) {
        JTree               tree;
        DefaultTreeModel    treeModel;
        DefaultMutableTreeNode  root;
        
        if (editConsolidationMode) {
            tree = treeConsolidation;
            treeModel = consolidatedElementTreeModel;
            root = rootNodeConsolidatedElements;
        } else {
            tree = treeElements;
            treeModel = elementTreeModel;
            root = rootNodeElements;
        }

        int selRows[] = tree.getSelectionRows();
        ElementTreeNode node;
        IDimension dim = null;

        for (int i = 0; i < selRows.length; i++) {
            if (selRows[i] > 0) {
                node = (ElementTreeNode)treeModel.getChild(root, selRows[i]);
                treeModel.removeNodeFromParent(node);
                if (toFirst) {
                    treeModel.insertNodeInto(node, root, i);
                } else {
                    treeModel.insertNodeInto(node, root, selRows[i] - 1);
                }
                IElement elem = node.getElement();

                if (editConsolidationMode == false) {
                    if (toFirst) {
                        elem.move(i);
                    } else {
                        elem.move(selRows[i] - 1);
                    }
                    dim = node.getDimension();
                }
            }
        }

        if (editConsolidationMode == false) {
            //if (dim != null)
                //dim.reload(true); //TODO check if really necessary, because not implemented...
        }

        tree.clearSelection();
        treeModel.nodeStructureChanged(root);

        for (int i = 0; i < selRows.length; i++) {
            if (toFirst) {
                tree.addSelectionRow(i);
            } else {
                tree.addSelectionRow(selRows[i] - 1);
            }
        }

        updateDimensionPanelButtonStates();
    }//GEN-LAST:event_btnMoveUpActionPerformed

    private void btnMoveRightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMoveRightActionPerformed
        addElementsToConsolidated();
        updateDimensionPanelButtonStates();
    }//GEN-LAST:event_btnMoveRightActionPerformed

    private void treeElementsValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_treeElementsValueChanged
        TreePath path = treeElements.getSelectionPath();
        if (path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
            if (node instanceof ElementTreeNode) {
                if (editConsolidationMode == false) {
                    editElementNode = (ElementTreeNode)node;
                    fillConsolidatedElementList(editElementNode);
                }
            }
            updateDimensionPanelButtonStates();
        }
    }//GEN-LAST:event_treeElementsValueChanged

    private void treeElementsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_treeElementsMouseClicked
        if (evt.getClickCount() == 2) {
            if (editConsolidationMode == false) {
                activateConsolidationMode();
            } else {
                addElementsToConsolidated();
            }
       }

       updateDimensionPanelButtonStates();
    }//GEN-LAST:event_treeElementsMouseClicked

    private void btnElementNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnElementNewActionPerformed
        createElement();
        updateDimensionPanelButtonStates();
    }//GEN-LAST:event_btnElementNewActionPerformed

    private void btnElementDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnElementDeleteActionPerformed
        deleteSelectedElement();
        updateDimensionPanelButtonStates();
    }//GEN-LAST:event_btnElementDeleteActionPerformed

    private void btnElementRenameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnElementRenameActionPerformed
        editElementName();
        updateDimensionPanelButtonStates();
    }//GEN-LAST:event_btnElementRenameActionPerformed

    private void btnMoveLeftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMoveLeftActionPerformed
        removeSelectedFromConsolidation();
        updateDimensionPanelButtonStates();
    }//GEN-LAST:event_btnMoveLeftActionPerformed

    private void btnMoveDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMoveDownActionPerformed
        moveElementDown(false);
    }//GEN-LAST:event_btnMoveDownActionPerformed

    private void moveElementDown(boolean toLast) {
        JTree               tree;
        DefaultTreeModel    treeModel;
        DefaultMutableTreeNode  root;

        if (editConsolidationMode) {
            tree = treeConsolidation;
            treeModel = consolidatedElementTreeModel;
            root = rootNodeConsolidatedElements;
        } else {
            tree = treeElements;
            treeModel = elementTreeModel;
            root = rootNodeElements;
        }

        int selRows[] = tree.getSelectionRows();
        ElementTreeNode node;
        IDimension dim = null;

        for (int i = selRows.length - 1; i >= 0; i--) {
            node = (ElementTreeNode)treeModel.getChild(root, selRows[i]);
            treeModel.removeNodeFromParent(node);
            if (toLast)
                treeModel.insertNodeInto(node, root, tree.getRowCount() - (selRows.length - 1 - i));
            else
                treeModel.insertNodeInto(node, root, selRows[i] + 1);

            if (editConsolidationMode == false) {
                IElement elem = node.getElement();
                if (toLast)
                    elem.move(tree.getRowCount() - 1 - (selRows.length - 1 - i));
                else
                    elem.move(selRows[i] + 1);
                dim = node.getDimension();
            }
        }

        if (editConsolidationMode == false) {
            //if (dim != null)
            //    dim.reload(true); //TODO check if necessary!
        }

        tree.clearSelection();
        treeModel.nodeStructureChanged(root);

        for (int i = 0; i < selRows.length; i++) {
            if (toLast)
                tree.addSelectionRow(tree.getRowCount() - selRows.length + i);
            else
                tree.addSelectionRow(selRows[i] + 1);
        }

        updateDimensionPanelButtonStates();
    }

    private void btnConsolidationCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConsolidationCancelActionPerformed
        deactivateConsolidationMode();
        treeConsolidation.removeAll();
        fillConsolidatedElementList(editElementConsolidationNode);
        updateDimensionPanelButtonStates();
}//GEN-LAST:event_btnConsolidationCancelActionPerformed

    private void btnConsolidationOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConsolidationOKActionPerformed

        deactivateConsolidationMode();

        int             childCount = elementTreeModel.getChildCount(rootNodeConsolidatedElements);
        IConsolidation   elemCons[] = new IConsolidation[childCount];
        IDimension       dim = editElementConsolidationNode.getDimension();

        try {
            for (int i = 0; i < childCount; i++) {
                ElementTreeNode node = (ElementTreeNode)rootNodeConsolidatedElements.getChildAt(i);
                elemCons[i] = dim.newConsolidation(editElementConsolidationNode.getElement(), node.getElement(), 1.0);
            }

            editElementConsolidationNode.getDimension().removeConsolidations(new IElement[]{editElementConsolidationNode.getElement()});
            editElementConsolidationNode.getDimension().updateConsolidations(elemCons);
            IElement updatedElement = editElementConsolidationNode.getDimension().getElementByName(editElementConsolidationNode.getElement().getName(), false);
            editElementConsolidationNode.setElement(updatedElement);
            elementTreeModel.nodeChanged(editElementConsolidationNode);
            //if (childCount == 0) {
            //    editElementConsolidationNode.getDimension().updateElementsType(new IElement[]{editElementConsolidationNode.getElement()},IElement.ElementType.ELEMENT_NUMERIC);
            //}
        } catch (PaloJException pe) {
            JOptionPane.showMessageDialog(this, pe.getMessage(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
        } catch (PaloException pe) {
            JOptionPane.showMessageDialog(this, pe.getDescription() + ", " + pe.getReason(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
        }
        updateDimensionPanelButtonStates();
}//GEN-LAST:event_btnConsolidationOKActionPerformed

    private void treeConsolidationMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_treeConsolidationMouseClicked
        if (evt.getClickCount() == 2) {
            if (editConsolidationMode == true) {
                removeSelectedFromConsolidation();
            }
        }
        updateDimensionPanelButtonStates();
    }//GEN-LAST:event_treeConsolidationMouseClicked

    private void btnSortUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSortUpActionPerformed
        sortList(true);
        updateDimensionPanelButtonStates();
    }//GEN-LAST:event_btnSortUpActionPerformed

    private void btnSortDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSortDownActionPerformed
        sortList(false);
        updateDimensionPanelButtonStates();
    }//GEN-LAST:event_btnSortDownActionPerformed

    
    private IDimension getAttributeDimension(AttributesTreeNode node) {
       IDatabase database = node.getDatabase();
       return database.getDimensionByName("#_"+node.getDimension().getName()+"_");
    }

    private ICube getAttributeCube(DimensionTreeNode node) {
        IDatabase database = node.getDatabase();
        return database.getCubeByName("#_"+node.getDimension().getName());
    }
     


    private void btnNoSortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNoSortActionPerformed
        TreePath path = treeOverview.getSelectionPath();
        if (path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
            if (node instanceof DimensionTreeNode) {
                fillElementList(((DimensionTreeNode)node).getDimension());
            } else if (node instanceof AttributesTreeNode) {
                fillElementList(getAttributeDimension((AttributesTreeNode)node));
            }
        }
        updateDimensionPanelButtonStates();
    }//GEN-LAST:event_btnNoSortActionPerformed

    private void treeElementsKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_treeElementsKeyPressed
        if (treeElements.isEditing() == false && editConsolidationMode == false) {
            if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                createElement();
            } else if (evt.getKeyCode() == KeyEvent.VK_DELETE) {
                deleteSelectedElement();
            }
            updateDimensionPanelButtonStates();
        }
    }//GEN-LAST:event_treeElementsKeyPressed

    private void treeConsolidationKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_treeConsolidationKeyPressed
        if (editConsolidationMode == true) {
            if (evt.getKeyCode() == KeyEvent.VK_DELETE) {
                removeSelectedFromConsolidation();
            }
            updateDimensionPanelButtonStates();
        }
    }//GEN-LAST:event_treeConsolidationKeyPressed

    private void treeElementsFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_treeElementsFocusGained
        updateDimensionPanelButtonStates();
    }//GEN-LAST:event_treeElementsFocusGained

    private void treeConsolidationFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_treeConsolidationFocusGained
        updateDimensionPanelButtonStates();
    }//GEN-LAST:event_treeConsolidationFocusGained

    private void treeElementsFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_treeElementsFocusLost
        updateDimensionPanelButtonStates();
    }//GEN-LAST:event_treeElementsFocusLost

    private void btnHierarchyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHierarchyActionPerformed
        showDimensionPanelFlatHierarchy();
    }//GEN-LAST:event_btnHierarchyActionPerformed

    private void btnFlatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFlatActionPerformed
        showDimensionPanelFlat();
}//GEN-LAST:event_btnFlatActionPerformed

    private void btnExpandActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExpandActionPerformed
        expand(treeHierarchy);
}//GEN-LAST:event_btnExpandActionPerformed

    private void btnCollapseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCollapseActionPerformed
        collapse(treeHierarchy);
}//GEN-LAST:event_btnCollapseActionPerformed

    private void btnCollapseAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCollapseAllActionPerformed
        expandAll(treeHierarchy, false);
}//GEN-LAST:event_btnCollapseAllActionPerformed

    private void btnExpandAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExpandAllActionPerformed
        expandAll(treeHierarchy, true);
}//GEN-LAST:event_btnExpandAllActionPerformed

    private void btnLevel1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLevel1ActionPerformed
        collapseToLevel(treeHierarchy, 1);
}//GEN-LAST:event_btnLevel1ActionPerformed

    private void btnLevel2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLevel2ActionPerformed
        expandToLevel(treeHierarchy, 1);
        collapseToLevel(treeHierarchy, 2);
    }//GEN-LAST:event_btnLevel2ActionPerformed

    private void btnLevel3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLevel3ActionPerformed
        expandToLevel(treeHierarchy, 2);
        collapseToLevel(treeHierarchy, 3);
    }//GEN-LAST:event_btnLevel3ActionPerformed

    private void btnLevel4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLevel4ActionPerformed
        expandToLevel(treeHierarchy, 3);
        collapseToLevel(treeHierarchy, 4);
    }//GEN-LAST:event_btnLevel4ActionPerformed

    private void btnLevel5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLevel5ActionPerformed
        expandToLevel(treeHierarchy, 4);
        collapseToLevel(treeHierarchy, 5);
    }//GEN-LAST:event_btnLevel5ActionPerformed

    private void btnLevel6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLevel6ActionPerformed
        expandToLevel(treeHierarchy, 5);
        collapseToLevel(treeHierarchy, 6);
    }//GEN-LAST:event_btnLevel6ActionPerformed

    private void btnLevel7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLevel7ActionPerformed
        expandToLevel(treeHierarchy, 6);
        collapseToLevel(treeHierarchy, 7);
    }//GEN-LAST:event_btnLevel7ActionPerformed

    private void btnLevel8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLevel8ActionPerformed
        expandToLevel(treeHierarchy, 7);
        collapseToLevel(treeHierarchy, 8);
    }//GEN-LAST:event_btnLevel8ActionPerformed

    private void btnDatabaseNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDatabaseNewActionPerformed
        createDatabase();
    }//GEN-LAST:event_btnDatabaseNewActionPerformed

    private void btnDatabaseDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDatabaseDeleteActionPerformed
        deleteSelectedDatabase();
    }//GEN-LAST:event_btnDatabaseDeleteActionPerformed

    private void treeDatabasesValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_treeDatabasesValueChanged
        TreePath path = treeDatabases.getSelectionPath();
        if (path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
            if (node instanceof DatabaseTreeNode) {
                editDatabaseNode = (DatabaseTreeNode)node;
            }
        }
        updateServerPanelButtonStates();
    }//GEN-LAST:event_treeDatabasesValueChanged

    private void treeDatabasesFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_treeDatabasesFocusGained
        updateServerPanelButtonStates();
    }//GEN-LAST:event_treeDatabasesFocusGained

    private void treeDatabasesFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_treeDatabasesFocusLost
        updateServerPanelButtonStates();
    }//GEN-LAST:event_treeDatabasesFocusLost

    private void treeDimensionsFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_treeDimensionsFocusGained
        updateDatabasePanelButtonStates();
    }//GEN-LAST:event_treeDimensionsFocusGained

    private void treeDimensionsFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_treeDimensionsFocusLost
        updateDatabasePanelButtonStates();
    }//GEN-LAST:event_treeDimensionsFocusLost

    private void treeCubesFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_treeCubesFocusGained
        updateDatabasePanelButtonStates();
    }//GEN-LAST:event_treeCubesFocusGained

    private void treeCubesFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_treeCubesFocusLost
        updateDatabasePanelButtonStates();
    }//GEN-LAST:event_treeCubesFocusLost

    private void btnConnectionNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConnectionNewActionPerformed
        ConnectionEditorDialog editor = new ConnectionEditorDialog(this, true, context);
        editor.setLocationRelativeTo(this);
        editor.setVisible(true);
        if (editor.getModalResult() == JOptionPane.OK_OPTION) {
            synchronized(connectionHandler) {
                ArrayList<ConnectionInfo> connections;
                connections = connectionHandler.getConnections();
                ConnectionInfo connectionInfo = null;
                for (int i = 0; i < connections.size() && connectionInfo == null; i++) {
                    if (connections.get(i).getName().equalsIgnoreCase(editor.txtName.getText())) {
                        connectionInfo = connections.get(i);
                        JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Connection_name_already_used."), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                if (connectionInfo == null) {
                    connectionInfo = new ConnectionInfo(context, connectionHandler, editor.txtName.getText(), "", "", "", "", ConnectionInfo.TYPE_HTTP, false);
                    connections.add(connectionInfo);
//                    connectionInfo.addConnectionChangeListener(this); TODO
                }

                connectionInfo.setHost(editor.txtHost.getText());
                try {
                    connectionInfo.setPort(editor.txtPort.getText());
                } catch (NumberFormatException e) {}
                connectionInfo.setUsername(editor.txtUsername.getText());
                connectionInfo.setPassword(String.valueOf(editor.txtPassword.getPassword()));
                connectionInfo.setAutoLogin(editor.chkAutoLogin.isSelected());
                if (editor.comboDatabaseType.getSelectedIndex() == 0)
                    connectionInfo.setType(ConnectionInfo.TYPE_HTTP);
                else
                    connectionInfo.setType(ConnectionInfo.TYPE_XMLA);

                connectionHandler.saveConfig();
            }
            buildOverview(false);
        }
    }//GEN-LAST:event_btnConnectionNewActionPerformed

    private void btnConnectionDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConnectionDeleteActionPerformed

        DefaultMutableTreeNode node = getSelected(treeOverview);
        if (node instanceof ConnectionTreeNode) {
            synchronized(connectionHandler) {
                ConnectionInfo connectionInfo = ((ConnectionTreeNode)node).getConnection();
                ArrayList<ConnectionInfo> connections = connectionHandler.getConnections();
//                 connectionInfo.removeConnectionChangeListener(this); TODO
                connections.remove(connectionInfo);
                connectionInfo.setState(ConnectionState.Disconnected);
                connectionHandler.saveConfig();
            }
            buildOverview(false);
        }

    }//GEN-LAST:event_btnConnectionDeleteActionPerformed

    private void btnConnectionEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConnectionEditActionPerformed

        DefaultMutableTreeNode node = getSelected(treeOverview);
        if (node instanceof ConnectionTreeNode) {
            ConnectionInfo connectionInfo = ((ConnectionTreeNode)node).getConnection();
            ConnectionEditorDialog editor = new ConnectionEditorDialog(this, true, context);
            if (connectionInfo.getType() == ConnectionInfo.TYPE_HTTP)
                editor.comboDatabaseType.setSelectedIndex(0);
            else
                editor.comboDatabaseType.setSelectedIndex(1);
            editor.txtName.setText(connectionInfo.getName());
            editor.txtName.setEditable(false);
            editor.txtHost.setText(connectionInfo.getHost());
            editor.txtPort.setText(connectionInfo.getPort());
            editor.txtUsername.setText(connectionInfo.getUsername());
            editor.txtPassword.setText(connectionInfo.getPassword());
            editor.chkAutoLogin.setSelected(connectionInfo.isAutoLogin());
            editor.setLocationRelativeTo(this);
            editor.setVisible(true);
            if (editor.getModalResult() == JOptionPane.OK_OPTION) {
                connectionInfo.setHost(editor.txtHost.getText());
                try {
                    connectionInfo.setPort(editor.txtPort.getText());
                } catch (NumberFormatException e) {}
                connectionInfo.setUsername(editor.txtUsername.getText());
                connectionInfo.setPassword(String.valueOf(editor.txtPassword.getPassword()));
                if (editor.comboDatabaseType.getSelectedIndex() == 0)
                    connectionInfo.setType(ConnectionInfo.TYPE_HTTP);
                else
                    connectionInfo.setType(ConnectionInfo.TYPE_XMLA);
                connectionInfo.setAutoLogin(editor.chkAutoLogin.isSelected());
                connectionHandler.saveConfig();
                buildOverview(false);
            }
        }
    }//GEN-LAST:event_btnConnectionEditActionPerformed

    private void btnConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConnectActionPerformed
        DefaultMutableTreeNode node = getSelected(treeOverview);
        if (node instanceof ConnectionTreeNode) {
            ConnectionTreeNode conNode = (ConnectionTreeNode)node;
            ConnectionInfo con = conNode.getConnection();
            con.connect(this);
            buildOverview(false);
        }
    }//GEN-LAST:event_btnConnectActionPerformed

    private void btnDisconnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDisconnectActionPerformed
        DefaultMutableTreeNode node = getSelected(treeOverview);
        if (node instanceof ConnectionTreeNode) {
            ConnectionTreeNode conNode = (ConnectionTreeNode)node;
            ConnectionInfo con = conNode.getConnection();
            con.setState(ConnectionState.Disconnected);
            buildOverview(false);
        }
    }//GEN-LAST:event_btnDisconnectActionPerformed

    private void btnRuleUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRuleUpActionPerformed
        moveRule(true);
    }//GEN-LAST:event_btnRuleUpActionPerformed

    private void btnRuleDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRuleDownActionPerformed
        moveRule(false);
    }//GEN-LAST:event_btnRuleDownActionPerformed

    private void btnNewRuleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewRuleActionPerformed
        DefaultMutableTreeNode node = getSelected(treeOverview);
        if (node instanceof CubeRulesTreeNode) {
            CubeRulesTreeNode cubeNode = (CubeRulesTreeNode)node;
            IRule rule = cubeNode.newRule();

            Date lastChange = new Date(rule.getTimestamp());

            DefaultTableModel model = (DefaultTableModel)tableRules.getModel();
            Object row[] = new Object[] {
                    (Object)rule.getDefinition(),
                    (Object)rule.getComment(),
                    (Object)DateFormat.getInstance().format(lastChange)};
            model.addRow(row);

            tableRules.setRowSelectionInterval(model.getRowCount() - 1, model.getRowCount() - 1);
        }
    }//GEN-LAST:event_btnNewRuleActionPerformed

    private void btnUpdateRuleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateRuleActionPerformed
        int selRow = tableRules.getSelectedRow();
        if (selRow == -1)
            return;

        DefaultMutableTreeNode node = getSelected(treeOverview);
        if (node instanceof CubeRulesTreeNode) {
            CubeRulesTreeNode cubeNode = (CubeRulesTreeNode)node;
            IRule rule = cubeNode.updateRule(selRow,txtRule.getText(),txtRuleComment.getText());

            try {
                Date lastChange = new Date(rule.getTimestamp());
                txtError.setText("");
                DefaultTableModel model = (DefaultTableModel)tableRules.getModel();
                model.setValueAt(rule.getDefinition(), selRow, 0);
                model.setValueAt(rule.getComment(), selRow, 1);
                model.setValueAt(DateFormat.getInstance().format(lastChange), selRow, 2);
                //update rules on server. we have to completly write anew, since we cannot change order otherwise
                cubeNode.getCube().removeRules();
                //IRule[] serverRules = cubeNode.getCube().getRules();
                for (IRule r : cubeNode.getRules()) {
                    cubeNode.getCube().addRule(r.getDefinition(), r.isActive(), r.getExternalIdentifier(),r.getComment());
                }
                //update ids of rule proxys to saved state
                for (CubeRulesTreeNode.RuleProxy r : cubeNode.getRules()) {
                    r.setIdentifier(1);
                }

            } catch (PaloJException pe) {
                txtError.setText(pe.getMessage());
                txtError.setForeground(new Color(255, 0, 0));
            } catch (PaloException pe) {
                txtError.setText(pe.getReason());
                txtError.setForeground(new Color(255, 0, 0));
            }
        }
        updateRulesPanelButtonStates();
    }//GEN-LAST:event_btnUpdateRuleActionPerformed

    private void btnDeleteRuleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteRuleActionPerformed
        DefaultTableModel model = (DefaultTableModel)tableRules.getModel();
        int selRow = tableRules.getSelectedRow();
        if (selRow == -1)
            return;

        DefaultMutableTreeNode node = getSelected(treeOverview);
        if (node instanceof CubeRulesTreeNode) {
            CubeRulesTreeNode cubeNode = (CubeRulesTreeNode)node;
            IRule rule = cubeNode.getRules().remove(selRow);
            if (rule.getIdentifier() > 0) {
                cubeNode.getCube().removeRules();
                //IRule[] serverRules = cubeNode.getCube().getRules();
                for (IRule r : cubeNode.getRules()) {
                    cubeNode.getCube().addRule(r.getDefinition(), r.isActive(), r.getExternalIdentifier(),r.getComment());
                }
                //update ids of rule proxys to saved state
                for (CubeRulesTreeNode.RuleProxy r : cubeNode.getRules()) {
                    r.setIdentifier(1);
                }
            }

            model.removeRow(selRow);
            txtRule.setText("");
            txtRuleComment.setText("");
            txtError.setText("");
        }

        updateRulesPanelButtonStates();
    }//GEN-LAST:event_btnDeleteRuleActionPerformed

    private void btnCheckRuleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCheckRuleActionPerformed
        int selRow = tableRules.getSelectedRow();
        if (selRow == -1)
            return;

        DefaultMutableTreeNode node = getSelected(treeOverview);
        if (node instanceof CubeRulesTreeNode) {
            CubeRulesTreeNode cubeNode = (CubeRulesTreeNode)node;
            //IRule rule = cubeNode.getRules().get(selRow);
            try {
                //cubeNode.getCube().getDatabase().parseRule(rule.getCube(), txtRule.getText(), "");
                String result = cubeNode.getCube().parseRule(txtRule.getText());
                //cubeNode.getCube().addRule(rule.getDefinition(), rule.isActive(), rule.getExternalIdentifier(),rule.getComment());
                //IRule[] cubeRules = cubeNode.getCube().getRules();
                //IRule tempRule = cubeRules[cubeRules.length-1];
                //cubeNode.getCube().removeRules(new IRule[]{tempRule});
                txtError.setText("Rule valid"); 
                //txtError.setText("Check not implemented yet!"); //TODO implement check
                txtError.setForeground(new Color(0, 255, 0));
            } catch (PaloJException pe) {
                txtError.setText("Invalid: "+pe.getMessage());
                txtError.setForeground(new Color(255, 0, 0));
            } catch (PaloException pe) {
                txtError.setText("Invalid: "+pe.getMessage());
                txtError.setForeground(new Color(255, 0, 0));
            }
        }
}//GEN-LAST:event_btnCheckRuleActionPerformed

    private void treeGroupsValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_treeGroupsValueChanged
        TreePath path = treeGroups.getSelectionPath();
        if (path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
            if (node instanceof GroupTreeNode) {
                editGroupNode = (GroupTreeNode)node;
            }
        }

        updateGroupsPanelButtonStates();
}//GEN-LAST:event_treeGroupsValueChanged

    private void treeGroupsFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_treeGroupsFocusGained
        updateGroupsPanelButtonStates();
}//GEN-LAST:event_treeGroupsFocusGained

    private void treeGroupsFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_treeGroupsFocusLost
        updateGroupsPanelButtonStates();
}//GEN-LAST:event_treeGroupsFocusLost

    private void btnGroupNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGroupNewActionPerformed
        createGroup();
}//GEN-LAST:event_btnGroupNewActionPerformed

    private void btnGroupDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGroupDeleteActionPerformed
        deleteSelectedGroup();
}//GEN-LAST:event_btnGroupDeleteActionPerformed

    private void btnGroupEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGroupEditActionPerformed
        editGroupName();
}//GEN-LAST:event_btnGroupEditActionPerformed

    private void treeUsersValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_treeUsersValueChanged
        TreePath path = treeUsers.getSelectionPath();
        if (path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
            if (node instanceof UserTreeNode) {
                editUserNode = (UserTreeNode)node;
            }
        }
        updateUsersPanelButtonStates();
}//GEN-LAST:event_treeUsersValueChanged

    private void treeUsersFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_treeUsersFocusGained
        updateUsersPanelButtonStates();
}//GEN-LAST:event_treeUsersFocusGained

    private void treeUsersFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_treeUsersFocusLost
        updateUsersPanelButtonStates();
}//GEN-LAST:event_treeUsersFocusLost

    private void btnUserNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUserNewActionPerformed
        createUser();
}//GEN-LAST:event_btnUserNewActionPerformed

    private void btnUserDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUserDeleteActionPerformed
        deleteSelectedUser();
}//GEN-LAST:event_btnUserDeleteActionPerformed

    private void btnUserEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUserEditActionPerformed
        editUserName();
}//GEN-LAST:event_btnUserEditActionPerformed

    private void treeRolesValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_treeRolesValueChanged
        TreePath path = treeRoles.getSelectionPath();
        if (path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
            if (node instanceof RoleTreeNode) {
                editRoleNode = (RoleTreeNode)node;
            }
        }
        updateRolesPanelButtonStates();
}//GEN-LAST:event_treeRolesValueChanged

    private void treeRolesFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_treeRolesFocusGained
        updateRolesPanelButtonStates();
}//GEN-LAST:event_treeRolesFocusGained

    private void treeRolesFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_treeRolesFocusLost
        updateRolesPanelButtonStates();

}//GEN-LAST:event_treeRolesFocusLost

    private void btnRoleNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRoleNewActionPerformed
        createRole();
}//GEN-LAST:event_btnRoleNewActionPerformed

    private void btnRoleDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRoleDeleteActionPerformed
        deleteSelectedRole();
}//GEN-LAST:event_btnRoleDeleteActionPerformed

    private void btnRoleEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRoleEditActionPerformed
        editRoleName();
}//GEN-LAST:event_btnRoleEditActionPerformed

    private void treeDatabasesKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_treeDatabasesKeyPressed
        /*
        if (treeDatabases.isEditing() == false) {
            if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                createDatabase();
            } else if (evt.getKeyCode() == KeyEvent.VK_DELETE) {
                deleteSelectedDatabase();
            }
            updateServerPanelButtonStates();
        }
         * 
         */
    }//GEN-LAST:event_treeDatabasesKeyPressed

    private void treeGroupsKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_treeGroupsKeyPressed
        if (treeGroups.isEditing() == false) {
            if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                createGroup();
            } else if (evt.getKeyCode() == KeyEvent.VK_DELETE) {
                deleteSelectedGroup();
            }
            updateGroupsPanelButtonStates();
        }
    }//GEN-LAST:event_treeGroupsKeyPressed

    private void treeUsersKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_treeUsersKeyPressed
        if (treeUsers.isEditing() == false) {
            if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                createUser();
            } else if (evt.getKeyCode() == KeyEvent.VK_DELETE) {
                deleteSelectedUser();
            }
            updateUsersPanelButtonStates();
        }
    }//GEN-LAST:event_treeUsersKeyPressed

    private void treeRolesKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_treeRolesKeyPressed
        if (treeRoles.isEditing() == false) {
            if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                createRole();
            } else if (evt.getKeyCode() == KeyEvent.VK_DELETE) {
                deleteSelectedRole();
            }
            updateRolesPanelButtonStates();
        }
    }//GEN-LAST:event_treeRolesKeyPressed

    private void btnPasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPasswordActionPerformed
        DefaultMutableTreeNode node = getSelected(treeOverview);
        if (node instanceof UserTreeNode) {
            UserTreeNode userNode = (UserTreeNode)node;
            PasswordDialog passwordDialog = new PasswordDialog(this, true);
    
            passwordDialog.setPassword(userNode.getUser().getPassword());
            passwordDialog.setVisible(true);
        
            if (passwordDialog.getModalResult() == JOptionPane.OK_OPTION) {
                userNode.getUser().setPassword(passwordDialog.getNewPassword());
            }
        }
    }//GEN-LAST:event_btnPasswordActionPerformed

    private void btnElementConsolidateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnElementConsolidateActionPerformed
        activateConsolidationMode();
}//GEN-LAST:event_btnElementConsolidateActionPerformed

    private void btnElementNumericActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnElementNumericActionPerformed
        setElementType(IElement.ElementType.ELEMENT_NUMERIC);
}//GEN-LAST:event_btnElementNumericActionPerformed

    private void btnElementStringActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnElementStringActionPerformed
        setElementType(IElement.ElementType.ELEMENT_STRING);
}//GEN-LAST:event_btnElementStringActionPerformed

    private void treeAttributesValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_treeAttributesValueChanged
        TreePath path = treeAttributes.getSelectionPath();
        if (path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
            if (node instanceof AttributeTreeNode) {
                editAttributeNode = (AttributeTreeNode)node;
            }
        }
        updateAttributesPanelButtonStates();
}//GEN-LAST:event_treeAttributesValueChanged

    private void treeAttributesFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_treeAttributesFocusGained
        updateAttributesPanelButtonStates();
}//GEN-LAST:event_treeAttributesFocusGained

    private void treeAttributesFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_treeAttributesFocusLost
        updateAttributesPanelButtonStates();
}//GEN-LAST:event_treeAttributesFocusLost

    private void treeAttributesKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_treeAttributesKeyPressed
        if (treeAttributes.isEditing() == false) {
            if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                createAttribute();
            } else if (evt.getKeyCode() == KeyEvent.VK_DELETE) {
                deleteSelectedAttribute();
            }
            updateAttributesPanelButtonStates();
        }

}//GEN-LAST:event_treeAttributesKeyPressed

    private void btnAttributeNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAttributeNewActionPerformed
        createAttribute();
}//GEN-LAST:event_btnAttributeNewActionPerformed

    private void btnAttributeDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAttributeDeleteActionPerformed
        deleteSelectedAttribute();
}//GEN-LAST:event_btnAttributeDeleteActionPerformed

    private void btnAttributeEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAttributeEditActionPerformed
        editAttributeName();
}//GEN-LAST:event_btnAttributeEditActionPerformed

    private void btnAttributeNumericActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAttributeNumericActionPerformed
        setAttributeType(IElement.ElementType.ELEMENT_NUMERIC);
        buildOverview(false);
}//GEN-LAST:event_btnAttributeNumericActionPerformed

    private void btnAttributeStringActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAttributeStringActionPerformed
        setAttributeType(IElement.ElementType.ELEMENT_STRING);
        buildOverview(false);
}//GEN-LAST:event_btnAttributeStringActionPerformed

    private void btnEditAttributesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditAttributesActionPerformed
        if (!isReadOnly()) {
            DefaultMutableTreeNode node = getSelected(treeOverview);
            if (node instanceof AttributeTreeNode) {
                DefaultMutableTreeNode cnode = node;
                IDimension   dim = null;
                IDatabase database = null;
                DimensionTreeNode dimNode = null;

                while (!(cnode instanceof ConnectionTreeNode) && cnode != null) {
                    cnode = (DefaultMutableTreeNode)cnode.getParent();
                    if (cnode instanceof DimensionTreeNode) {
                        dimNode = ((DimensionTreeNode)cnode);
                        dim = dimNode.getDimension();
                        database =  dimNode.getDatabase();
                    }
                }

                if (cnode == null || dim == null)
                    return;

                ConnectionTreeNode connNode = (ConnectionTreeNode)cnode;

                PalOOCaView view = new PalOOCaView(context);

                view.showCube(connNode.getConnection(), database, getAttributeCube(dimNode));

                setVisible(false);
                dispose();
            }
        }
    }//GEN-LAST:event_btnEditAttributesActionPerformed

    private void btnEditDimensionRightsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditDimensionRightsActionPerformed
        DefaultMutableTreeNode node = getSelected(treeOverview);
        if (node instanceof DimensionRightsTreeNode) {
            DefaultMutableTreeNode cnode = node;
            IDimension   dim = null;
            IDatabase database = null;

            while (!(cnode instanceof ConnectionTreeNode) && cnode != null) {
                cnode = (DefaultMutableTreeNode)cnode.getParent();
                if (cnode instanceof DimensionTreeNode) {
                    dim = ((DimensionTreeNode)cnode).getDimension();
                    database = ((DimensionTreeNode)cnode).getDatabase();
                }
            }

            if (cnode == null || dim == null)
                return;

            ConnectionTreeNode connNode = (ConnectionTreeNode)cnode;

            PalOOCaView view = new PalOOCaView(context);
            ICube cube = database.getCubeByName("#_GROUP_DIMENSION_DATA_" + dim.getName());

            view.showCube(connNode.getConnection(),database, cube);

            setVisible(false);
            dispose();
        }    
}//GEN-LAST:event_btnEditDimensionRightsActionPerformed

    private void btnWeightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnWeightActionPerformed
        if (!isReadOnly()) {
            ConsolidationFactorDialog dialog = new ConsolidationFactorDialog(this, true);

            DefaultMutableTreeNode node = getSelected(treeHierarchy);
            if (node instanceof ElementTreeNode) {
                ElementTreeNode elementNode = (ElementTreeNode)node;
                dialog.setFactor(elementNode.getWeight());
                dialog.setVisible(true);

                if (dialog.getModalResult() == JOptionPane.OK_OPTION) {
                    try  {
                        ElementTreeNode parent = (ElementTreeNode)node.getParent();
                        int childCount = parent.getChildCount();
                        IConsolidation elemCons[] = new IConsolidation[childCount];
                        IDimension dim = parent.getDimension();

                        for (int i = 0; i < childCount; i++) {
                            ElementTreeNode child = (ElementTreeNode) parent.getChildAt(i);
                            if (child == elementNode) {
                                elemCons[i] = dim.newConsolidation(parent.getElement(),child.getElement(), dialog.getFactor());
                            } else {
                                elemCons[i] = dim.newConsolidation(parent.getElement(), child.getElement(), child.getWeight());
                            }
                        }

                        parent.getDimension().updateConsolidations(elemCons);
                        //elementNode.setElement(parent.getDimension().getElementByName(elementNode.getElement().getName(), false));

                        elementNode.setWeight(dialog.getFactor());
                        elementHierarchyTreeModel.nodeChanged(elementNode);
                    } catch (PaloJException pe) {
                        JOptionPane.showMessageDialog(this, pe.getMessage(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
                    } catch (PaloException pe) {
                        JOptionPane.showMessageDialog(this, pe.getDescription() + ", " + pe.getReason(), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Error"), JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
      }
}//GEN-LAST:event_btnWeightActionPerformed

    private void treeHierarchyValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_treeHierarchyValueChanged
        boolean enabled = false;

        DefaultMutableTreeNode node = getSelected(treeHierarchy);
        if (node instanceof ElementTreeNode) {
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
            if (parent instanceof ElementTreeNode) {
                enabled = true;
            }
        }

        btnWeight.setEnabled(enabled);
    }//GEN-LAST:event_treeHierarchyValueChanged

    private void btnDeleteCubeValuesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteCubeValuesActionPerformed
//            Database database = connectionHandler.getDatabase("localhost/KopievonBiker");
//            if ( database == null )
//                return ;
//
//            Cube cube = database.getCubeByName("Orders");
//            if ( cube == null )
//                return ;
//
//            String[] coordStrings = new String[8];
//            coordStrings[0] = new String ("All Years");
//            coordStrings[1] = new String ("All Customers");
//            coordStrings[2] = new String ("All Channels");
//            coordStrings[3] = new String ("All Orders");
//            coordStrings[4] = new String ("All Datatypes");
//            coordStrings[5] = new String ("Units");
//            coordStrings[6] = new String ("All Products");
//            coordStrings[7] = new String ("All Months");
//
//            for (int j = 1; j < 2000; j++) {
//
//                Element[] elementCoords = new Element[coordStrings.length];
//                Dimension[] dimensions = cube.getDimensions();
//
//                coordStrings[3] = new Integer(200000 + j).toString();
//
//                for (int i = 0; i < coordStrings.length; i++) {
//                    elementCoords[i] = dimensions[i].getElementByName(coordStrings[i]);
//                    if (elementCoords[i] == null)
//                        return;
//                }
//
//                Cell cell = cube.getCell(elementCoords);
//            }
        if (!isReadOnly()) {
            DefaultMutableTreeNode node = getSelected(treeOverview);

            if (node instanceof CubeTreeNode) {
                if (JOptionPane.showConfirmDialog(this,
                        java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("DeleteCubeValues"),
                        java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Confirmation"),
                        JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION)
                    return;
                CubeTreeNode cubeNode = (CubeTreeNode)node;
                cubeNode.getCube().clear();
            }
        }
    }//GEN-LAST:event_btnDeleteCubeValuesActionPerformed

    private void mnuElementAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuElementAddActionPerformed
        createElement();
    }//GEN-LAST:event_mnuElementAddActionPerformed

    private void mnuElementDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuElementDeleteActionPerformed
        deleteSelectedElement();
    }//GEN-LAST:event_mnuElementDeleteActionPerformed

    private void mnuElementRenameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuElementRenameActionPerformed
        editElementName();
    }//GEN-LAST:event_mnuElementRenameActionPerformed

    private void mnuElementConsolidateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuElementConsolidateActionPerformed
        activateConsolidationMode();
    }//GEN-LAST:event_mnuElementConsolidateActionPerformed

    private void mnuElementNumericActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuElementNumericActionPerformed
        setElementType(IElement.ElementType.ELEMENT_NUMERIC);
    }//GEN-LAST:event_mnuElementNumericActionPerformed

    private void mnuElementStringActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuElementStringActionPerformed
        setElementType(IElement.ElementType.ELEMENT_STRING);
    }//GEN-LAST:event_mnuElementStringActionPerformed

    private void mnuElementSelectAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuElementSelectAllActionPerformed
        treeElements.setSelectionInterval(0, treeElements.getRowCount());
    }//GEN-LAST:event_mnuElementSelectAllActionPerformed

    private void mnuElementMoveFirstActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuElementMoveFirstActionPerformed
        moveElementUp(true);
    }//GEN-LAST:event_mnuElementMoveFirstActionPerformed

    private void mnuElementMoveLastActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuElementMoveLastActionPerformed
        moveElementDown(true);
    }//GEN-LAST:event_mnuElementMoveLastActionPerformed

    private void mnuElementCountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuElementCountActionPerformed
        int count = treeElements.getRowCount();

        JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("NumberElements") + " " + count,
                java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Information"),
                JOptionPane.INFORMATION_MESSAGE);

    }//GEN-LAST:event_mnuElementCountActionPerformed

    private void mnuElementSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuElementSearchActionPerformed
            SearchPatternDialog searchDialog = new SearchPatternDialog(this, true);
            searchDialog.setVisible(true);
            if (searchDialog.getModalResult() == JOptionPane.OK_OPTION) {
                if (PaloDialogUtilities.searchForNode(treeElements, searchDialog.getSearchText(), null, rootNodeElements, false, true) == false) {
                    JOptionPane.showMessageDialog(this,
                            java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("SearchNotFound"),
                            java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Information"), JOptionPane.INFORMATION_MESSAGE);
                }
            }
        
    }//GEN-LAST:event_mnuElementSearchActionPerformed

    private void treeHierarchyMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_treeHierarchyMouseClicked
        // TODO add your handling code here:
        if (evt.getClickCount() > 1 && evt.isShiftDown()) {
            TreePath path = treeHierarchy.getSelectionPath();
            if (path != null) {
                 DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
                 if (node.getParent() != null && node.getParent() instanceof ElementTreeNode) {
                    btnWeightActionPerformed(null);
                 }
            }
        }
    }//GEN-LAST:event_treeHierarchyMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAttributeDelete;
    private javax.swing.JButton btnAttributeEdit;
    private javax.swing.JButton btnAttributeNew;
    private javax.swing.JToggleButton btnAttributeNumeric;
    private javax.swing.JToggleButton btnAttributeString;
    private javax.swing.JButton btnCheckRule;
    private javax.swing.JButton btnCollapse;
    private javax.swing.JButton btnCollapseAll;
    private javax.swing.JButton btnConnect;
    private javax.swing.JButton btnConnectionDelete;
    private javax.swing.JButton btnConnectionEdit;
    private javax.swing.JButton btnConnectionNew;
    private javax.swing.JButton btnConsolidationCancel;
    private javax.swing.JButton btnConsolidationOK;
    private javax.swing.JButton btnCreateCube;
    private javax.swing.JButton btnCreateDimension;
    private javax.swing.JButton btnDatabaseDelete;
    private javax.swing.JButton btnDatabaseNew;
    private javax.swing.JButton btnDeleteCube;
    private javax.swing.JButton btnDeleteCubeValues;
    private javax.swing.JButton btnDeleteDimension;
    private javax.swing.JButton btnDeleteRule;
    private javax.swing.JButton btnDisconnect;
    private javax.swing.JButton btnEditAttributes;
    private javax.swing.JButton btnEditDimensionRights;
    private javax.swing.JButton btnElementConsolidate;
    private javax.swing.JButton btnElementDelete;
    private javax.swing.JButton btnElementNew;
    private javax.swing.JToggleButton btnElementNumeric;
    private javax.swing.JButton btnElementRename;
    private javax.swing.JToggleButton btnElementString;
    private javax.swing.JButton btnExpand;
    private javax.swing.JButton btnExpandAll;
    private javax.swing.JButton btnFlat;
    private javax.swing.JButton btnGroupDelete;
    private javax.swing.JButton btnGroupEdit;
    private javax.swing.JButton btnGroupNew;
    private javax.swing.JButton btnHierarchy;
    private javax.swing.JButton btnLevel1;
    private javax.swing.JButton btnLevel2;
    private javax.swing.JButton btnLevel3;
    private javax.swing.JButton btnLevel4;
    private javax.swing.JButton btnLevel5;
    private javax.swing.JButton btnLevel6;
    private javax.swing.JButton btnLevel7;
    private javax.swing.JButton btnLevel8;
    private javax.swing.JButton btnMoveDown;
    private javax.swing.JButton btnMoveLeft;
    private javax.swing.JButton btnMoveRight;
    private javax.swing.JButton btnMoveUp;
    private javax.swing.JButton btnNewRule;
    private javax.swing.JToggleButton btnNoSort;
    private javax.swing.JButton btnOK;
    private javax.swing.JButton btnPassword;
    private javax.swing.JButton btnRenameCube;
    private javax.swing.JButton btnRenameDimension;
    private javax.swing.JButton btnRoleDelete;
    private javax.swing.JButton btnRoleEdit;
    private javax.swing.JButton btnRoleNew;
    private javax.swing.JButton btnRuleDown;
    private javax.swing.JButton btnRuleUp;
    private javax.swing.JToggleButton btnSortDown;
    private javax.swing.JToggleButton btnSortUp;
    private javax.swing.JButton btnUpdateRule;
    private javax.swing.JButton btnUserDelete;
    private javax.swing.JButton btnUserEdit;
    private javax.swing.JButton btnUserNew;
    private javax.swing.JButton btnWeight;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroupAttributeType;
    private javax.swing.ButtonGroup buttonGroupElementType;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JScrollPane jScrollPane13;
    private javax.swing.JScrollPane jScrollPane14;
    private javax.swing.JScrollPane jScrollPane15;
    private javax.swing.JScrollPane jScrollPane16;
    private javax.swing.JScrollPane jScrollPane17;
    private javax.swing.JScrollPane jScrollPane18;
    private javax.swing.JScrollPane jScrollPane19;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JLabel lblCellCount;
    private javax.swing.JLabel lblFilledCellCount;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JMenuItem mnuElementAdd;
    private javax.swing.JMenuItem mnuElementConsolidate;
    private javax.swing.JMenuItem mnuElementCount;
    private javax.swing.JMenuItem mnuElementDelete;
    private javax.swing.JMenuItem mnuElementMoveFirst;
    private javax.swing.JMenuItem mnuElementMoveLast;
    private javax.swing.JCheckBoxMenuItem mnuElementNumeric;
    private javax.swing.JMenuItem mnuElementRename;
    private javax.swing.JMenuItem mnuElementSearch;
    private javax.swing.JMenuItem mnuElementSelectAll;
    private javax.swing.JCheckBoxMenuItem mnuElementString;
    private javax.swing.JPanel panelAttribute;
    private javax.swing.JPanel panelAttributes;
    private javax.swing.JPanel panelCube;
    private javax.swing.JPanel panelCubeRights;
    private javax.swing.JPanel panelDatabase;
    private javax.swing.JPanel panelDimension;
    private javax.swing.JPanel panelDimensionHierarchy;
    private javax.swing.JPanel panelDimensionRights;
    private javax.swing.JPanel panelGroup;
    private javax.swing.JPanel panelGroups;
    private javax.swing.JPanel panelRole;
    private javax.swing.JPanel panelRoles;
    private javax.swing.JPanel panelRules;
    private javax.swing.JPanel panelServer;
    private javax.swing.JPanel panelUser;
    private javax.swing.JPanel panelUsers;
    private javax.swing.JPopupMenu pmElementsFlat;
    private javax.swing.JTable tableCubeRights;
    private javax.swing.JTable tableGroups;
    private javax.swing.JTable tableRights;
    private javax.swing.JTable tableRoles;
    private javax.swing.JTable tableRules;
    private javax.swing.JTree treeAttributes;
    private javax.swing.JTree treeConsolidation;
    private javax.swing.JTree treeCubes;
    private javax.swing.JTree treeDatabases;
    private javax.swing.JTree treeDimensions;
    private javax.swing.JTree treeElements;
    private javax.swing.JTree treeGroups;
    private javax.swing.JTree treeHierarchy;
    private javax.swing.JTree treeOverview;
    private javax.swing.JTree treeRoles;
    private javax.swing.JTree treeUsers;
    private javax.swing.JTextArea txtAreaDimensions;
    private javax.swing.JLabel txtError;
    private javax.swing.JTextArea txtRule;
    private javax.swing.JTextArea txtRuleComment;
    // End of variables declaration//GEN-END:variables
}
