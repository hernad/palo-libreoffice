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
 */

package org.palooca.dialogs;

import org.palooca.dialogs.nodes.ElementTreeNode;
import com.sun.star.uno.XComponentContext;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import javax.swing.JOptionPane;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import com.jedox.palojlib.interfaces.IAttribute;
import com.jedox.palojlib.interfaces.IDimension;
import com.jedox.palojlib.interfaces.IDimension.DimensionType;
import com.jedox.palojlib.interfaces.IElement;
import javax.swing.JDialog;
import org.palooca.PalOOCaManager;
import org.palooca.network.ConnectionInfo;
import org.palooca.network.ConnectionState;

/**
 *
 * @author Andreas Schneider
 */
public class ElementSingleSelectDialog extends PaloDialogUtilities
        implements TreeExpansionListener {
    
    private int modalResult = JOptionPane.CANCEL_OPTION;
    
    public int getModalResult() {
        return modalResult;
    }

    public IDimension getDimension() {
        return dimension;
    }
            
    /** Creates new form ElementBrowserDialog */
    public ElementSingleSelectDialog(java.awt.Frame parent, boolean modal,
            XComponentContext context, final String servdb,
            final String dimensionName, final String selectedElementName,
            final String selectedElementPath, String selectedAttribute)
    {
        super(parent, modal, context);
        
        connectionHandler = PalOOCaManager.getInstance(context).getConnectionHandler();
        database = connectionHandler.getDatabase(servdb);
        if ( database != null )
            dimension = database.getDimensionByName(dimensionName);

        if (dimension != null)
            attribute = dimension.getAttributeByName(selectedAttribute);

        init(parent, dimension, attribute);

        TreePath selection = null;
        if (selectedElementPath == null || selectedElementPath.length() == 0) {
            TreeNode node = findNode(selectedElementName, rootNode, true);
            if (node != null) {
                selection = new TreePath(((DefaultMutableTreeNode)node).getPath());
                setSelection(pathToStringArray(selection));
            }
        } else {
            String[]    selectionPath = stringPathToStringArray(selectedElementPath);
            setSelection(selectionPath);
        }
    }
    
    public ElementSingleSelectDialog(java.awt.Dialog parent, boolean modal,
            XComponentContext context, 
            IDimension dimension, String[] selectedElementPath, IAttribute attribute)
    {
        super(parent, modal, context);
        init(parent, dimension, attribute);
        setSelection(selectedElementPath);
    }

    private void init(Component parent, IDimension dimension, IAttribute attribute) {
        this.resourceBundle = PalOOCaManager.getInstance(context).getResourceBundle("org/palooca/dialogs/PalOOCaDialogs");
        this.attribute = attribute;
        this.dimension = dimension;

        initComponents();
        prepareMenu(true);
        trElements.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        TreeCellRenderer renderer = new TreeElementRenderer(attribute);
        trElements.setCellRenderer(renderer);

        cmbAlias.addItem(resourceBundle.getString("None"));

        if (!dimension.getType().equals(DimensionType.DIMENSION_ATTRIBUTE) && !dimension.getType().equals(DimensionType.DIMENSION_SYSTEM)) {
            IAttribute[]  attributes = dimension.getAttributes();
            int selectedIndex = 0;
            int added = 1;
            for (int i = 0; i < attributes.length; i++) {
                if (attributes[i].getType().equals(IElement.ElementType.ELEMENT_STRING)) {
                    cmbAlias.addItem(attributes[i]);
                    if (attribute != null && attributes[i].getName().equals(attribute.getName())) selectedIndex = added;
                    added++;
                }
            }

            if (attribute != null)
                cmbAlias.setSelectedIndex(selectedIndex);

        }
        buildElementList(dimension, attribute);

        trElements.addTreeExpansionListener(this);

        setLocationRelativeTo(parent);
    }

    private void prepareMenu(boolean singleSelectionMode) {
        mnuSelectAll.setVisible(!singleSelectionMode);
        mnuDeselectAll.setVisible(!singleSelectionMode);
        mnuInvertSelection.setVisible(!singleSelectionMode);
        mnuSeparator1.setVisible(!singleSelectionMode);
    }
    
    public void setSelection(String[] selectionPath) {
        if (selectionPath != null) {
            TreePath selection = findByName(trElements, selectionPath);
            trElements.makeVisible(selection);
            trElements.setSelectionPath(selection);
            trElements.scrollPathToVisible(selection);
        }
    }
    
    public String[] getSelectionPath() {
        return pathToStringArray(trElements.getSelectionPath());
    }

    private IElement getFirstRootElement() {
        IElement[] roots = dimension.getRootElements(false);
        if (roots.length > 0) return roots[0];
        return null;
    }

    public IElement getSelectionElement() {
        if (trElements.getSelectionPath() == null)
            return null;
        ElementTreeNode node = (ElementTreeNode)trElements.getSelectionPath().getLastPathComponent();
        if (node == null) {
            return null;
        }

        return node.getElement();
    }
    
    public String getSelectedElementName() {
        ElementTreeNode node = (ElementTreeNode)trElements.getSelectionPath().getLastPathComponent();
        if (node != null)
            return node.getElement().getName();
        else
            return "";
    }
   
    public String getSelectedElementPath() {
        TreePath path = trElements.getSelectionPath();
        if (path != null)
            return pathToStringPath(path);
        else
            return "";
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
        trElements = new javax.swing.JTree();
        btnOK = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        jButtonExpand = new javax.swing.JButton();
        jButtonCollapse = new javax.swing.JButton();
        jButtonExpandAll = new javax.swing.JButton();
        jButtonCollapseAll = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        cmbAlias = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jTextFieldSearch = new javax.swing.JTextField();
        jButtonSearch = new javax.swing.JButton();

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

        trElements.setModel(elementTreeModel);
        trElements.setToolTipText(resourceBundle.getString("Right_click_tip")); // NOI18N
        trElements.setComponentPopupMenu(pmElements);
        trElements.setRootVisible(false);
        trElements.setShowsRootHandles(true);
        trElements.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                trElementsMouseClicked(evt);
            }
        });
        trElements.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                trElementsValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(trElements);

        btnOK.setText(resourceBundle.getString("OK")); // NOI18N
        btnOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOKActionPerformed(evt);
            }
        });

        btnCancel.setText(resourceBundle.getString("Cancel")); // NOI18N
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs"); // NOI18N
        jButtonExpand.setText(bundle.getString("Expand")); // NOI18N
        jButtonExpand.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonExpand.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExpandActionPerformed(evt);
            }
        });

        jButtonCollapse.setText(bundle.getString("Collapse")); // NOI18N
        jButtonCollapse.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonCollapse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCollapseActionPerformed(evt);
            }
        });

        jButtonExpandAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/expandAll.png"))); // NOI18N
        jButtonExpandAll.setText(bundle.getString("ExpandAll")); // NOI18N
        jButtonExpandAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExpandAllActionPerformed(evt);
            }
        });

        jButtonCollapseAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/collapseAll.png"))); // NOI18N
        jButtonCollapseAll.setText(bundle.getString("CollapseAll")); // NOI18N
        jButtonCollapseAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCollapseAllActionPerformed(evt);
            }
        });

        jLabel1.setText(bundle.getString("Hierarchy")); // NOI18N

        cmbAlias.setRenderer(new AttributeComboBoxRenderer());
        cmbAlias.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbAliasItemStateChanged(evt);
            }
        });

        jLabel3.setText(bundle.getString("UseAlias")); // NOI18N

        jButtonSearch.setText(bundle.getString("SearchSelect")); // NOI18N
        jButtonSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSearchActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel1)
                            .add(layout.createSequentialGroup()
                                .add(jButtonExpandAll, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jButtonCollapseAll, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 27, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(18, 18, 18)
                                .add(jButtonExpand, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jButtonCollapse, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(layout.createSequentialGroup()
                                .add(jLabel3)
                                .add(18, 18, 18)
                                .add(cmbAlias, 0, 198, Short.MAX_VALUE))
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(btnOK, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(btnCancel)))
                    .add(layout.createSequentialGroup()
                        .add(jTextFieldSearch, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 122, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButtonSearch, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 136, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(jButtonExpand)
                                .add(jButtonCollapse))
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(jButtonExpandAll)
                                .add(jButtonCollapseAll)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 331, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel3)
                            .add(cmbAlias, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(29, 29, 29)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jTextFieldSearch, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jButtonSearch)))
                    .add(layout.createSequentialGroup()
                        .add(31, 31, 31)
                        .add(btnOK)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnCancel)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void mnuCollapseAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuCollapseAllActionPerformed
        for (int i = 0; i < trElements.getRowCount(); i++)
            trElements.collapseRow(i);
    }//GEN-LAST:event_mnuCollapseAllActionPerformed

    private void mnuExpandAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuExpandAllActionPerformed
        for (int i = 0; i < trElements.getRowCount(); i++)
            trElements.expandRow(i);
    }//GEN-LAST:event_mnuExpandAllActionPerformed

    private void mnuInvertSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuInvertSelectionActionPerformed
        for (int i = 0; i < trElements.getRowCount(); i++) {
            if (trElements.isRowSelected(i))
                trElements.removeSelectionRow(i);
            else
                trElements.addSelectionRow(i);
        }
    }//GEN-LAST:event_mnuInvertSelectionActionPerformed

    private void mnuDeselectAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuDeselectAllActionPerformed
        for (int i = 0; i < trElements.getRowCount(); i++)
            trElements.removeSelectionRow(i);
            
    }//GEN-LAST:event_mnuDeselectAllActionPerformed

    private void mnuSelectAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuSelectAllActionPerformed
        for (int i = 0; i < trElements.getRowCount(); i++)
            trElements.addSelectionRow(i);
    }//GEN-LAST:event_mnuSelectAllActionPerformed

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
        expand(trElements);
}//GEN-LAST:event_jButtonExpandActionPerformed

    private void jButtonCollapseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCollapseActionPerformed
        collapse(trElements);
    }//GEN-LAST:event_jButtonCollapseActionPerformed

    private void jButtonExpandAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExpandAllActionPerformed
        expandAll(trElements, true);
    }//GEN-LAST:event_jButtonExpandAllActionPerformed

    private void jButtonCollapseAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCollapseAllActionPerformed
        expandAll(trElements, false);
    }//GEN-LAST:event_jButtonCollapseAllActionPerformed

    private void trElementsValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_trElementsValueChanged
        TreePath[] paths = trElements.getSelectionPaths();
        btnOK.setEnabled(paths != null && paths.length > 0);
    }//GEN-LAST:event_trElementsValueChanged

    private void trElementsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_trElementsMouseClicked
        if ((evt.getClickCount() == 2 && evt.isShiftDown()))
            btnOK.doClick();
}//GEN-LAST:event_trElementsMouseClicked

    private void cmbAliasItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbAliasItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            if (evt.getItem() instanceof String) {
                attribute = null;
            } else {
                attribute = (IAttribute)evt.getItem();
            }
            TreeElementRenderer treeRenderer = (TreeElementRenderer)trElements.getCellRenderer();
            treeRenderer.setAttribute(attribute);

            int cnt = trElements.getRowCount();
            trElements.getModel();

            for (int i = 0; i < cnt; i++) {
                TreePath path = trElements.getPathForRow(i);
                ElementTreeNode node = (ElementTreeNode) path.getLastPathComponent();
                elementTreeModel.nodeChanged(node);
            }

            trElements.repaint();
            TreePath[] paths = trElements.getSelectionPaths();
            btnOK.setEnabled(paths != null && paths.length > 0);
        }
}//GEN-LAST:event_cmbAliasItemStateChanged

    private void jButtonSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSearchActionPerformed
        String searchText = jTextFieldSearch.getText();

        if ((evt.getModifiers() & ActionEvent.SHIFT_MASK) == 0) {
            trElements.clearSelection();
        }

        if (searchForNode(trElements, searchText, attribute, rootNode, (evt.getModifiers() & ActionEvent.SHIFT_MASK) != 0, true) == false) {
            JOptionPane.showMessageDialog(this,
                    java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("SearchNotFound"),
                    java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Information"), JOptionPane.INFORMATION_MESSAGE);
        }
}//GEN-LAST:event_jButtonSearchActionPerformed

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

    @Override
    public void connectionChanged(ConnectionInfo connectionInfo) {
        super.connectionChanged(connectionInfo);

        if (connectionInfo.getState() != ConnectionState.Connected) {
            elementTreeModel.reload();
        }
    }

    public void treeExpanded(TreeExpansionEvent event) {
        if (trElements.getSelectionModel().getSelectionMode() == TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION) {
            TreePath path = event.getPath();
            if (trElements.isPathSelected(path)) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                for (int i = 0; i < node.getChildCount(); i++)
                    trElements.addSelectionPath(new TreePath(((DefaultMutableTreeNode)node.getChildAt(i)).getPath()));
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
    private javax.swing.JButton btnOK;
    private javax.swing.JComboBox cmbAlias;
    private javax.swing.JButton jButtonCollapse;
    private javax.swing.JButton jButtonCollapseAll;
    private javax.swing.JButton jButtonExpand;
    private javax.swing.JButton jButtonExpandAll;
    private javax.swing.JButton jButtonSearch;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextFieldSearch;
    private javax.swing.JMenuItem mnuCollapseAll;
    private javax.swing.JMenuItem mnuDeselectAll;
    private javax.swing.JMenuItem mnuExpandAll;
    private javax.swing.JMenuItem mnuInvertSelection;
    private javax.swing.JMenuItem mnuSelectAll;
    private javax.swing.JSeparator mnuSeparator1;
    private javax.swing.JPopupMenu pmElements;
    private javax.swing.JTree trElements;
    // End of variables declaration//GEN-END:variables
    
}
