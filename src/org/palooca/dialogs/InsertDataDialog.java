/*
 * InsertDataDialog.java
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

import com.sun.star.sheet.XCellRangeAddressable;
import com.sun.star.sheet.XCellRangeData;
import com.sun.star.sheet.XCellRangeFormula;
import com.sun.star.table.CellRangeAddress;
import com.sun.star.table.XCellRange;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import java.awt.Component;
import java.awt.Frame;
import java.util.HashMap;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JList;
import com.jedox.palojlib.interfaces.ICube;
import com.jedox.palojlib.interfaces.IDatabase;
import com.jedox.palojlib.interfaces.IDimension;
import com.jedox.palojlib.interfaces.IElement;
import org.palooca.PalOOCaImpl;
import org.palooca.PalOOCaManager;
import org.palooca.formula.ArgumentToken;
import org.palooca.formula.FunctionToken;
import org.palooca.formula.NoopToken;
import org.palooca.formula.OperandToken;
import org.palooca.formula.OperandType;
import org.palooca.network.ConnectionChangeListener;
import org.palooca.network.ConnectionInfo;
import org.palooca.network.ConnectionState;

/**
 *
 * @author Andreas Schneider
 */
public class InsertDataDialog extends PaloDialogUtilities
        implements ConnectionChangeListener
{
    private XCellRange targetRange;
    private DefaultListModel listModel;

    /** Creates new form InsertDataDialog */
    public InsertDataDialog(Frame parent, boolean modal, XComponentContext context,
            XCellRange targetRange) {
        super(parent, modal, context);
        this.context = context;
        this.targetRange = targetRange;
        this.connectionHandler = PalOOCaManager.getInstance(context).getConnectionHandler();
        this.resourceBundle = PalOOCaManager.getInstance(context).getResourceBundle("org/palooca/dialogs/PalOOCaDialogs");

//        setIconImage(new ImageIcon(ViewDialog.class.getResource("/images/InsertFunction.PNG")).getImage());

        listModel = new DefaultListModel();

        initComponents();
        setLocationRelativeTo(parent);

        jComboBoxDatabase.setRenderer(new DatabaseListCellRenderer());
        buildDatabaseList();

        setDatabase(connectionHandler.getLastConnectionInfo(), connectionHandler.getLastDatabase(), null);
        if (database != null)
            selectDatabaseCombo(connectionHandler.getLastConnectionInfo(), database, jComboBoxDatabase);

        setServDB(connectionHandler.getLastConnectionInfo(), connectionHandler.getLastDatabase());
    }

    private void setServDB(ConnectionInfo connectionInfo, IDatabase database) {
        if (connectionInfo != null && database != null) {
            setConnectionInfo(connectionInfo);
            this.database = database;

            listModel.clear();
            ICube[] cubes = database.getCubes();
            for (int i = 0; i < cubes.length; i++) {
                if (cbAttributeCubes.isSelected()) {
                    if (cubes[i].getType().equals(ICube.CubeType.CUBE_ATTRIBUTE)) {
                        IDimension[] dims = cubes[i].getDimensions();
                        int j;
                        for (j = 0; j < dims.length; j++) {
                            if (dims[j].getElements(false).length == 0)
                                break;
                        }
                        if (j == cubes[i].getDimensions().length)
                            listModel.addElement(cubes[i]);
                    }
                } else {
                    if (!cubes[i].getType().equals(ICube.CubeType.CUBE_ATTRIBUTE) &&
                        !cubes[i].getType().equals(ICube.CubeType.CUBE_SYSTEM) &&
                        !cubes[i].getType().equals(ICube.CubeType.CUBE_USERINFO)) {
                        listModel.addElement(cubes[i]);
                    }
                }
            }

            if (listModel.size() > 0)
                lstCubes.setSelectedIndex(0);
        }
    }

    private class CubeListRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            setText(((ICube)value).getName());
            setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/cube.png")));
            return this;
        }

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblDatabase = new javax.swing.JLabel();
        spList = new javax.swing.JScrollPane();
        lstCubes = new javax.swing.JList();
        btnOK = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        cbGuess = new javax.swing.JCheckBox();
        lblCube = new javax.swing.JLabel();
        cbAttributeCubes = new javax.swing.JCheckBox();
        jComboBoxDatabase = new javax.swing.JComboBox();
        cmbFunction = new javax.swing.JComboBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(resourceBundle.getString("Insert_Data_Caption")); // NOI18N
        setAlwaysOnTop(true);
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        lblDatabase.setText(resourceBundle.getString("Database")); // NOI18N

        lstCubes.setModel(listModel);
        lstCubes.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lstCubes.setCellRenderer(new CubeListRenderer());
        lstCubes.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstCubesValueChanged(evt);
            }
        });
        spList.setViewportView(lstCubes);

        btnOK.setText(resourceBundle.getString("Insert")); // NOI18N
        btnOK.setEnabled(false);
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

        cbGuess.setText(resourceBundle.getString("Guess_Parameters")); // NOI18N
        cbGuess.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbGuess.setMargin(new java.awt.Insets(0, 0, 0, 0));

        lblCube.setText(resourceBundle.getString("Cube")); // NOI18N

        cbAttributeCubes.setText(resourceBundle.getString("Show_attribute_cubes")); // NOI18N
        cbAttributeCubes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbAttributeCubes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        cbAttributeCubes.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbAttributeCubesItemStateChanged(evt);
            }
        });
        cbAttributeCubes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbAttributeCubesActionPerformed(evt);
            }
        });

        jComboBoxDatabase.setModel(databaseModel);
        jComboBoxDatabase.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxDatabaseItemStateChanged(evt);
            }
        });

        cmbFunction.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "PALO.DATA", "PALO.DATAC", "PALO.DATAV", "PALO.SETDATA" }));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(spList, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
                    .add(lblDatabase)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(cbGuess)
                            .add(cmbFunction, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 147, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 89, Short.MAX_VALUE)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(btnCancel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(btnOK, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 110, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(lblCube)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 136, Short.MAX_VALUE)
                        .add(cbAttributeCubes))
                    .add(jComboBoxDatabase, 0, 350, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(lblDatabase)
                .add(4, 4, 4)
                .add(jComboBoxDatabase, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cbAttributeCubes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblCube))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(spList, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 340, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(cbGuess)
                        .add(14, 14, 14)
                        .add(cmbFunction, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(btnOK)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnCancel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(15, 15, 15))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        setConnectionInfo(null);
    }//GEN-LAST:event_formWindowClosed

    private void cbAttributeCubesItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbAttributeCubesItemStateChanged
        setServDB(connectionInfo, database);
    }//GEN-LAST:event_cbAttributeCubesItemStateChanged

    private void lstCubesValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstCubesValueChanged
        btnOK.setEnabled(lstCubes.getSelectedIndex() > -1);
    }//GEN-LAST:event_lstCubesValueChanged

    private void btnOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOKActionPerformed
        int selected = lstCubes.getSelectedIndex();
        if (selected > -1) {
            XCellRangeAddressable cellRangeAddressable = (XCellRangeAddressable) UnoRuntime.queryInterface(XCellRangeAddressable.class, targetRange);
            CellRangeAddress address = cellRangeAddressable.getRangeAddress();
            IDimension[] colDimensions = new IDimension[address.EndColumn - address.StartColumn + 1];
            IDimension[] rowDimensions = new IDimension[address.EndRow - address.StartRow + 1];
            HashMap<IDimension, OperandToken> additionalDimensions = new HashMap<IDimension, OperandToken>();

            ICube cube = (ICube) listModel.getElementAt(selected);
            IDimension[] dimensions = cube.getDimensions();

            //Guess Parameters
            if (cbGuess.isSelected()) {
                if (address.StartColumn > 0) {
                    XCellRange headerRange = PalOOCaManager.getInstance(context).getRange(address.StartColumn - 1, address.StartRow, address.StartColumn - 1, address.EndRow);
                    if (headerRange != null) {
                        XCellRangeData cellRangeData = (XCellRangeData) UnoRuntime.queryInterface(XCellRangeData.class, headerRange);
                        Object[][] data = cellRangeData.getDataArray();

                        for (int i = 0; i < data.length; i++)
                            rowDimensions[i] = PalOOCaManager.findDimensionByElementName(cube, PalOOCaImpl.processElementName(data[i][0]));
                    }
                }

                if (address.StartRow > 0) {
                    XCellRange headerRange = PalOOCaManager.getInstance(context).getRange(address.StartColumn, address.StartRow - 1, address.EndColumn, address.StartRow - 1);
                    if (headerRange != null) {
                        XCellRangeData cellRangeData = (XCellRangeData) UnoRuntime.queryInterface(XCellRangeData.class, headerRange);
                        Object[][] data = cellRangeData.getDataArray();

                        for (int i = 0; i < data[0].length; i++)
                            colDimensions[i] = PalOOCaManager.findDimensionByElementName(cube, PalOOCaImpl.processElementName(data[0][i]));
                    }
                }

                if (address.StartColumn > 0 && address.StartRow > 0) {
                    XCellRange headerRange = PalOOCaManager.getInstance(context).getRange(0, 0, address.StartColumn - 1, address.StartRow - 1);
                    if (headerRange != null) {
                        XCellRangeData cellRangeData = (XCellRangeData) UnoRuntime.queryInterface(XCellRangeData.class, headerRange);
                        Object[][] data = cellRangeData.getDataArray();

                        for (int row = 0; row < data.length; row++) {
                            for (int col = 0; col < data[row].length; col++) {
                                if (data[row][col] != null) {
                                    IDimension dimension = PalOOCaManager.findDimensionByElementName(cube, PalOOCaImpl.processElementName(data[row][col]));
                                    if (dimension != null)
                                        additionalDimensions.put(dimension, new OperandToken(OperandType.Range, PalOOCaManager.getCellIdentifier(col, row, true, true)));
                                }
                            }
                        }
                    }
                }
            }

            //Fallback
            HashMap<IDimension, OperandToken> defaultElements = new HashMap<IDimension, OperandToken>();
            for (int i = 0; i < dimensions.length; i++) {
                IElement[] elements = dimensions[i].getElements(false);
                if (elements.length > 0)
                    defaultElements.put(dimensions[i], new OperandToken(OperandType.Text, elements[0].getName()));
                else
                    defaultElements.put(dimensions[i], new OperandToken(OperandType.Text, ""));
            }

            //Fill cells
            XCellRangeFormula cellRangeFormula = (XCellRangeFormula) UnoRuntime.queryInterface(XCellRangeFormula.class, targetRange);
            String[][] formulas = cellRangeFormula.getFormulaArray();
            FunctionToken function;

            switch (cmbFunction.getSelectedIndex()) {
                case 1:
                    function = new FunctionToken("org.palooca.PalOOCa.PALO_DATAC");
                    break;
                case 2:
                    function = new FunctionToken("org.palooca.PalOOCa.PALO_DATAV");
                    break;
                case 3:
                    function = new FunctionToken("org.palooca.PalOOCa.PALO_SETDATA");
                    function.add(new ArgumentToken(function, new OperandToken(OperandType.Number, "value")));
                    function.add(new ArgumentToken(function, new OperandToken(OperandType.Logical, "false")));
                    break;
                default:
                    function = new FunctionToken("org.palooca.PalOOCa.PALO_DATA");
                    break;
            }

            function.add(new ArgumentToken(function, new OperandToken(OperandType.Text, connectionInfo.getName() + "/" + database.getName())));
            function.add(new ArgumentToken(function, new OperandToken(OperandType.Text, cube.getName())));

            int dimensions_start_index = function.size();
            for (int i = 0; i < dimensions.length; i++)
                function.add(new ArgumentToken(function, new NoopToken("")));

            ArgumentToken argument;
            for (int row = 0; row < formulas.length; row++) {
                for (int col = 0; col < formulas[row].length; col++) {
                    for (int i = 0; i < dimensions.length; i++) {
                        argument = (ArgumentToken) function.get(dimensions_start_index + i);
                        if (rowDimensions[row] == dimensions[i])
                            argument.set(0, new OperandToken(OperandType.Range, PalOOCaManager.getCellIdentifier(address.StartColumn - 1, address.StartRow + row, true, false)));
                        else if (colDimensions[col] == dimensions[i])
                            argument.set(0, new OperandToken(OperandType.Range, PalOOCaManager.getCellIdentifier(address.StartColumn + col, address.StartRow - 1, false, true)));
                        else if (additionalDimensions.containsKey(dimensions[i]))
                            argument.set(0, additionalDimensions.get(dimensions[i]));
                        else
                            argument.set(0, defaultElements.get(dimensions[i]));
                    }
                    formulas[row][col] = function.getFormula(true);
                }
            }

            cellRangeFormula.setFormulaArray(formulas);
        }

        setVisible(false);
        dispose();
    }//GEN-LAST:event_btnOKActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        setVisible(false);
        dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void jComboBoxDatabaseItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxDatabaseItemStateChanged
        comboDatabaseChangeEvent(evt, null);
        setServDB(connectionInfo, database);

}//GEN-LAST:event_jComboBoxDatabaseItemStateChanged

    private void cbAttributeCubesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbAttributeCubesActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbAttributeCubesActionPerformed

    @Override
    public void connectionChanged(ConnectionInfo connectionInfo) {
        super.connectionChanged(connectionInfo);

        if (connectionInfo.getState() != ConnectionState.Connected) {
            listModel.clear();
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnOK;
    private javax.swing.JCheckBox cbAttributeCubes;
    private javax.swing.JCheckBox cbGuess;
    private javax.swing.JComboBox cmbFunction;
    private javax.swing.JComboBox jComboBoxDatabase;
    private javax.swing.JLabel lblCube;
    private javax.swing.JLabel lblDatabase;
    private javax.swing.JList lstCubes;
    private javax.swing.JScrollPane spList;
    // End of variables declaration//GEN-END:variables

}
