/*
 * ElementBrowserDialog.java
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
 * Created on 12. September 2007, 21:59
 */
package org.palooca.dialogs;

import org.palooca.dialogs.nodes.ElementTreeNode;
import com.sun.star.uno.XComponentContext;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.Arrays;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import com.jedox.palojlib.interfaces.IAttribute;
import com.jedox.palojlib.interfaces.IElement;
import com.jedox.palojlib.interfaces.IDimension;
import javax.swing.JDialog;
import org.palooca.PaloLibUtil;
import org.palooca.network.ConnectionInfo;
import org.palooca.network.ConnectionState;

/**
 *
 * @author Andreas Schneider
 */
public class ElementBrowserDialog extends PaloDialogUtilities
        implements TreeExpansionListener {

    private int modalResult = JOptionPane.CANCEL_OPTION;
    private boolean flatMode = false;
    private Vector<TreePath> selectionPaths;

    public int getModalResult() {
        return modalResult;
    }

    public ElementBrowserDialog(java.awt.Dialog parent, boolean modal,
            XComponentContext context, 
            IDimension dimension, Vector<String[]> selection, IAttribute attribute) {
        super(parent, modal, context);

        initComponents();
        this.attribute = attribute;
        this.dimension = dimension;

        buildElementList(dimension, attribute);

        treeElements.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        treeElements.setCellRenderer(new TreeElementRenderer(attribute));

        prepareMenu(false);

        updateMoveButtons(treeElements, jListElements, jButtonMoveLeft, jButtonMoveRight, jButtonMoveUp, jButtonMoveDown);
        jListElements.setCellRenderer(new ElementListRenderer(attribute));

        cmbAlias.addItem(resourceBundle.getString("None"));

        IAttribute[]  attributes = dimension.getAttributes();

        int added = 1;
        int selectedIndex = 0;
        for (int i = 0; i < attributes.length; i++) {
            if (attributes[i].getType().equals(IElement.ElementType.ELEMENT_STRING)) {
                cmbAlias.addItem(attributes[i]);
                if (attribute != null && attributes[i].getName().equals(attribute.getName())) selectedIndex = added;
                added++;
            }
        }

        if (attribute != null)
            cmbAlias.setSelectedIndex(selectedIndex);

        setSelection(selection);

        treeElements.addTreeExpansionListener(this);            // register after setSelection !!

        setLocationRelativeTo(parent);
    }


    public void setSelection(Vector<String[]> selection) {

        treeElements.clearSelection();
        for (int i = 0; i < treeElements.getRowCount(); i++) {
            treeElements.collapseRow(i);
        }

        boolean irregular = false;

        if (selection.size() > 0) {
            TreePath[] paths = new TreePath[selection.size()];

            for (int i = 0; i < selection.size(); i++) {
                TreePath path = findByName(treeElements, selection.get(i));
                if (path != null) {
                    ElementTreeNode node = (ElementTreeNode) path.getLastPathComponent();
                    ListElement le = new ListElement(node, path);
                    elementModel.addElement(le);
                    if (treeElements.isPathSelected(path)) {
                        irregular = true;
                    }
                    if (path.getParentPath() != null) treeElements.expandPath(path.getParentPath());
                    paths[i] = path;
                }
            }

            treeElements.addSelectionPaths(paths);
        }

        int[] rows = treeElements.getSelectionRows();
        if (rows != null) {
            for (int i = 1; i < rows.length; i++) {
                if (rows[i] <= rows[i - 1]) {
                    irregular = true;
                    break;
                }
            }
        }

        if (irregular) {
            treeElements.clearSelection();
            for (int i = 0; i < treeElements.getRowCount(); i++) {
                treeElements.collapseRow(i);
            }
        } else {
            //elementModel.clear();
        }
    }

    public void getSelection(DimensionSubsetListItem dimItem) {
        
        dimItem.getSelectedElements().clear();
        dimItem.getSelectedElementObjects().clear();

        if (dimItem.getSubset() != null) {
            //TODO
        } else {
            if (jListElements.getModel().getSize() > 0) {
                ListElement el;

                for (int i = 0; i < elementModel.getSize(); i++) {
                    el = (ListElement) elementModel.get(i);
                    TreePath path = el.path;
                    dimItem.getSelectedElements().add(pathToStringArray(path));
                    dimItem.getSelectedElementObjects().add(el.getNode().getElement());
                }
            } else {
                for (int i = 0; i < treeElements.getRowCount(); i++) {
                    if (treeElements.isRowSelected(i)) {
                        TreePath path = treeElements.getPathForRow(i);
                        dimItem.getSelectedElements().add(pathToStringArray(path));
                        ElementTreeNode node = (ElementTreeNode)path.getLastPathComponent();
                        dimItem.getSelectedElementObjects().add(node.getElement());
                    }
                }
            }
        }

        return;
    }

    private void prepareMenu(boolean singleSelectionMode) {
        mnuSelectAll.setVisible(!singleSelectionMode);
        mnuDeselectAll.setVisible(!singleSelectionMode);
        mnuInvertSelection.setVisible(!singleSelectionMode);
        mnuSeparator1.setVisible(!singleSelectionMode);
    }

    private void preExpansionStateChange(boolean expand) {
        selectionPaths = new Vector<TreePath>();
        int cnt = treeElements.getRowCount();

        for (int i = 0; i < cnt; i++) {
            TreePath path = treeElements.getPathForRow(i);
            if (treeElements.isPathSelected(path)) {
                selectionPaths.add(path);
                if (expand) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                    for (int j = 0; j < node.getChildCount(); j++) {
                        selectionPaths.add(new TreePath(((DefaultMutableTreeNode) node.getChildAt(j)).getPath()));
                    }
                }
            }
        }

        treeElements.clearSelection();
    }

    private void postExpansionStateChange(boolean expand) {
        if (selectionPaths.size() != 0) { // this is done to speed up selection during event handling

            if (expand == false) {
                for (int i = selectionPaths.size() - 1; i >= 0; i--) {
                    if (treeElements.isVisible(selectionPaths.get(i)) == false) {
                        selectionPaths.remove(i);
                    }
                }
            }

            TreePath[] paths = new TreePath[selectionPaths.size()];

            for (int i = 0; i < selectionPaths.size(); i++) {
                paths[i] = selectionPaths.get(i);
            }
            selectionPaths.clear();
            treeElements.addSelectionPaths(paths);
        }

        selectionPaths = null;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pmElements = new javax.swing.JPopupMenu();
        mnuSelectAll = new javax.swing.JMenuItem();
        mnuDeselectAll = new javax.swing.JMenuItem();
        mnuInvertSelection = new javax.swing.JMenuItem();
        mnuSeparator1 = new javax.swing.JSeparator();
        mnuExpandAll = new javax.swing.JMenuItem();
        mnuCollapseAll = new javax.swing.JMenuItem();
        jScrollPane1 = new javax.swing.JScrollPane();
        treeElements = new javax.swing.JTree();
        btnOK = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        jButtonExpand = new javax.swing.JButton();
        jButtonCollapse = new javax.swing.JButton();
        jButtonExpandAll = new javax.swing.JButton();
        jButtonCollapseAll = new javax.swing.JButton();
        jTextFieldSearch = new javax.swing.JTextField();
        jButtonSearch = new javax.swing.JButton();
        jButtonSelectAll = new javax.swing.JButton();
        jButtonSelectChildren = new javax.swing.JButton();
        jButtonSelect1 = new javax.swing.JButton();
        jButtonSelect2 = new javax.swing.JButton();
        jButtonSelect3 = new javax.swing.JButton();
        jButtonSelect4 = new javax.swing.JButton();
        jButtonSelect5 = new javax.swing.JButton();
        jButtonSelectB = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jListElements = new javax.swing.JList();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jButtonMoveRight = new javax.swing.JButton();
        jButtonMoveLeft = new javax.swing.JButton();
        jButtonSortUp = new javax.swing.JButton();
        jButtonSortDown = new javax.swing.JButton();
        jButtonClear = new javax.swing.JButton();
        jButtonMoveUp = new javax.swing.JButton();
        jButtonMoveDown = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        cmbAlias = new javax.swing.JComboBox();
        btnInvertSelection = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        btnHierarchy = new javax.swing.JButton();

        mnuSelectAll.setText(resourceBundle.getString("Select_all")); // NOI18N
        mnuSelectAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuSelectAllActionPerformed(evt);
            }
        });
        pmElements.add(mnuSelectAll);

        mnuDeselectAll.setText(resourceBundle.getString("Deselect_all")); // NOI18N
        mnuDeselectAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuDeselectAllActionPerformed(evt);
            }
        });
        pmElements.add(mnuDeselectAll);

        mnuInvertSelection.setText(resourceBundle.getString("Invert_selection")); // NOI18N
        mnuInvertSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuInvertSelectionActionPerformed(evt);
            }
        });
        pmElements.add(mnuInvertSelection);
        pmElements.add(mnuSeparator1);

        mnuExpandAll.setText(resourceBundle.getString("Expand_all_nodes")); // NOI18N
        mnuExpandAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuExpandAllActionPerformed(evt);
            }
        });
        pmElements.add(mnuExpandAll);

        mnuCollapseAll.setText(resourceBundle.getString("Collapse_all_nodes")); // NOI18N
        mnuCollapseAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuCollapseAllActionPerformed(evt);
            }
        });
        pmElements.add(mnuCollapseAll);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(resourceBundle.getString("Select_Element_Caption")); // NOI18N
        setAlwaysOnTop(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        treeElements.setModel(elementTreeModel);
        treeElements.setToolTipText(resourceBundle.getString("Right_click_tip")); // NOI18N
        treeElements.setComponentPopupMenu(pmElements);
        treeElements.setRootVisible(false);
        treeElements.setShowsRootHandles(true);
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
        jScrollPane1.setViewportView(treeElements);

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 60, 230, 340));

        btnOK.setText(resourceBundle.getString("OK")); // NOI18N
        btnOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOKActionPerformed(evt);
            }
        });
        getContentPane().add(btnOK, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 30, 90, -1));

        btnCancel.setText(resourceBundle.getString("Cancel")); // NOI18N
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });
        getContentPane().add(btnCancel, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 60, 90, -1));

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs"); // NOI18N
        jButtonExpand.setText(bundle.getString("Expand")); // NOI18N
        jButtonExpand.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonExpand.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExpandActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonExpand, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 30, 25, -1));

        jButtonCollapse.setText(bundle.getString("Collapse")); // NOI18N
        jButtonCollapse.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonCollapse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCollapseActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonCollapse, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 30, 25, -1));

        jButtonExpandAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/expandAll.png"))); // NOI18N
        jButtonExpandAll.setText(bundle.getString("ExpandAll")); // NOI18N
        jButtonExpandAll.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonExpandAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExpandAllActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonExpandAll, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 30, -1, -1));

        jButtonCollapseAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/collapseAll.png"))); // NOI18N
        jButtonCollapseAll.setText(bundle.getString("CollapseAll")); // NOI18N
        jButtonCollapseAll.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonCollapseAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCollapseAllActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonCollapseAll, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 30, -1, -1));
        getContentPane().add(jTextFieldSearch, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 400, 120, -1));

        jButtonSearch.setText(bundle.getString("SearchSelect")); // NOI18N
        jButtonSearch.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSearchActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonSearch, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 400, 130, -1));

        jButtonSelectAll.setText(bundle.getString("SelectAll")); // NOI18N
        jButtonSelectAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSelectAllActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonSelectAll, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 340, 120, -1));

        jButtonSelectChildren.setText(bundle.getString("SelectChildren")); // NOI18N
        jButtonSelectChildren.setMargin(new java.awt.Insets(2, 6, 2, 6));
        jButtonSelectChildren.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSelectChildrenActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonSelectChildren, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 340, 130, -1));

        jButtonSelect1.setText("1"); // NOI18N
        jButtonSelect1.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonSelect1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSelect1ActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonSelect1, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 370, -1, -1));

        jButtonSelect2.setText("2"); // NOI18N
        jButtonSelect2.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonSelect2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSelect2ActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonSelect2, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 370, -1, -1));

        jButtonSelect3.setText("3"); // NOI18N
        jButtonSelect3.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonSelect3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSelect3ActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonSelect3, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 370, -1, -1));

        jButtonSelect4.setText("4"); // NOI18N
        jButtonSelect4.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonSelect4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSelect4ActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonSelect4, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 370, -1, -1));

        jButtonSelect5.setText("5"); // NOI18N
        jButtonSelect5.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonSelect5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSelect5ActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonSelect5, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 370, -1, -1));

        jButtonSelectB.setText("B"); // NOI18N
        jButtonSelectB.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonSelectB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSelectBActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonSelectB, new org.netbeans.lib.awtextra.AbsoluteConstraints(395, 370, -1, -1));

        jListElements.setModel(elementModel);
        jListElements.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jListElementsMouseClicked(evt);
            }
        });
        jListElements.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listElementsValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(jListElements);

        getContentPane().add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 60, 260, 270));

        jLabel1.setText(bundle.getString("Hierarchy")); // NOI18N
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, -1));

        jLabel2.setText(bundle.getString("PickList")); // NOI18N
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 10, -1, -1));

        jButtonMoveRight.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/MoveNext.png"))); // NOI18N
        jButtonMoveRight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonMoveRightActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonMoveRight, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 180, 25, -1));

        jButtonMoveLeft.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/MovePrevious.png"))); // NOI18N
        jButtonMoveLeft.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonMoveLeftActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonMoveLeft, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 210, 25, -1));

        jButtonSortUp.setText(bundle.getString("SortUp")); // NOI18N
        jButtonSortUp.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonSortUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSortUpActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonSortUp, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 30, 83, -1));

        jButtonSortDown.setText(bundle.getString("SortDown")); // NOI18N
        jButtonSortDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSortDownActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonSortDown, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 30, -1, -1));

        jButtonClear.setText(bundle.getString("Clear")); // NOI18N
        jButtonClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonClearActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonClear, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 30, 80, -1));

        jButtonMoveUp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/MoveUp.png"))); // NOI18N
        jButtonMoveUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonMoveUpActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonMoveUp, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 140, 25, -1));

        jButtonMoveDown.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/MoveDown.PNG"))); // NOI18N
        jButtonMoveDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonMoveDownActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonMoveDown, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 250, 25, -1));

        jLabel3.setText(bundle.getString("UseAlias")); // NOI18N
        getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 420, -1, -1));

        cmbAlias.setRenderer(new AttributeComboBoxRenderer());
        cmbAlias.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbAliasItemStateChanged(evt);
            }
        });
        cmbAlias.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbAliasActionPerformed(evt);
            }
        });
        getContentPane().add(cmbAlias, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 417, 160, -1));

        btnInvertSelection.setText(bundle.getString("InvertSelection")); // NOI18N
        btnInvertSelection.setMargin(new java.awt.Insets(2, 6, 2, 6));
        btnInvertSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInvertSelectionActionPerformed(evt);
            }
        });
        getContentPane().add(btnInvertSelection, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 370, 130, -1));

        jLabel7.setText(bundle.getString("ShiftTip")); // NOI18N
        getContentPane().add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 430, -1, -1));

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 60, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 50, Short.MAX_VALUE)
        );

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 410, 60, 50));

        btnHierarchy.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/showHierarchy.png"))); // NOI18N
        btnHierarchy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHierarchyActionPerformed(evt);
            }
        });
        getContentPane().add(btnHierarchy, new org.netbeans.lib.awtextra.AbsoluteConstraints(215, 30, 25, -1));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void mnuCollapseAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuCollapseAllActionPerformed
        preExpansionStateChange(false);
        expandAll(treeElements, false);
        postExpansionStateChange(false);
    }//GEN-LAST:event_mnuCollapseAllActionPerformed

    private void mnuExpandAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuExpandAllActionPerformed
        preExpansionStateChange(true);
        expandAll(treeElements, true);
        postExpansionStateChange(true);
    }//GEN-LAST:event_mnuExpandAllActionPerformed

    private void mnuInvertSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuInvertSelectionActionPerformed
        int cnt = treeElements.getRowCount();
        int amount = 0;

        for (int i = 0; i < cnt; i++) {
            TreePath path = treeElements.getPathForRow(i);
            if (treeElements.isPathSelected(path)) {
            } else {
                amount++;
            }
        }

        TreePath[] paths = new TreePath[amount];
        amount = 0;

        for (int i = 0; i < cnt; i++) {
            TreePath path = treeElements.getPathForRow(i);
            if (treeElements.isPathSelected(path)) {
            } else {
                paths[amount] = path;
                amount++;
            }
        }

        treeElements.clearSelection();
        treeElements.addSelectionPaths(paths);

        updateOKButton();
    }//GEN-LAST:event_mnuInvertSelectionActionPerformed

    private void mnuDeselectAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuDeselectAllActionPerformed
        for (int i = 0; i < treeElements.getRowCount(); i++) {
            treeElements.removeSelectionRow(i);
        }
        updateOKButton();
    }//GEN-LAST:event_mnuDeselectAllActionPerformed

    private void mnuSelectAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuSelectAllActionPerformed
        for (int i = 0; i < treeElements.getRowCount(); i++) {
            treeElements.addSelectionRow(i);
        }
        updateOKButton();
    }//GEN-LAST:event_mnuSelectAllActionPerformed

    private void treeElementsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_treeElementsMouseClicked
        if ((evt.getClickCount() == 2 && evt.isShiftDown())) {
            jButtonMoveRight.doClick();
        }
}//GEN-LAST:event_treeElementsMouseClicked

    private void treeElementsValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_treeElementsValueChanged
        updateMenu();
        updateOKButton();
        updateMoveButtons(treeElements, jListElements, jButtonMoveLeft, jButtonMoveRight, jButtonMoveUp, jButtonMoveDown);
}//GEN-LAST:event_treeElementsValueChanged

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        modalResult = JOptionPane.CANCEL_OPTION;
        setVisible(false);
        dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void btnOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOKActionPerformed
        modalResult = JOptionPane.OK_OPTION;
        setVisible(false);
        dispose();
    }//GEN-LAST:event_btnOKActionPerformed

    private void jButtonExpandActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExpandActionPerformed
        preExpansionStateChange(true);
        expand(treeElements);
        postExpansionStateChange(true);
}//GEN-LAST:event_jButtonExpandActionPerformed

    private void jButtonCollapseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCollapseActionPerformed
        preExpansionStateChange(false);
        collapse(treeElements);
        postExpansionStateChange(false);
    }//GEN-LAST:event_jButtonCollapseActionPerformed

    private void jButtonExpandAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExpandAllActionPerformed
        preExpansionStateChange(true);
        expandAll(treeElements, true);
        postExpansionStateChange(true);
    }//GEN-LAST:event_jButtonExpandAllActionPerformed

    private void jButtonCollapseAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCollapseAllActionPerformed
        preExpansionStateChange(false);
        expandAll(treeElements, false);
        postExpansionStateChange(false);
    }//GEN-LAST:event_jButtonCollapseAllActionPerformed

    private void jButtonSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSearchActionPerformed
        String searchText = jTextFieldSearch.getText();

        if ((evt.getModifiers() & ActionEvent.SHIFT_MASK) == 0) {
            treeElements.clearSelection();
        }

        if (searchForNode(treeElements, searchText, attribute, rootNode, (evt.getModifiers() & ActionEvent.SHIFT_MASK) != 0, false) == false) {
             JOptionPane.showMessageDialog(this,
                     java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("SearchNotFound"),
                     java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Information"), JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_jButtonSearchActionPerformed

    private void jButtonSelectAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSelectAllActionPerformed
        treeElements.setSelectionInterval(0, treeElements.getRowCount());
    }//GEN-LAST:event_jButtonSelectAllActionPerformed

    private void jButtonSelectChildrenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSelectChildrenActionPerformed
        TreePath path[] = treeElements.getSelectionPaths();

        for (int i = 0; i < path.length; i++) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path[i].getLastPathComponent();
            TreePath[] paths = new TreePath[node.getChildCount()];
            for (int j = 0; j < node.getChildCount(); j++) {
                paths[j] = new TreePath(((DefaultMutableTreeNode) node.getChildAt(j)).getPath());
            }
            treeElements.addSelectionPaths(paths);
        }
    }//GEN-LAST:event_jButtonSelectChildrenActionPerformed

    private void jButtonSelect1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSelect1ActionPerformed
        selectByLevel(1, ((evt.getModifiers() & ActionEvent.SHIFT_MASK) != 0) ? true : false);
    }//GEN-LAST:event_jButtonSelect1ActionPerformed

    private void jButtonSelect2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSelect2ActionPerformed
        selectByLevel(2, ((evt.getModifiers() & ActionEvent.SHIFT_MASK) != 0) ? true : false);
    }//GEN-LAST:event_jButtonSelect2ActionPerformed

    private void jButtonSelect3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSelect3ActionPerformed
        selectByLevel(3, ((evt.getModifiers() & ActionEvent.SHIFT_MASK) != 0) ? true : false);
    }//GEN-LAST:event_jButtonSelect3ActionPerformed

    private void jButtonSelect4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSelect4ActionPerformed
        selectByLevel(4, ((evt.getModifiers() & ActionEvent.SHIFT_MASK) != 0) ? true : false);
    }//GEN-LAST:event_jButtonSelect4ActionPerformed

    private void jButtonSelect5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSelect5ActionPerformed
        selectByLevel(5, ((evt.getModifiers() & ActionEvent.SHIFT_MASK) != 0) ? true : false);
    }//GEN-LAST:event_jButtonSelect5ActionPerformed

    private void jButtonSelectBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSelectBActionPerformed
        int cnt = treeElements.getRowCount();

        if ((evt.getModifiers() & ActionEvent.SHIFT_MASK) == 0) {
            treeElements.clearSelection();
        }

        int amount = 0;

        for (int i = 0; i < cnt; i++) {
            TreePath path = treeElements.getPathForRow(i);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            if (flatMode) {
                ElementTreeNode  elementNode = (ElementTreeNode)node;
                if (PaloLibUtil.getElementDepth(elementNode.getElement()) == 0) {
                    amount++;
                }
            } else {
                if (node.getChildCount() == 0) {
                    amount++;
                }
            }
        }

        TreePath[] paths = new TreePath[amount];
        amount = 0;

        for (int i = 0; i < cnt; i++) {
            TreePath path = treeElements.getPathForRow(i);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            if (flatMode) {
                ElementTreeNode  elementNode = (ElementTreeNode)node;
                if (PaloLibUtil.getElementDepth(elementNode.getElement()) == 0) {
                    paths[amount] = path;
                    amount++;
                }
            } else {
                if (node.getChildCount() == 0) {
                    paths[amount] = path;
                    amount++;
                }
            }
        }
        treeElements.addSelectionPaths(paths);
    }//GEN-LAST:event_jButtonSelectBActionPerformed

    private void jButtonMoveRightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonMoveRightActionPerformed
        int selRows[] = treeElements.getSelectionRows();
        if (selRows == null)
            return;

        treeElements.getModel();

        Arrays.sort(selRows);

        for (int i = 0; i < selRows.length; i++) {
            TreePath path = treeElements.getPathForRow(selRows[i]);
            ElementTreeNode node = (ElementTreeNode) path.getLastPathComponent();

            ListElement le = new ListElement(node, path);
            elementModel.addElement(le);
        }

        updateOKButton();
        updateMoveButtons(treeElements, jListElements, jButtonMoveLeft, jButtonMoveRight, jButtonMoveUp, jButtonMoveDown);
    }//GEN-LAST:event_jButtonMoveRightActionPerformed

    private void jButtonMoveLeftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonMoveLeftActionPerformed
        int selRows[] = jListElements.getSelectedIndices();

        for (int i = selRows.length - 1; i >= 0; i--) {
            elementModel.remove(selRows[i]);
        }

        updateOKButton();
        updateMoveButtons(treeElements, jListElements, jButtonMoveLeft, jButtonMoveRight, jButtonMoveUp, jButtonMoveDown);
    }//GEN-LAST:event_jButtonMoveLeftActionPerformed

    private void jButtonMoveUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonMoveUpActionPerformed
        int selRows[] = jListElements.getSelectedIndices();
        Object obj;

        for (int i = 0; i < selRows.length; i++) {
            if (selRows[i] > 0) {
                obj = elementModel.get(selRows[i]);
                elementModel.remove(selRows[i]);
                elementModel.insertElementAt(obj, selRows[i] - 1);
                jListElements.addSelectionInterval(selRows[i] - 1, selRows[i] - 1);
            }
        }

        updateMoveButtons(treeElements, jListElements, jButtonMoveLeft, jButtonMoveRight, jButtonMoveUp, jButtonMoveDown);
    }//GEN-LAST:event_jButtonMoveUpActionPerformed

    private void listElementsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listElementsValueChanged
        updateOKButton();
        updateMoveButtons(treeElements, jListElements, jButtonMoveLeft, jButtonMoveRight, jButtonMoveUp, jButtonMoveDown);
    }//GEN-LAST:event_listElementsValueChanged

    private void jButtonClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonClearActionPerformed
        elementModel.clear();
        updateOKButton();
        updateMoveButtons(treeElements, jListElements, jButtonMoveLeft, jButtonMoveRight, jButtonMoveUp, jButtonMoveDown);
    }//GEN-LAST:event_jButtonClearActionPerformed

    private void jButtonMoveDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonMoveDownActionPerformed
        int selRows[] = jListElements.getSelectedIndices();
        Object obj;

        for (int i = selRows.length - 1; i >= 0; i--) {
            obj = elementModel.get(selRows[i]);
            elementModel.remove(selRows[i]);
            elementModel.insertElementAt(obj, selRows[i] + 1);
            jListElements.addSelectionInterval(selRows[i] + 1, selRows[i] + 1);
        }

        updateMoveButtons(treeElements, jListElements, jButtonMoveLeft, jButtonMoveRight, jButtonMoveUp, jButtonMoveDown);
    }//GEN-LAST:event_jButtonMoveDownActionPerformed

    private void jButtonSortUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSortUpActionPerformed
        sortList(true);
    }//GEN-LAST:event_jButtonSortUpActionPerformed

    private void jButtonSortDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSortDownActionPerformed
        sortList(false);
    }//GEN-LAST:event_jButtonSortDownActionPerformed

    private void cmbAliasItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbAliasItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            if (evt.getItem() instanceof String) {
                attribute = null;
            } else {
                attribute = (IAttribute)evt.getItem();
            }
            TreeElementRenderer treeRenderer = (TreeElementRenderer)treeElements.getCellRenderer();
            treeRenderer.setAttribute(attribute);
            
            int cnt = treeElements.getRowCount();
            treeElements.getModel();

            for (int i = 0; i < cnt; i++) {
                TreePath path = treeElements.getPathForRow(i);
                ElementTreeNode node = (ElementTreeNode) path.getLastPathComponent();
                elementTreeModel.nodeChanged(node);
            }

            treeElements.repaint();
            ElementListRenderer renderer = (ElementListRenderer)jListElements.getCellRenderer();
            renderer.setAttribute(attribute);
            jListElements.repaint();
            updateOKButton();
        }
    }//GEN-LAST:event_cmbAliasItemStateChanged

    private void cmbAliasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbAliasActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbAliasActionPerformed

    private void btnInvertSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInvertSelectionActionPerformed
        int cnt = treeElements.getRowCount();
        int amount = 0;

        for (int i = 0; i < cnt; i++) {
            TreePath path = treeElements.getPathForRow(i);
            if (treeElements.isPathSelected(path)) {
            } else {
                amount++;
            }
        }

        TreePath[] paths = new TreePath[amount];
        amount = 0;

        for (int i = 0; i < cnt; i++) {
            TreePath path = treeElements.getPathForRow(i);
            if (treeElements.isPathSelected(path)) {
            } else {
                paths[amount] = path;
                amount++;
            }
        }

        treeElements.clearSelection();
        treeElements.addSelectionPaths(paths);
}//GEN-LAST:event_btnInvertSelectionActionPerformed

    private void btnHierarchyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHierarchyActionPerformed

        flatMode = !flatMode;
        TreeElementRenderer    renderer = (TreeElementRenderer)treeElements.getCellRenderer();

        if (flatMode) {
            buildFlatElementList(dimension, attribute);
            btnHierarchy.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/showFlat.png"))); // NOI18N
        } else {
            buildElementList(dimension, attribute);
            btnHierarchy.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/showHierarchy.png"))); // NOI18N
        }

        renderer.setShowIcon(flatMode);
        treeElements.setShowsRootHandles(!flatMode);
        updateOKButton();
}//GEN-LAST:event_btnHierarchyActionPerformed

    private void jListElementsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListElementsMouseClicked
        //System.err.println("Handle click: "+evt.getClickCount());
        if (evt.getClickCount() >= 2 && !evt.isConsumed()) {
           
            jButtonMoveLeft.doClick();
            evt.consume();
          
        }
    }//GEN-LAST:event_jListElementsMouseClicked

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        for (JDialog d : manager.getOpenDialogs()) {
            if (d.getClass().equals(this.getClass())) {
                setVisible(false);
                dispose();
                return;
            }
        }
        manager.getOpenDialogs().add(this);
        this.requestFocus();
    }//GEN-LAST:event_formWindowOpened

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        manager.getOpenDialogs().remove(this);
    }//GEN-LAST:event_formWindowClosed

    private void updateOKButton() {
        TreePath[] paths = treeElements.getSelectionPaths();
        btnOK.setEnabled((paths != null && paths.length > 0) || elementModel.size() > 0);

        jButtonSelect2.setEnabled(!flatMode);
        jButtonSelect3.setEnabled(!flatMode);
        jButtonSelect4.setEnabled(!flatMode);
        jButtonSelect5.setEnabled(!flatMode);
        jButtonSelectChildren.setEnabled(!flatMode);
    }

    private void updateMenu() {
    }

    private void selectByLevel(int level, boolean addSelection) {
        int cnt = treeElements.getRowCount();

        if (addSelection == false)
            treeElements.clearSelection();

        int amount = 0;

        for (int i = 0; i < cnt; i++) {
            TreePath path = treeElements.getPathForRow(i);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            if (flatMode) {
                ElementTreeNode  elementNode = (ElementTreeNode)node;
                if (PaloLibUtil.getElementDepth(elementNode.getElement()) == level - 1) {
                    amount++;
                }
            } else {
                if (node.getLevel() == level) {
                    amount++;
                }
            }
        }

        TreePath[] paths = new TreePath[amount];
        amount = 0;

        for (int i = 0; i < cnt; i++) {
            TreePath path = treeElements.getPathForRow(i);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            if (flatMode) {
                ElementTreeNode  elementNode = (ElementTreeNode)node;
                if (PaloLibUtil.getElementDepth(elementNode.getElement()) == level - 1) {
                    paths[amount] = path;
                    amount++;
                }
            } else {
                if (node.getLevel() == level) {
                    paths[amount] = path;
                    amount++;
                }
            }
        }

        treeElements.addSelectionPaths(paths);

        updateMoveButtons(treeElements, jListElements, jButtonMoveLeft, jButtonMoveRight, jButtonMoveUp, jButtonMoveDown);
    }

    @Override
    public void connectionChanged(ConnectionInfo connectionInfo) {
        super.connectionChanged(connectionInfo);

        if (connectionInfo.getState() != ConnectionState.Connected) {
            elementTreeModel.reload();
        }
    }

    public void treeExpanded(TreeExpansionEvent event) {
        if (treeElements.getSelectionModel().getSelectionMode() == TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION) {
            TreePath path = event.getPath();
            boolean use = false;

            if (selectionPaths == null) {
                use = treeElements.isPathSelected(path);
            } else {
                use = treeElements.isPathSelected(path) || selectionPaths.contains(path);
            }
            
            if (use) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                if (selectionPaths == null) {
                    TreePath[] paths = new TreePath[node.getChildCount()];
                    for (int i = 0; i < node.getChildCount(); i++) {
                        paths[i] = new TreePath(((DefaultMutableTreeNode) node.getChildAt(i)).getPath());
                    }
                    treeElements.addSelectionPaths(paths);
                } else {
                    for (int i = 0; i < node.getChildCount(); i++) {
                        selectionPaths.add(new TreePath(((DefaultMutableTreeNode) node.getChildAt(i)).getPath()));
                    }
                }
            }
        }
    }

    public void treeCollapsed(TreeExpansionEvent event) {
    }

    public javax.swing.JButton getOKButton() {
        return btnOK;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnHierarchy;
    private javax.swing.JButton btnInvertSelection;
    private javax.swing.JButton btnOK;
    private javax.swing.JComboBox cmbAlias;
    private javax.swing.JButton jButtonClear;
    private javax.swing.JButton jButtonCollapse;
    private javax.swing.JButton jButtonCollapseAll;
    private javax.swing.JButton jButtonExpand;
    private javax.swing.JButton jButtonExpandAll;
    private javax.swing.JButton jButtonMoveDown;
    private javax.swing.JButton jButtonMoveLeft;
    private javax.swing.JButton jButtonMoveRight;
    private javax.swing.JButton jButtonMoveUp;
    private javax.swing.JButton jButtonSearch;
    private javax.swing.JButton jButtonSelect1;
    private javax.swing.JButton jButtonSelect2;
    private javax.swing.JButton jButtonSelect3;
    private javax.swing.JButton jButtonSelect4;
    private javax.swing.JButton jButtonSelect5;
    private javax.swing.JButton jButtonSelectAll;
    private javax.swing.JButton jButtonSelectB;
    private javax.swing.JButton jButtonSelectChildren;
    private javax.swing.JButton jButtonSortDown;
    private javax.swing.JButton jButtonSortUp;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JList jListElements;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField jTextFieldSearch;
    private javax.swing.JMenuItem mnuCollapseAll;
    private javax.swing.JMenuItem mnuDeselectAll;
    private javax.swing.JMenuItem mnuExpandAll;
    private javax.swing.JMenuItem mnuInvertSelection;
    private javax.swing.JMenuItem mnuSelectAll;
    private javax.swing.JSeparator mnuSeparator1;
    private javax.swing.JPopupMenu pmElements;
    private javax.swing.JTree treeElements;
    // End of variables declaration//GEN-END:variables
}
