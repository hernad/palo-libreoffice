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

import com.jedox.palojlib.interfaces.IAttribute;
import com.jedox.palojlib.interfaces.IConnection;
import com.sun.star.uno.XComponentContext;
import java.awt.event.ItemEvent;
import javax.swing.JComboBox;
import com.jedox.palojlib.interfaces.ICube;
import com.jedox.palojlib.interfaces.IDatabase;
import com.jedox.palojlib.interfaces.IDimension;
import com.jedox.palojlib.interfaces.IElement;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.palooca.PalOOCaView;
import org.palooca.RunnableWarning;
import org.palooca.network.ConnectionInfo;
import org.palooca.network.ConnectionState;

/**
 *
 * @author Andreas Schneider
 */
public class ViewDialog extends PaloDialogUtilities
{
    private double columnWidth = 2.9;
    private boolean columnCustomSize = true;
    private boolean columnLineBreak = true;
    private boolean rowInset = true;
    private boolean showElementOnDblClk = false;
    private boolean hideEmptyData = false;
    private boolean countDistinct = false;

    private int functionType = 0;

    /** Creates new form CreateViewDialog */
    public ViewDialog(java.awt.Frame parent, boolean modal, XComponentContext context) {
        super(parent, modal, context);

        init(parent);
        
        setDatabase(connectionHandler.getLastConnectionInfo(), connectionHandler.getLastDatabase(), comboBoxCube);
        if (database != null) {
            selectDatabaseCombo(connectionHandler.getLastConnectionInfo(), database, jComboBoxDatabase);
            //TODO check for autoconnect??
         }

    }

    public ViewDialog(java.awt.Frame parent, boolean modal, XComponentContext context, PalOOCaView view) {
        super(parent, modal, context);

        columnWidth = manager.getColumnWidth();
        columnCustomSize = manager.isColumnCustomSize();
        columnLineBreak = manager.isColumnLineBreak();
        rowInset = manager.isRowInset();
        showElementOnDblClk = manager.isShowElementOnDblClk();
        hideEmptyData = manager.isHideEmptyData();
        countDistinct = manager.isCountDistinct();
        functionType = manager.getFunctionType(); 

        init(parent);

        cube = view.getCube();
        String[] dbStrings = view.getServDB().split("/");
        if (dbStrings.length == 2) {
            IConnection connection = connectionHandler.getConnection(dbStrings[0]);
            if (connection != null) {
                 ConnectionInfo connInfo = connectionHandler.getConnectionInfo(connection);
                 database = connInfo.connect(this).getDatabaseByName(dbStrings[1]);
                 setConnectionInfo(connInfo);
                 selectDatabaseCombo(connInfo, database, jComboBoxDatabase);
            }
            if (connection == null || database == null) {
                 RunnableWarning modalWarning = new RunnableWarning("Could not connect to "+view.getServDB(),connectionHandler.getResourceBundle().getString("Connection_Failed_Caption"));
                 Thread thread = new Thread(modalWarning);
                 thread.start();
                 btnCancelActionPerformed(null);
            }
        } else {
            database = connectionHandler.getLastDatabase();
            setConnectionInfo(connectionHandler.getLastConnectionInfo());
            selectDatabaseCombo(connectionHandler.getLastConnectionInfo(), database, jComboBoxDatabase);
        }

        setTitle(resourceBundle.getString("Edit_View_Caption"));
        btnCreate.setText(resourceBundle.getString("Apply"));

       

        cube = view.getCube();                  // is overwritten so assgin again
        comboBoxCube.setSelectedItem(cube);
        Object obj = comboBoxCube.getSelectedItem();

        jComboBoxFunction.setSelectedIndex(functionType);

//        comboBoxCube.setEnabled(false);
//        jComboBoxDatabase.setEnabled(false);

        dimensionSubsetListFilter.clearDimensions();
        for (int i = 0; i < view.getFilterDimensions().size(); i++) {
            dimensionSubsetListFilter.addDimension(new DimensionSubsetListItem(view.getFilterDimensions().get(i)));
        }
        dimensionSubsetListRow.clearDimensions();
        for (int i = 0; i < view.getRowDimensions().size(); i++) {
            dimensionSubsetListRow.addDimension(new DimensionSubsetListItem(view.getRowDimensions().get(i)));
        }
        dimensionSubsetListColumn.clearDimensions();
        for (int i = 0; i < view.getColumnDimensions().size(); i++) {
            dimensionSubsetListColumn.addDimension(new DimensionSubsetListItem(view.getColumnDimensions().get(i)));
        }
    }

    private void init(java.awt.Frame parent) {
//        setIconImage(new ImageIcon(ViewDialog.class.getResource("/images/InsertView.PNG")).getImage());

        initComponents();

        dimensionSubsetListFilter.setFilter(true);
        dimensionSubsetListFilter.setContext(context);
        dimensionSubsetListRow.setContext(context);
        dimensionSubsetListColumn.setContext(context);

        jComboBoxDatabase.setRenderer(new DatabaseListCellRenderer());
        jComboBoxFunction.setSelectedIndex(1);

        buildDatabaseList();

        setLocationRelativeTo(parent);
    }

    @Override
    protected void setDatabase(ConnectionInfo connectionInfoNew, IDatabase databaseNew, JComboBox comboBoxCube) {
        super.setDatabase(connectionInfoNew, databaseNew, comboBoxCube);

        if (connectionInfoNew != null && databaseNew != null) {
            btnDataCubes.setEnabled(true);
            btnAttributeCubes.setEnabled(true);
//            btnUserCubes.setEnabled(true);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btngCubeType = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jCheckBoxRowInset = new javax.swing.JCheckBox();
        dimensionSubsetListRow = new org.palooca.dialogs.DimensionSubsetList();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        dimensionSubsetListFilter = new org.palooca.dialogs.DimensionSubsetList();
        jPanel4 = new javax.swing.JPanel();
        lblDatabase = new javax.swing.JLabel();
        lblCube = new javax.swing.JLabel();
        comboBoxCube = new javax.swing.JComboBox();
        btnDataCubes = new javax.swing.JToggleButton();
        btnAttributeCubes = new javax.swing.JToggleButton();
        jComboBoxDatabase = new javax.swing.JComboBox();
        refreshButton = new javax.swing.JButton();
        btnCreate = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jComboBoxFunction = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jCheckBoxZeroSuppression = new javax.swing.JCheckBox();
        jCheckBoxElementOnDblClk = new javax.swing.JCheckBox();
        jCheckBoxCountDistinct = new javax.swing.JCheckBox();
        jCheckBoxHideIds = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        dimensionSubsetListColumn = new org.palooca.dialogs.DimensionSubsetList();
        jCheckBoxColumnWidth = new javax.swing.JCheckBox();
        jTextColumnWidth = new javax.swing.JTextField();
        jCheckBoxLineBreak = new javax.swing.JCheckBox();
        jPanel6 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(resourceBundle.getString("Create_View_Caption")); // NOI18N
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

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceBundle.getString("Rows"))); // NOI18N

        jCheckBoxRowInset.setSelected(isRowInset());
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/palooca/dialogs/PalOOCaDialogs"); // NOI18N
        jCheckBoxRowInset.setText(bundle.getString("RowInset")); // NOI18N
        jCheckBoxRowInset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxRowInsetActionPerformed(evt);
            }
        });

        dimensionSubsetListRow.setBackground(new java.awt.Color(255, 255, 255));
        dimensionSubsetListRow.setAutoscrolls(true);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jCheckBoxRowInset)
                    .add(dimensionSubsetListRow, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(dimensionSubsetListRow, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 167, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jCheckBoxRowInset)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 340, 280, 230));

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceBundle.getString("Filter"))); // NOI18N

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        dimensionSubsetListFilter.setAutoscrolls(true);
        dimensionSubsetListFilter.setMaximumSize(new java.awt.Dimension(200, 170));
        dimensionSubsetListFilter.setPreferredSize(new java.awt.Dimension(200, 170));
        jScrollPane1.setViewportView(dimensionSubsetListFilter);

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(28, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 100, 280, 230));
        jPanel3.getAccessibleContext().setAccessibleName(""); // NOI18N

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceBundle.getString("Source"))); // NOI18N

        lblDatabase.setText(resourceBundle.getString("SelectDatabase")); // NOI18N

        lblCube.setText(resourceBundle.getString("SelectCube")); // NOI18N

        comboBoxCube.setModel(cubeModel);
        comboBoxCube.setRenderer(new CubeComboBoxRenderer());
        comboBoxCube.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comboBoxCubeItemStateChanged(evt);
            }
        });
        comboBoxCube.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxCubeActionPerformed(evt);
            }
        });

        btngCubeType.add(btnDataCubes);
        btnDataCubes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/cube.png"))); // NOI18N
        btnDataCubes.setSelected(true);
        btnDataCubes.setToolTipText(resourceBundle.getString("DataCubes")); // NOI18N
        btnDataCubes.setEnabled(false);
        btnDataCubes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDataCubesActionPerformed(evt);
            }
        });

        btngCubeType.add(btnAttributeCubes);
        btnAttributeCubes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/attribute.png"))); // NOI18N
        btnAttributeCubes.setToolTipText(resourceBundle.getString("AttributeCubes")); // NOI18N
        btnAttributeCubes.setEnabled(false);
        btnAttributeCubes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAttributeCubesActionPerformed(evt);
            }
        });

        jComboBoxDatabase.setModel(databaseModel);
        jComboBoxDatabase.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxDatabaseItemStateChanged(evt);
            }
        });

        refreshButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/serverRefresh.png"))); // NOI18N
        refreshButton.setMaximumSize(new java.awt.Dimension(32, 32));
        refreshButton.setMinimumSize(new java.awt.Dimension(32, 32));
        refreshButton.setPreferredSize(new java.awt.Dimension(32, 32));
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lblDatabase)
                    .add(jPanel4Layout.createSequentialGroup()
                        .add(jComboBoxDatabase, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 241, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(refreshButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 31, Short.MAX_VALUE)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel4Layout.createSequentialGroup()
                        .add(comboBoxCube, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 176, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(btnDataCubes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnAttributeCubes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(lblCube))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel4Layout.createSequentialGroup()
                        .add(lblCube)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(comboBoxCube, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(btnAttributeCubes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(btnDataCubes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(jPanel4Layout.createSequentialGroup()
                        .add(lblDatabase)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(refreshButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jComboBoxDatabase, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 570, 80));

        btnCreate.setText(resourceBundle.getString("Create")); // NOI18N
        btnCreate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCreateActionPerformed(evt);
            }
        });
        getContentPane().add(btnCreate, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 20, 90, -1));

        btnCancel.setText(resourceBundle.getString("Cancel")); // NOI18N
        btnCancel.setDefaultCapable(false);
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });
        getContentPane().add(btnCancel, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 50, 90, -1));

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceBundle.getString("Data"))); // NOI18N

        jComboBoxFunction.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "PALO.DATA", "PALO.DATAC", "PALO.DATAV" }));
        jComboBoxFunction.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxFunctionActionPerformed(evt);
            }
        });

        jLabel1.setText(bundle.getString("FormulaType")); // NOI18N

        jCheckBoxZeroSuppression.setSelected(isHideEmptyData());
        jCheckBoxZeroSuppression.setText(bundle.getString("ZeroSuppression")); // NOI18N

        jCheckBoxElementOnDblClk.setSelected(isShowElementOnDblClk());
        jCheckBoxElementOnDblClk.setText(bundle.getString("ElementSelection")); // NOI18N

        jCheckBoxCountDistinct.setSelected(isCountDistinct());
        jCheckBoxCountDistinct.setText("Count Distinct 1st Filter");

        jCheckBoxHideIds.setText("Hide Ids on Attribute Use");
        jCheckBoxHideIds.setActionCommand("hideIdsOnAttributeUse");
        jCheckBoxHideIds.setSelected(manager.isHideIdsOnAttributeUse());

        org.jdesktop.layout.GroupLayout jPanel5Layout = new org.jdesktop.layout.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jCheckBoxHideIds)
                    .add(jCheckBoxCountDistinct)
                    .add(jPanel5Layout.createSequentialGroup()
                        .add(jLabel1)
                        .add(43, 43, 43)
                        .add(jComboBoxFunction, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 107, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jCheckBoxElementOnDblClk)
                    .add(jCheckBoxZeroSuppression))
                .addContainerGap(17, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .add(22, 22, 22)
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(jComboBoxFunction, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(jCheckBoxElementOnDblClk)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jCheckBoxZeroSuppression)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jCheckBoxCountDistinct)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jCheckBoxHideIds)
                .addContainerGap(22, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 340, 280, 230));

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceBundle.getString("Columns"))); // NOI18N

        dimensionSubsetListColumn.setAutoscrolls(true);

        jCheckBoxColumnWidth.setSelected(isColumnCustomSize());
        jCheckBoxColumnWidth.setText(bundle.getString("ColumnWidth")); // NOI18N

        jTextColumnWidth.setText(getColumnWidth());
        jTextColumnWidth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextColumnWidthActionPerformed(evt);
            }
        });

        jCheckBoxLineBreak.setSelected(isColumnLineBreak());
        jCheckBoxLineBreak.setText(bundle.getString("LineBreak")); // NOI18N
        jCheckBoxLineBreak.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxLineBreakActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(dimensionSubsetListColumn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 248, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(jCheckBoxLineBreak)
                        .add(29, 29, 29)
                        .add(jCheckBoxColumnWidth)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jTextColumnWidth, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 52, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                .add(dimensionSubsetListColumn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 159, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jCheckBoxLineBreak)
                    .add(jTextColumnWidth, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jCheckBoxColumnWidth))
                .addContainerGap())
        );

        getContentPane().add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 100, 280, 230));

        org.jdesktop.layout.GroupLayout jPanel6Layout = new org.jdesktop.layout.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 40, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 40, Short.MAX_VALUE)
        );

        getContentPane().add(jPanel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 540, 40, 40));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        setVisible(false);
        dispose();
}//GEN-LAST:event_btnCancelActionPerformed

    private void btnCreateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCreateActionPerformed
        //Create a plain view
        PalOOCaView view = new PalOOCaView(context);
        view.setServDB(connectionInfo.getName() + "/" + database.getName());
        view.setCube(cube);
        manager.setColumnLineBreak(jCheckBoxLineBreak.isSelected());
        manager.setColumnCustomSize(jCheckBoxColumnWidth.isSelected());
        manager.setColumnWidth(Double.parseDouble(jTextColumnWidth.getText()));
        manager.setRowInset(jCheckBoxRowInset.isSelected());
        boolean countDistinct = jCheckBoxCountDistinct.isSelected() && dimensionSubsetListFilter.getDimensionCount() > 0;
        manager.setHideEmptyData(jCheckBoxZeroSuppression.isSelected() || countDistinct);
        manager.setCountDistinct(countDistinct);
        manager.setHideIdsOnAttributeUse(jCheckBoxHideIds.isSelected());
        manager.setShowElementOnDblClk(jCheckBoxElementOnDblClk.isSelected());
        manager.setFunctionType(jComboBoxFunction.getSelectedIndex());

        view.getFilterDimensions().clear();
        for (int i = 0; i < dimensionSubsetListFilter.getDimensionCount(); i++) {
            view.getFilterDimensions().add(new DimensionSubsetListItem(dimensionSubsetListFilter.getDimension(i)));
        }

        view.getColumnDimensions().clear();
        for (int i = 0; i < dimensionSubsetListColumn.getDimensionCount(); i++) {
            view.getColumnDimensions().add(new DimensionSubsetListItem(dimensionSubsetListColumn.getDimension(i)));
        }
        
        view.getRowDimensions().clear();
        for (int i = 0; i < dimensionSubsetListRow.getDimensionCount(); i++) {
            view.getRowDimensions().add(new DimensionSubsetListItem(dimensionSubsetListRow.getDimension(i)));
        }
         
        view.generate(manager.getActiveSpreadSheet());

        setVisible(false);
        dispose();
}//GEN-LAST:event_btnCreateActionPerformed

    private void jCheckBoxLineBreakActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxLineBreakActionPerformed
        
}//GEN-LAST:event_jCheckBoxLineBreakActionPerformed

    private void jCheckBoxRowInsetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxRowInsetActionPerformed
        
}//GEN-LAST:event_jCheckBoxRowInsetActionPerformed

    private void jComboBoxFunctionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxFunctionActionPerformed
        
}//GEN-LAST:event_jComboBoxFunctionActionPerformed

    private void btnAttributeCubesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAttributeCubesActionPerformed
        buildCubeList(ICube.CubeType.CUBE_ATTRIBUTE, comboBoxCube);
}//GEN-LAST:event_btnAttributeCubesActionPerformed

    private void btnDataCubesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDataCubesActionPerformed
        buildCubeList(ICube.CubeType.CUBE_NORMAL, comboBoxCube);
}//GEN-LAST:event_btnDataCubesActionPerformed

    private void comboBoxCubeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comboBoxCubeItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            cube = (ICube) evt.getItem();
            IDimension[] dimensions = cube.getDimensions();

            dimensionSubsetListRow.clearDimensions();
            if (dimensions.length > 0)
                dimensionSubsetListRow.addDimension(getConnectionInfo(),getDatabase(),dimensions[0]);

            dimensionSubsetListColumn.clearDimensions();
            if (dimensions.length > 1)
                dimensionSubsetListColumn.addDimension(getConnectionInfo(),getDatabase(),dimensions[1]);

            dimensionSubsetListFilter.clearDimensions();
            for (int i = 2; i < dimensions.length; i++) {
                dimensionSubsetListFilter.addDimension(getConnectionInfo(),getDatabase(),dimensions[i]);
            }

            dimensionSubsetListFilter.repaint();
            dimensionSubsetListColumn.repaint();
            dimensionSubsetListRow.repaint();
        } else {
            cube = null;
        }
}//GEN-LAST:event_comboBoxCubeItemStateChanged

    private void jComboBoxDatabaseItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxDatabaseItemStateChanged
        comboDatabaseChangeEvent(evt, comboBoxCube);
    }//GEN-LAST:event_jComboBoxDatabaseItemStateChanged

    private void comboBoxCubeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxCubeActionPerformed
        
}//GEN-LAST:event_comboBoxCubeActionPerformed

    private void jTextColumnWidthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextColumnWidthActionPerformed

}//GEN-LAST:event_jTextColumnWidthActionPerformed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        if (!manager.getOpenDialogs().isEmpty()) {
            setVisible(false);
            dispose();
        } else {
            manager.getOpenDialogs().add(this);
            this.requestFocus();
        }
    }//GEN-LAST:event_formWindowOpened

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        manager.getOpenDialogs().remove(this);
    }//GEN-LAST:event_formWindowClosed

    private Set<String> getElementSet(IDimension dimension) {
        Set<String> result = new HashSet<String>();
        for (IElement element : dimension.getElements(true)) {
            result.add(element.getName());
        }
        return result;
    }

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        Object item = databaseModel.getSelectedItem();
        int index = databaseModel.getIndexOf(item);
        ConnectionInfo info = null;
        if (item instanceof ConnectionInfo) {
            info = (ConnectionInfo)item;
        }
        if (item instanceof DatabaseInfo) {
            DatabaseInfo dbInfo = (DatabaseInfo)item;
            info = dbInfo.getConnInfo();
        }
        if (info != null) {
            if (!info.getState().equals(ConnectionState.Connected)) {
                info.connect(1000);
                if (!info.getState().equals(ConnectionState.Connected)) {
                    return;
                }
            } else {
                if (info.getConnection() instanceof com.jedox.palojlib.main.Connection) {
                    com.jedox.palojlib.main.Connection conn = (com.jedox.palojlib.main.Connection)info.getConnection();
                    conn.resetCache();
                    conn.getDatabases();
                } else {
                    info.getConnection().close();
                    info.getConnection().open();
                }
            }
        } else {
            return;
        }

        if (item instanceof DatabaseInfo) {
            boolean initNew = false;
            Collection<DimensionSubsetListItem> rowDims = dimensionSubsetListRow.getDimensions();
            Collection<DimensionSubsetListItem> colDims = dimensionSubsetListColumn.getDimensions();
            Collection<DimensionSubsetListItem> filterDims = dimensionSubsetListFilter.getDimensions();
            String databaseName = ((DatabaseInfo)item).getDatabase().getName();
            database = info.getConnection().getDatabaseByName(databaseName);
            if (database != null) {
                if (cube != null) cube = database.getCubeByName(cube.getName());
                if (cube != null) {
                    for (DimensionSubsetListItem d : rowDims) {
                        IDimension updatedDimension = cube.getDimensionByName(d.getDimension().getName());
                        if (d.getAttribute() != null) {
                            IAttribute updatedAttribute = updatedDimension.getAttributeByName(d.getAttribute().getName());
                            d.setAttribute(updatedAttribute);
                        }
                        if (updatedDimension == null) {
                            initNew = true;
                            break;
                        }
                        else {
                            d.setDatabase(database);
                            d.setDimension(updatedDimension);
                            d.updateElements(getElementSet(updatedDimension),false,true);
                        }
                    }
                    if (!initNew) for (DimensionSubsetListItem d : colDims) {
                        IDimension updatedDimension = cube.getDimensionByName(d.getDimension().getName());
                        if (d.getAttribute() != null) {
                            IAttribute updatedAttribute = updatedDimension.getAttributeByName(d.getAttribute().getName());
                            d.setAttribute(updatedAttribute);
                        }
                        if (updatedDimension == null) {
                            initNew = true;
                            break;
                        }
                        else {
                            d.setDatabase(database);
                            d.setDimension(updatedDimension);
                            d.updateElements(getElementSet(updatedDimension),false,true);
                        }
                    }
                    if (!initNew) for (DimensionSubsetListItem d : filterDims) {
                       IDimension updatedDimension = cube.getDimensionByName(d.getDimension().getName());
                        if (d.getAttribute() != null) {
                            IAttribute updatedAttribute = updatedDimension.getAttributeByName(d.getAttribute().getName());
                            d.setAttribute(updatedAttribute);
                        }
                        if (updatedDimension == null) {
                            initNew = true;
                            break;
                        }
                        else {
                            d.setDatabase(database);
                            d.setDimension(updatedDimension);
                            d.updateElements(getElementSet(updatedDimension),false,true);
                        }
                    }
                }
            }
            if (initNew) {
                //set connection item for new init
                int i = index;
                item = null;
                while (i > 0) {
                    if (i < databaseModel.getSize()) {
                        if (databaseModel.getElementAt(i) instanceof ConnectionInfo) {
                            item = databaseModel.getElementAt(i);
                            break;
                        }
                    }
                    i--;
                }
                index --;
            }
        }

        if (item instanceof ConnectionInfo) {
            resetDatabase();
            buildDatabaseList();
            if (databaseModel.getSize() > index+1 && databaseModel.getElementAt(index+1) instanceof DatabaseInfo) {
                DatabaseInfo dbInfo = (DatabaseInfo)databaseModel.getElementAt(index+1);
                databaseModel.setSelectedItem(dbInfo);
                setDatabase(info, dbInfo.getDatabase(), comboBoxCube);
                item = dbInfo;
            }
            databaseModel.setSelectedItem(item);
            jComboBoxDatabase.showPopup();
        }
    }//GEN-LAST:event_refreshButtonActionPerformed

    @Override
    protected void resetDatabase()
    {
        super.resetDatabase();
        
        cube = null;
        comboBoxCube.setEnabled(false);
        btnDataCubes.setEnabled(false);
        btnAttributeCubes.setEnabled(false);
//        btnUserCubes.setEnabled(false);

        dimensionSubsetListRow.clearDimensions();
        dimensionSubsetListColumn.clearDimensions();
        dimensionSubsetListFilter.clearDimensions();
        dimensionSubsetListFilter.repaint();
        dimensionSubsetListColumn.repaint();
        dimensionSubsetListRow.repaint();
    }

    @Override
    public void connectionChanged(ConnectionInfo connectionInfo) {
        super.connectionChanged(connectionInfo);
        if (connectionInfo.getState() != ConnectionState.Connected) {
            btnDataCubes.setEnabled(false);
            btnAttributeCubes.setEnabled(false);
//            btnUserCubes.setEnabled(false);
        }
    }

    public int getFunctionType() {
        return functionType;
    }

    public void setFunctionType(int functionType) {
        this.functionType = functionType;
    }

    public boolean isHideEmptyData() {
        return hideEmptyData;
    }

    public void setHideEmptyData(boolean hideEmptyData) {
        this.hideEmptyData = hideEmptyData;
    }

    public boolean isShowElementOnDblClk() {
        return showElementOnDblClk;
    }

    public void setShowElementOnDblClk(boolean showElementOnDblClk) {
        this.showElementOnDblClk = showElementOnDblClk;
    }

    public boolean isColumnLineBreak() {
        return columnLineBreak;
    }

    public void setColumnLineBreak(boolean columnLineBreak) {
        this.columnLineBreak = columnLineBreak;
    }

    public boolean isRowInset() {
        return rowInset;
    }

    public void setRowInset(boolean rowInset) {
        this.rowInset = rowInset;
    }

    public boolean isColumnCustomSize() {
        return columnCustomSize;
    }

    public void setColumnCustomSize(boolean columnCustomSize) {
        this.columnCustomSize = columnCustomSize;
    }

    public String getColumnWidth() {
        return ((Double)columnWidth).toString();
    }

    public boolean isCountDistinct() {
        return countDistinct;
    }

    public void setCountDistinct(boolean countDistinct) {
        this.countDistinct = countDistinct;
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton btnAttributeCubes;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnCreate;
    private javax.swing.JToggleButton btnDataCubes;
    private javax.swing.ButtonGroup btngCubeType;
    private javax.swing.JComboBox comboBoxCube;
    private org.palooca.dialogs.DimensionSubsetList dimensionSubsetListColumn;
    private org.palooca.dialogs.DimensionSubsetList dimensionSubsetListFilter;
    private org.palooca.dialogs.DimensionSubsetList dimensionSubsetListRow;
    private javax.swing.JCheckBox jCheckBoxColumnWidth;
    private javax.swing.JCheckBox jCheckBoxCountDistinct;
    private javax.swing.JCheckBox jCheckBoxElementOnDblClk;
    private javax.swing.JCheckBox jCheckBoxHideIds;
    private javax.swing.JCheckBox jCheckBoxLineBreak;
    private javax.swing.JCheckBox jCheckBoxRowInset;
    private javax.swing.JCheckBox jCheckBoxZeroSuppression;
    private javax.swing.JComboBox jComboBoxDatabase;
    private javax.swing.JComboBox jComboBoxFunction;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextColumnWidth;
    private javax.swing.JLabel lblCube;
    private javax.swing.JLabel lblDatabase;
    private javax.swing.JButton refreshButton;
    // End of variables declaration//GEN-END:variables

}
