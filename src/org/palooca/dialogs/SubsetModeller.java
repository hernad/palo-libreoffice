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

import com.sun.star.uno.XComponentContext;
import java.awt.event.ItemEvent;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.tree.TreeCellRenderer;
import com.jedox.palojlib.interfaces.*;
import org.palooca.dialogs.nodes.ElementTreeNode;
import org.palooca.network.ConnectionInfo;
import org.palooca.subsets.Subset2;
import org.palooca.subsets.SubsetFilter;

/**
 *
 * @author MichaelRaue
 */
public class SubsetModeller extends PaloDialogUtilities {
    Subset2     subBackup = null;
    boolean     dirty = false;

    /** Creates new form SubsetModeller */
    public SubsetModeller(java.awt.Dialog parent, boolean modal, XComponentContext context) {
        super(parent, modal, context);
        initComponents();

        subsetList1.setModeller(this);

        TreeCellRenderer renderer = new TreeElementRenderer(null);
        jTree1.setCellRenderer(renderer);

        cmbDatabase.setRenderer(new DatabaseListCellRenderer());
        buildDatabaseList();

        cmbDimension.setRenderer(new DimensionListCellRenderer());

        setDatabase(connectionHandler.getLastConnectionInfo(), connectionHandler.getLastDatabase(), null);
        if (database != null)
            selectDatabaseCombo(connectionHandler.getLastConnectionInfo(), database, cmbDatabase);

        setLocationRelativeTo(parent);
    }

    @Override
    public void setDatabase(ConnectionInfo connectionInfoNew, IDatabase databaseNew, JComboBox combBoxCube) {
        super.setDatabase(connectionInfoNew, databaseNew, null);

        if (connectionInfoNew != null && databaseNew != null) {

            dimensionComboModel.removeAllElements();

            IDimension[] dims = database.getDimensions();
            for (int i = 0; i < dims.length; i++) {
                if (dims[i].getType() == IDimension.DimensionType.DIMENSION_NORMAL) {
                    dimensionComboModel.addElement(dims[i]);
                }
            }

            if (dimensionComboModel.getSize() > 0) {
                cmbDimension.setSelectedIndex(0);
            }

            cmbDimension.setEnabled(true);
        } else {
            cmbDimension.setEnabled(false);
        }
    }

    public void setDimension(IDimension dimension) {
        dimensionComboModel.setSelectedItem(dimension);
        this.dimension = dimension;
    }

    @Override
    protected void resetDatabase()
    {
        super.resetDatabase();

        dimensionComboModel.removeAllElements();
        cmbDimension.setEnabled(false);
    }

    public void setSubset(Subset2 subset) {

        if (dirty) {
            int ret = JOptionPane.showConfirmDialog(this, java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Delete_selected_Database?"), java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs").getString("Confirmation"), JOptionPane.YES_NO_CANCEL_OPTION);
            switch (ret) {
                case JOptionPane.CANCEL_OPTION:
                    return;
                case JOptionPane.YES_OPTION:
                    break;
                case JOptionPane.NO_OPTION:
                    break;
            }
        }

        rootNode.removeAllChildren();

        if (subset == null) 
            return;

        subBackup = subset.copy();
       
        /*
        SortingFilter setting = (SortingFilter)subset.getFilter(SubsetFilter.TYPE_SORTING);
        if (setting == null)
            return;

        switch (setting.getWhole()) {
            case SortingFilter.HIERARCHICAL_MODE_DISABLED:
                switch (setting.getReverse()) {
                    case SortingFilter.ORDER_MODE_REVERSE_DISABLED:
                        radioViewAsList.setSelected(true);
                        chkShowHiddenChildren.setSelected(false);
                        chkReverse.setSelected(false);
                        break;
                    case SortingFilter.ORDER_MODE_REVERSE_TOTAL:
                        break;
                    case SortingFilter.ORDER_MODE_REVERSE_PER_LEVEL:
                        break;
                    default:
                        break;
                }
                break;
            case SortingFilter.HIERARCHICAL_MODE_SHOW_CHILDREN:
                switch (setting.getReverse()) {
                    case SortingFilter.ORDER_MODE_REVERSE_DISABLED:
                        radioViewAsHierarchy.setSelected(true);
                        chkShowHiddenChildren.setSelected(true);
                        chkReverse.setSelected(false);
                        break;
                    case SortingFilter.ORDER_MODE_REVERSE_TOTAL:
                        radioViewParentsBelowChildren.setSelected(true);
                        chkShowHiddenChildren.setSelected(true);
                        chkReverse.setSelected(false);
                        break;
                    case SortingFilter.ORDER_MODE_REVERSE_PER_LEVEL:
                        radioViewAsHierarchy.setSelected(true);
                        chkShowHiddenChildren.setSelected(true);
                        chkReverse.setSelected(true);
                        break;
                    default:
                        radioViewParentsBelowChildren.setSelected(true);
                        chkShowHiddenChildren.setSelected(true);
                        chkReverse.setSelected(true);
                        break;
                }
                break;
            case SortingFilter.HIERARCHICAL_MODE_HIDE_CHILDREN:
                switch (setting.getReverse()) {
                    case SortingFilter.ORDER_MODE_REVERSE_DISABLED:
                        radioViewAsHierarchy.setSelected(true);
                        chkShowHiddenChildren.setSelected(false);
                        chkReverse.setSelected(false);
                        break;
                    case SortingFilter.ORDER_MODE_REVERSE_TOTAL:
                        radioViewParentsBelowChildren.setSelected(true);
                        chkShowHiddenChildren.setSelected(true);
                        chkReverse.setSelected(false);
                        break;
                    case SortingFilter.ORDER_MODE_REVERSE_PER_LEVEL:
                        radioViewAsHierarchy.setSelected(true);
                        chkShowHiddenChildren.setSelected(false);
                        chkReverse.setSelected(false);
                        break;
                    default:
                        radioViewParentsBelowChildren.setSelected(true);
                        chkShowHiddenChildren.setSelected(true);
                        chkReverse.setSelected(true);
                        break;
                }
                break;
        }
         * */
       
        updatePreview();
        updateFormula();
    }

    void updatePreview() {
        rootNode.removeAllChildren();

        if (dimension == null)
            return;

        Subset2 sub = subsetList1.getSelectedSubset();
        if (sub == null)
            return;

        IElement roots[] = sub.getRootNodes();

        for (int i = 0; i < roots.length; i++) {
            ElementTreeNode elementNode = new ElementTreeNode(roots[i],sub.getDimension());
            rootNode.add(elementNode);
            addElementChildren(elementNode, roots[i]);
        }

        elementTreeModel.reload();
        expandAll(jTree1, true);
    }

    private void addElementChildren(ElementTreeNode elementNode, IElement node) {
        IElement[] children = node.getChildren();
        if (children == null)
            return;

        for (int i = 0; i < children.length; i++) {
            ElementTreeNode elementChild = new ElementTreeNode(children[i],elementNode.getDimension());
            elementNode.add(elementChild);
            addElementChildren(elementChild, children[i]);
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

        buttonGroupLayout = new javax.swing.ButtonGroup();
        buttonGroupHierarchyNumbering = new javax.swing.ButtonGroup();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        subsetList1 = new org.palooca.dialogs.SubsetList();
        jPanel2 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        panelGeneral = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        cmbDatabase = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        cmbDimension = new javax.swing.JComboBox();
        jPanel4 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        cmbAlias1 = new javax.swing.JComboBox();
        cmbAlias2 = new javax.swing.JComboBox();
        jCheckBox3 = new javax.swing.JCheckBox();
        jCheckBox4 = new javax.swing.JCheckBox();
        jPanel5 = new javax.swing.JPanel();
        radioViewAsList = new javax.swing.JRadioButton();
        radioViewAsHierarchy = new javax.swing.JRadioButton();
        radioViewParentsBelowChildren = new javax.swing.JRadioButton();
        jPanel6 = new javax.swing.JPanel();
        radioHighestLevel1 = new javax.swing.JRadioButton();
        radioLowestLevel0 = new javax.swing.JRadioButton();
        radioHighestLevel0 = new javax.swing.JRadioButton();
        jPanel7 = new javax.swing.JPanel();
        chkShowHiddenChildren = new javax.swing.JCheckBox();
        chkIncludeDuplicates = new javax.swing.JCheckBox();
        panelHierarchy = new javax.swing.JPanel();
        chkActivateHierarchy = new javax.swing.JCheckBox();
        panelText = new javax.swing.JPanel();
        chkActivateText = new javax.swing.JCheckBox();
        panelPickList = new javax.swing.JPanel();
        panelAttribute = new javax.swing.JPanel();
        panelData = new javax.swing.JPanel();
        panelSort = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        chkReverse = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();
        btnSave = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();
        btnSubsetNew = new javax.swing.JButton();
        btnSubsetDelete = new javax.swing.JButton();
        btnSubsetEdit = new javax.swing.JButton();
        txtFormula = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs"); // NOI18N
        setTitle(bundle.getString("InsertSubset")); // NOI18N
        setAlwaysOnTop(true);
        setName("Form"); // NOI18N
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jSplitPane1.setDividerLocation(160);
        jSplitPane1.setName("jSplitPane1"); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N

        jLabel1.setText(bundle.getString("SavedSubsets")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        subsetList1.setName("subsetList1"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(subsetList1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 141, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(subsetList1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 442, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSplitPane1.setLeftComponent(jPanel1);

        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        panelGeneral.setName("panelGeneral"); // NOI18N
        panelGeneral.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("Source"))); // NOI18N
        jPanel3.setName("jPanel3"); // NOI18N

        jLabel3.setText(bundle.getString("Database_")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        cmbDatabase.setModel(databaseModel);
        cmbDatabase.setName("cmbDatabase"); // NOI18N
        cmbDatabase.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbDatabaseItemStateChanged(evt);
            }
        });

        jLabel4.setText(bundle.getString("Dimension_")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        cmbDimension.setModel(dimensionComboModel);
        cmbDimension.setName("cmbDimension"); // NOI18N
        cmbDimension.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbDimensionItemStateChanged(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(cmbDatabase, 0, 151, Short.MAX_VALUE)
                    .add(jLabel3)
                    .add(cmbDimension, 0, 151, Short.MAX_VALUE)
                    .add(jLabel4))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .add(jLabel3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cmbDatabase, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jLabel4)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cmbDimension, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelGeneral.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 200, 130));

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("Alias"))); // NOI18N
        jPanel4.setName("jPanel4"); // NOI18N

        jLabel5.setText(bundle.getString("Alias1")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        jLabel6.setText(bundle.getString("Alias2")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        cmbAlias1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbAlias1.setName("cmbAlias1"); // NOI18N

        cmbAlias2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbAlias2.setName("cmbAlias2"); // NOI18N

        jCheckBox3.setText(bundle.getString("Variable")); // NOI18N
        jCheckBox3.setName("jCheckBox3"); // NOI18N

        jCheckBox4.setText(bundle.getString("Variable")); // NOI18N
        jCheckBox4.setName("jCheckBox4"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(cmbAlias1, 0, 139, Short.MAX_VALUE)
                    .add(jLabel5)
                    .add(jLabel6)
                    .add(cmbAlias2, 0, 139, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jCheckBox4)
                    .add(jCheckBox3))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .add(jLabel5)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cmbAlias1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jCheckBox3))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jLabel6)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jCheckBox4)
                    .add(cmbAlias2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        panelGeneral.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 10, 270, 130));

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("Layout"))); // NOI18N
        jPanel5.setName("jPanel5"); // NOI18N

        buttonGroupLayout.add(radioViewAsList);
        radioViewAsList.setText(bundle.getString("ListView")); // NOI18N
        radioViewAsList.setName("radioViewAsList"); // NOI18N
        radioViewAsList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioViewAsListActionPerformed(evt);
            }
        });

        buttonGroupLayout.add(radioViewAsHierarchy);
        radioViewAsHierarchy.setText(bundle.getString("ViewHierarchy")); // NOI18N
        radioViewAsHierarchy.setName("radioViewAsHierarchy"); // NOI18N
        radioViewAsHierarchy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioViewAsHierarchyActionPerformed(evt);
            }
        });

        buttonGroupLayout.add(radioViewParentsBelowChildren);
        radioViewParentsBelowChildren.setText(bundle.getString("ViewParents")); // NOI18N
        radioViewParentsBelowChildren.setName("radioViewParentsBelowChildren"); // NOI18N
        radioViewParentsBelowChildren.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioViewParentsBelowChildrenActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel5Layout = new org.jdesktop.layout.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(radioViewAsList)
                    .add(radioViewAsHierarchy)
                    .add(radioViewParentsBelowChildren))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .add(radioViewAsList)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(radioViewAsHierarchy)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(radioViewParentsBelowChildren)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelGeneral.add(jPanel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 150, 200, 100));

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("HierarchyLevelNumber"))); // NOI18N
        jPanel6.setName("jPanel6"); // NOI18N

        buttonGroupHierarchyNumbering.add(radioHighestLevel1);
        radioHighestLevel1.setText(bundle.getString("HighLevel1")); // NOI18N
        radioHighestLevel1.setName("radioHighestLevel1"); // NOI18N
        radioHighestLevel1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioHighestLevel1ActionPerformed(evt);
            }
        });

        buttonGroupHierarchyNumbering.add(radioLowestLevel0);
        radioLowestLevel0.setText(bundle.getString("LowLevel0")); // NOI18N
        radioLowestLevel0.setName("radioLowestLevel0"); // NOI18N
        radioLowestLevel0.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioLowestLevel0ActionPerformed(evt);
            }
        });

        buttonGroupHierarchyNumbering.add(radioHighestLevel0);
        radioHighestLevel0.setText(bundle.getString("HighestLevel0")); // NOI18N
        radioHighestLevel0.setName("radioHighestLevel0"); // NOI18N
        radioHighestLevel0.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioHighestLevel0ActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel6Layout = new org.jdesktop.layout.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(radioHighestLevel1)
                    .add(radioLowestLevel0)
                    .add(radioHighestLevel0))
                .addContainerGap(97, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel6Layout.createSequentialGroup()
                .add(radioHighestLevel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(radioLowestLevel0)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(radioHighestLevel0)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelGeneral.add(jPanel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 150, 270, 100));

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("Options"))); // NOI18N
        jPanel7.setName("jPanel7"); // NOI18N

        chkShowHiddenChildren.setText(bundle.getString("ShowHiddenChildren")); // NOI18N
        chkShowHiddenChildren.setName("chkShowHiddenChildren"); // NOI18N
        chkShowHiddenChildren.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkShowHiddenChildrenActionPerformed(evt);
            }
        });

        chkIncludeDuplicates.setText(bundle.getString("WithDuplicats")); // NOI18N
        chkIncludeDuplicates.setName("chkIncludeDuplicates"); // NOI18N
        chkIncludeDuplicates.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkIncludeDuplicatesActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel7Layout = new org.jdesktop.layout.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(chkShowHiddenChildren)
                    .add(chkIncludeDuplicates))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel7Layout.createSequentialGroup()
                .add(chkShowHiddenChildren)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chkIncludeDuplicates)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelGeneral.add(jPanel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 260, 200, 80));

        jTabbedPane1.addTab(bundle.getString("General"), panelGeneral); // NOI18N

        panelHierarchy.setName("panelHierarchy"); // NOI18N
        panelHierarchy.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance().getContext().getResourceMap(SubsetModeller.class);
        chkActivateHierarchy.setFont(resourceMap.getFont("chkActivateHierarchy.font")); // NOI18N
        chkActivateHierarchy.setText(bundle.getString("ActivateFilter")); // NOI18N
        chkActivateHierarchy.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        chkActivateHierarchy.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        chkActivateHierarchy.setName("chkActivateHierarchy"); // NOI18N
        panelHierarchy.add(chkActivateHierarchy, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 10, -1, -1));

        jTabbedPane1.addTab(bundle.getString("Hierarchy"), panelHierarchy); // NOI18N

        panelText.setName("panelText"); // NOI18N
        panelText.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        chkActivateText.setFont(resourceMap.getFont("chkActivateText.font")); // NOI18N
        chkActivateText.setText(bundle.getString("ActivateFilter")); // NOI18N
        chkActivateText.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        chkActivateText.setName("chkActivateText"); // NOI18N
        panelText.add(chkActivateText, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 10, -1, -1));

        jTabbedPane1.addTab(bundle.getString("Text"), panelText); // NOI18N

        panelPickList.setName("panelPickList"); // NOI18N

        org.jdesktop.layout.GroupLayout panelPickListLayout = new org.jdesktop.layout.GroupLayout(panelPickList);
        panelPickList.setLayout(panelPickListLayout);
        panelPickListLayout.setHorizontalGroup(
            panelPickListLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 489, Short.MAX_VALUE)
        );
        panelPickListLayout.setVerticalGroup(
            panelPickListLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 444, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(bundle.getString("PickList"), panelPickList); // NOI18N

        panelAttribute.setName("panelAttribute"); // NOI18N

        org.jdesktop.layout.GroupLayout panelAttributeLayout = new org.jdesktop.layout.GroupLayout(panelAttribute);
        panelAttribute.setLayout(panelAttributeLayout);
        panelAttributeLayout.setHorizontalGroup(
            panelAttributeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 489, Short.MAX_VALUE)
        );
        panelAttributeLayout.setVerticalGroup(
            panelAttributeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 444, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(bundle.getString("Attribute"), panelAttribute); // NOI18N

        panelData.setName("panelData"); // NOI18N

        org.jdesktop.layout.GroupLayout panelDataLayout = new org.jdesktop.layout.GroupLayout(panelData);
        panelData.setLayout(panelDataLayout);
        panelDataLayout.setHorizontalGroup(
            panelDataLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 489, Short.MAX_VALUE)
        );
        panelDataLayout.setVerticalGroup(
            panelDataLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 444, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(bundle.getString("Data"), panelData); // NOI18N

        panelSort.setName("panelSort"); // NOI18N
        panelSort.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("RevertInstruction"))); // NOI18N
        jPanel8.setName("jPanel8"); // NOI18N
        jPanel8.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        chkReverse.setText(bundle.getString("Reverse")); // NOI18N
        chkReverse.setName("chkReverse"); // NOI18N
        chkReverse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkReverseActionPerformed(evt);
            }
        });
        jPanel8.add(chkReverse, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, -1, 20));

        panelSort.add(jPanel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 180, 480, 50));

        jTabbedPane1.addTab(bundle.getString("Sort"), panelSort); // NOI18N

        jPanel2.add(jTabbedPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 510, 490));

        jLabel2.setText(bundle.getString("Preview")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N
        jPanel2.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 10, -1, -1));

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTree1.setModel(elementTreeModel);
        jTree1.setName("jTree1"); // NOI18N
        jTree1.setRootVisible(false);
        jTree1.setShowsRootHandles(true);
        jScrollPane1.setViewportView(jTree1);

        jPanel2.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 30, 170, 470));

        jSplitPane1.setRightComponent(jPanel2);

        getContentPane().add(jSplitPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 880, 510));

        btnSave.setText(bundle.getString("Save")); // NOI18N
        btnSave.setName("btnSave"); // NOI18N
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });
        getContentPane().add(btnSave, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 520, 90, -1));

        btnClose.setText(resourceMap.getString("btnClose.text")); // NOI18N
        btnClose.setName("btnClose"); // NOI18N
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });
        getContentPane().add(btnClose, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 520, 90, -1));

        btnSubsetNew.setIcon(resourceMap.getIcon("btnSubsetNew.icon")); // NOI18N
        btnSubsetNew.setText(resourceMap.getString("btnSubsetNew.text")); // NOI18N
        btnSubsetNew.setName("btnSubsetNew"); // NOI18N
        btnSubsetNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSubsetNewActionPerformed(evt);
            }
        });
        getContentPane().add(btnSubsetNew, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 520, 30, -1));

        btnSubsetDelete.setIcon(resourceMap.getIcon("btnSubsetDelete.icon")); // NOI18N
        btnSubsetDelete.setName("btnSubsetDelete"); // NOI18N
        btnSubsetDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSubsetDeleteActionPerformed(evt);
            }
        });
        getContentPane().add(btnSubsetDelete, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 520, 30, -1));

        btnSubsetEdit.setIcon(resourceMap.getIcon("btnSubsetEdit.icon")); // NOI18N
        btnSubsetEdit.setName("btnSubsetEdit"); // NOI18N
        getContentPane().add(btnSubsetEdit, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 520, 30, -1));

        txtFormula.setText(resourceMap.getString("txtFormula.text")); // NOI18N
        txtFormula.setName("txtFormula"); // NOI18N
        getContentPane().add(txtFormula, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 560, 860, 20));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cmbDatabaseItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbDatabaseItemStateChanged
        comboDatabaseChangeEvent(evt, null);
    }//GEN-LAST:event_cmbDatabaseItemStateChanged

    private void cmbDimensionItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbDimensionItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            Object val  = evt.getItem();
            if (val instanceof IDimension) {
                dimension = (IDimension) evt.getItem();
                subsetList1.setDimension(dimension);
                subsetList1.repaint();
            }
        }
    }//GEN-LAST:event_cmbDimensionItemStateChanged

    private void btnSubsetNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSubsetNewActionPerformed
        if (dimension == null)
            return;

        String name = resourceBundle.getString("SubsetNew");
        int cnt = 0, i;
        Subset2 sub = null;
        String [] names = null; //TODO dimension.getSubsetHandler().getSubsetNames();

        if (names != null) {
            do {
                cnt++;
                for (i = 0; i < names.length; i++) {
                    if (names[i].equalsIgnoreCase(name + cnt)) {
                        break;
                    }
                }
            } while (i != names.length);
        }

        //TODO sub = dimension.getSubsetHandler().addSubset(name + cnt, Subset2.TYPE_GLOBAL);
        if (sub != null) {
            subsetList1.setSelectedSubset(sub);
            setSubset(sub);
            subsetList1.repaint();
        }
    }//GEN-LAST:event_btnSubsetNewActionPerformed

    private void btnSubsetDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSubsetDeleteActionPerformed
        if (dimension == null)
            return;

        Subset2 sub = subsetList1.getSelectedSubset();
        if (sub != null)
            //TODO dimension.getSubsetHandler().remove(sub);

        subsetList1.setSelectedSubset(null);
        subsetList1.repaint();

    }//GEN-LAST:event_btnSubsetDeleteActionPerformed

    private void radioViewAsListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioViewAsListActionPerformed
        chkShowHiddenChildren.setSelected(false);
        chkShowHiddenChildren.setEnabled(false);
        setSortOrderAndHierarchy();
    }//GEN-LAST:event_radioViewAsListActionPerformed

    private void radioViewAsHierarchyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioViewAsHierarchyActionPerformed
        chkShowHiddenChildren.setSelected(true);
        chkShowHiddenChildren.setEnabled(true);
        setSortOrderAndHierarchy();
    }//GEN-LAST:event_radioViewAsHierarchyActionPerformed

    private void radioViewParentsBelowChildrenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioViewParentsBelowChildrenActionPerformed
        chkShowHiddenChildren.setSelected(true);
        chkShowHiddenChildren.setEnabled(true);
        setSortOrderAndHierarchy();
    }//GEN-LAST:event_radioViewParentsBelowChildrenActionPerformed

    private void chkReverseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkReverseActionPerformed
        setSortOrderAndHierarchy();
    }//GEN-LAST:event_chkReverseActionPerformed

    private void chkShowHiddenChildrenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkShowHiddenChildrenActionPerformed
        setSortOrderAndHierarchy();
    }//GEN-LAST:event_chkShowHiddenChildrenActionPerformed

    private void radioHighestLevel1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioHighestLevel1ActionPerformed
        Subset2 sub = getSelectedSubset();
        sub.setIndent(1);
        modified();
    }//GEN-LAST:event_radioHighestLevel1ActionPerformed

    private void radioLowestLevel0ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioLowestLevel0ActionPerformed
        Subset2 sub = getSelectedSubset();
        sub.setIndent(2);
        modified();
    }//GEN-LAST:event_radioLowestLevel0ActionPerformed

    private void radioHighestLevel0ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioHighestLevel0ActionPerformed
        Subset2 sub = getSelectedSubset();
        sub.setIndent(3);
        modified();
    }//GEN-LAST:event_radioHighestLevel0ActionPerformed

    private void chkIncludeDuplicatesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkIncludeDuplicatesActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_chkIncludeDuplicatesActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        setVisible(false);
        dispose();
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        // TODO add your handling code here:
        setVisible(false);
        dispose();
    }//GEN-LAST:event_btnCloseActionPerformed

    private void setSortOrderAndHierarchy()
    {
        /*
        SortingFilter setting = (SortingFilter)getSelectedSubset().getFilter(SubsetFilter.TYPE_SORTING);
        if (setting == null)
            return;

        if (chkReverse.isSelected()) {
            if (radioViewAsList.isSelected()) {
                setting.setWhole(SortingFilter.HIERARCHICAL_MODE_DISABLED);
                setting.setReverse(SortingFilter.ORDER_MODE_REVERSE_PER_LEVEL);
            } else if (radioViewAsHierarchy.isSelected()) {
                if (chkShowHiddenChildren.isSelected()) {
                    setting.setWhole(SortingFilter.HIERARCHICAL_MODE_SHOW_CHILDREN);
                    setting.setReverse(SortingFilter.ORDER_MODE_REVERSE_PER_LEVEL);
                } else {
                    setting.setWhole(SortingFilter.HIERARCHICAL_MODE_HIDE_CHILDREN);
                    setting.setReverse(SortingFilter.ORDER_MODE_REVERSE_PER_LEVEL);
                }
            } else {
                if (chkShowHiddenChildren.isSelected()) {
                    setting.setWhole(SortingFilter.HIERARCHICAL_MODE_SHOW_CHILDREN);
                    setting.setReverse(SortingFilter.ORDER_MODE_REVERSE_HIERARCHY); //ORDER_MODE_REVERSE_HIERARCHY
                } else {
                    setting.setWhole(SortingFilter.HIERARCHICAL_MODE_HIDE_CHILDREN);
                    setting.setReverse(SortingFilter.ORDER_MODE_REVERSE_HIERARCHY); //ORDER_MODE_REVERSE_HIERARCHY
                }
            }
        } else {
            if (radioViewAsList.isSelected()) {
                setting.setWhole(SortingFilter.HIERARCHICAL_MODE_DISABLED);
                setting.setReverse(SortingFilter.ORDER_MODE_REVERSE_DISABLED);
            } else if (radioViewAsHierarchy.isSelected()) {
                if (chkShowHiddenChildren.isSelected()) {
                    setting.setWhole(SortingFilter.HIERARCHICAL_MODE_SHOW_CHILDREN);
                    setting.setReverse(SortingFilter.ORDER_MODE_REVERSE_DISABLED);
                } else {
                    setting.setWhole(SortingFilter.HIERARCHICAL_MODE_HIDE_CHILDREN);
                    setting.setReverse(SortingFilter.ORDER_MODE_REVERSE_DISABLED);
                }
            } else {
                if (chkShowHiddenChildren.isSelected()) {
                    setting.setWhole(SortingFilter.HIERARCHICAL_MODE_SHOW_CHILDREN);
                    setting.setReverse(SortingFilter.ORDER_MODE_REVERSE_TOTAL);
                } else {
                    setting.setWhole(SortingFilter.HIERARCHICAL_MODE_HIDE_CHILDREN);
                    setting.setReverse(SortingFilter.ORDER_MODE_REVERSE_TOTAL);
                }
            }
        }
*/
        modified();
    }


    private Subset2 getSelectedSubset() {
        Subset2 sub = subsetList1.getSelectedSubset();
        return sub;
    }

    private void modified() {
        Subset2 sub = getSelectedSubset();
        sub.modified();
        dirty = true;
        updateFormula();
        updatePreview();
    }

    private void updateFormula() {
        String separator = ";";
        String formula = "=PALO.SUBSET(";

        if (database == null || dimension == null)
            return;

        Subset2  subset = getSelectedSubset();
        if (subset == null)
            return;

        formula += "\"" + getConnectionInfo().getHost() + "/";
        formula += database.getName() + "\"" + separator + "\"" ;
        formula += dimension.getName() + "\"";
        formula += dimension.getName() + "\"" + separator;
        formula += subset.getIndent() + separator;

        formula += separator + separator; //....

        /*
        SortingFilter setting = (SortingFilter)subset.getFilter(SubsetFilter.TYPE_SORTING);
        if (setting == null)
            return;

        formula += "PALO.SORT(";
        formula += setting.getWhole() + separator;
        formula += setting.getSortingCriteria() + separator; // sort order
        if (setting.getSortAttribute() != -1)
            formula += setting.getSortAttribute();
        formula += separator;
        formula += setting.getType() + separator;
        if (setting.getSortLevel() != -1)
            formula += setting.getSortLevel();
        formula += separator;
        formula += setting.getReverse() + separator;
        formula += (setting.isShowDuplicates() ? 0 : 1) + ")";

        formula += ")";

        txtFormula.setText(formula);
         * 
         */
        
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnSubsetDelete;
    private javax.swing.JButton btnSubsetEdit;
    private javax.swing.JButton btnSubsetNew;
    private javax.swing.ButtonGroup buttonGroupHierarchyNumbering;
    private javax.swing.ButtonGroup buttonGroupLayout;
    private javax.swing.JCheckBox chkActivateHierarchy;
    private javax.swing.JCheckBox chkActivateText;
    private javax.swing.JCheckBox chkIncludeDuplicates;
    private javax.swing.JCheckBox chkReverse;
    private javax.swing.JCheckBox chkShowHiddenChildren;
    private javax.swing.JComboBox cmbAlias1;
    private javax.swing.JComboBox cmbAlias2;
    private javax.swing.JComboBox cmbDatabase;
    private javax.swing.JComboBox cmbDimension;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JCheckBox jCheckBox4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTree jTree1;
    private javax.swing.JPanel panelAttribute;
    private javax.swing.JPanel panelData;
    private javax.swing.JPanel panelGeneral;
    private javax.swing.JPanel panelHierarchy;
    private javax.swing.JPanel panelPickList;
    private javax.swing.JPanel panelSort;
    private javax.swing.JPanel panelText;
    private javax.swing.JRadioButton radioHighestLevel0;
    private javax.swing.JRadioButton radioHighestLevel1;
    private javax.swing.JRadioButton radioLowestLevel0;
    private javax.swing.JRadioButton radioViewAsHierarchy;
    private javax.swing.JRadioButton radioViewAsList;
    private javax.swing.JRadioButton radioViewParentsBelowChildren;
    private org.palooca.dialogs.SubsetList subsetList1;
    private javax.swing.JLabel txtFormula;
    // End of variables declaration//GEN-END:variables

}
