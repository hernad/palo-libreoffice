/*
 * PalOOCaSheetReplicator.java
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
 * Created on 13. August 2008, 12:13
 */

package org.palooca;

import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XIndexContainer;
import com.sun.star.container.XNameContainer;
import com.sun.star.drawing.XDrawPage;
import com.sun.star.drawing.XDrawPages;
import com.sun.star.drawing.XDrawPagesSupplier;
import com.sun.star.form.XFormsSupplier;
import com.sun.star.sheet.CellFlags;
import com.sun.star.sheet.XCellRangeData;
import com.sun.star.sheet.XCellRangeFormula;
import com.sun.star.sheet.XCellRangesQuery;
import com.sun.star.sheet.XSheetCellRanges;
import com.sun.star.sheet.XSpreadsheetDocument;
import com.sun.star.sheet.XSpreadsheets;
import com.sun.star.table.CellRangeAddress;
import com.sun.star.table.XCell;
import com.sun.star.table.XCellRange;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import org.palooca.dialogs.ModalState;
import org.palooca.dialogs.SnapshotSettingsDialog;
import org.palooca.formula.FormulaParser;
import org.palooca.formula.FunctionToken;
import org.palooca.formula.OperandToken;
import org.palooca.formula.OperandType;
import org.palooca.formula.Token;

/**
 *
 * @author Andreas Schneider
 */
public class PalOOCaSheetReplicator {

    public static void replicateDocument(XComponentContext context) {
        
        try {
        
            PalOOCaManager manager = PalOOCaManager.getInstance(context);
            
            // Prepare the spreadsheet documents

            XSpreadsheetDocument xSpreadsheetDoc = manager.getActiveDocument();
            if (xSpreadsheetDoc == null)
                return;

            XSpreadsheetDocument xNewDocument = manager.createNewSheetComponent();
            if (xNewDocument == null)
                return;

            // Get access to the sheets and synchronize them

            XSpreadsheets xSheets = xSpreadsheetDoc.getSheets();
            XSpreadsheets xNewSheets = xNewDocument.getSheets();

            String[] sheetElements = xSheets.getElementNames();
            short sheetCount = (short) sheetElements.length;
            
            for (short i = 0; i < sheetCount; i++) {
                if (xNewSheets.hasByName(sheetElements[i]))
                    xNewSheets.removeByName(sheetElements[i]);
                xNewSheets.insertNewByName(sheetElements[i], i);
            }
            
            String[] newSheetElements = xNewSheets.getElementNames();
            short newSheetCount = (short) newSheetElements.length;
            
            for (short i = sheetCount; i < newSheetCount; i++) {
                xNewSheets.removeByName(newSheetElements[i]);
            }
            
            //TODO : also set the active sheet
            
            // Transfer data from the original sheet to the new one (with filter)
            
            for (int i = 0; i < sheetCount; i++) {
                XCellRangesQuery xCellRangesQuery = (XCellRangesQuery) UnoRuntime.queryInterface(XCellRangesQuery.class, xSheets.getByName(sheetElements[i]));
                XSheetCellRanges xSheetCellRanges = xCellRangesQuery.queryContentCells((short)(0xFFFF & ~CellFlags.FORMULA)); //get all cells that don't require a filter
                CellRangeAddress[] cellRangeAddresses = xSheetCellRanges.getRangeAddresses();
                
                // Handle the cell ranges ...
                for (int j = 0; j < xSheetCellRanges.getCount(); j++) {
                    CellRangeAddress address = cellRangeAddresses[j];
                    
                    // Get the source data
                    Object oSheetCellRange = xSheetCellRanges.getByIndex(j);
                    XCellRangeData xCellRangeData = (XCellRangeData) UnoRuntime.queryInterface(XCellRangeData.class, oSheetCellRange);
                    Object[][] sourceData = xCellRangeData.getDataArray();
                    
                    // Set the target data
                    XCellRange sheetCellRange = (XCellRange) UnoRuntime.queryInterface(XCellRange.class, xNewSheets.getByName(sheetElements[i]));
                    XCellRange targetCellRange = sheetCellRange.getCellRangeByPosition(address.StartColumn, address.StartRow, address.EndColumn, address.EndRow);
                    XCellRangeData targetCellRangeData = (XCellRangeData) UnoRuntime.queryInterface(XCellRangeData.class, targetCellRange);
                    targetCellRangeData.setDataArray(sourceData);
                }
            }
        
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }
    
    public static void freezeDocument(XComponentContext context) {
        try {
            
            PalOOCaManager manager = PalOOCaManager.getInstance(context);
            
            // Prepare the spreadsheet document

            XSpreadsheetDocument xSpreadsheetDoc = manager.getActiveDocument();
            if (xSpreadsheetDoc == null)
                return;
            
            // Query the user for specific settings
            
            SnapshotSettingsDialog snapshotSettingsDialog = new SnapshotSettingsDialog(context);
            if ( snapshotSettingsDialog.showModal() != ModalState.OK )
                return;
            
            if (snapshotSettingsDialog.cbRemoveFunctions.isChecked()) {
                
                boolean removePaloFunctionsOnly = snapshotSettingsDialog.cbRemovePaloFunctionsOnly.isChecked();

                // Get access to the sheets and remove any formulas

                XSpreadsheets xSheets = xSpreadsheetDoc.getSheets();

                String[] sheetElements = xSheets.getElementNames();
                short sheetCount = (short) sheetElements.length;

                for (int i = 0; i < sheetCount; i++) {
                    XCellRangesQuery xCellRangesQuery = (XCellRangesQuery) UnoRuntime.queryInterface(XCellRangesQuery.class, xSheets.getByName(sheetElements[i]));
                    XSheetCellRanges xSheetCellRanges = xCellRangesQuery.queryContentCells((short)(CellFlags.FORMULA)); //get all cells that contain a formula

                    // Handle the cell ranges ...
                    for (int j = 0; j < xSheetCellRanges.getCount(); j++) {
                        Object oSheetCellRange = xSheetCellRanges.getByIndex(j);
                        if (removePaloFunctionsOnly) {
                            XCellRange xCellRange = (XCellRange) UnoRuntime.queryInterface(XCellRange.class, oSheetCellRange);
                            XCellRangeFormula xCellRangeFormula = (XCellRangeFormula) UnoRuntime.queryInterface(XCellRangeFormula.class, oSheetCellRange);
                            String[][] formulas = xCellRangeFormula.getFormulaArray();
                            
                            for (int y = 0; y < formulas.length; y++) {
                                for (int x = 0; x < formulas[y].length; x++) {
                                    /*Token formula = FormulaParser.parseFormula(formulas[x][y]);
                                    boolean found = false;
                                    for (int k = 0; k < formula.size() && !found; k++) {
                                        Token token = formula.get(k);
                                        if (token instanceof FunctionToken && token.getContent().startsWith("org.palooca.PalOOCa.")) {
                                            XCell xCell = xCellRange.getCellByPosition(x, y);
                                            Object value = token.calculate(xCell);
                                            
                                            if (k == 0) { //first (and now only) token {
                                                XCellRangeData xCellRangeData = (XCellRangeData) UnoRuntime.queryInterface(XCellRangeData.class, xCell);
                                                Object[][] data = new Object[1][1];
                                                data[0][0] = value;
                                                xCellRangeData.setDataArray(data);
                                            } else {
                                                OperandToken operand;
                                                if (value instanceof String) {
                                                    operand = new OperandToken(OperandType.Text, (String)value);
                                                } else {
                                                    operand = new OperandToken(value.toString());
                                                }
                                                
                                                Token parentToken = formula.get(k - 1); WRONG!!! --> Hierarchy
                                            }
                                            
                                            found = true;
                                        }
                                    }*/ //Easy solution for now...
                                    if (formulas[y][x].contains("org.palooca.PalOOCa.")) {
                                        XCellRangeData xCellRangeData = (XCellRangeData) UnoRuntime.queryInterface(XCellRangeData.class, xCellRange.getCellByPosition(x, y));
                                        xCellRangeData.setDataArray(xCellRangeData.getDataArray());
                                    }
                                }
                            }
                        } else {
                            XCellRangeData xCellRangeData = (XCellRangeData) UnoRuntime.queryInterface(XCellRangeData.class, oSheetCellRange);
                            xCellRangeData.setDataArray(xCellRangeData.getDataArray());
                        }
                    }
                }
                
            }
            
            if (snapshotSettingsDialog.cbDisableFormFields.isChecked()) {
                
                // Get the draw pages supplier
                
                XDrawPagesSupplier drawPagesSupplier = (XDrawPagesSupplier) UnoRuntime.queryInterface(XDrawPagesSupplier.class, xSpreadsheetDoc);
                if (drawPagesSupplier != null) {
                    XDrawPages drawPages = drawPagesSupplier.getDrawPages();
                    for (int i = 0; i < drawPages.getCount(); i++) {
                        XDrawPage drawPage = (XDrawPage) UnoRuntime.queryInterface(XDrawPage.class, drawPages.getByIndex(i));
                        XFormsSupplier formsSupplier = (XFormsSupplier) UnoRuntime.queryInterface(XFormsSupplier.class, drawPage);
                        if (formsSupplier != null) {
                            XIndexContainer forms = (XIndexContainer) UnoRuntime.queryInterface(XIndexContainer.class, formsSupplier.getForms());
                            for (int j = 0; j < forms.getCount(); j++) {
                                XIndexContainer controls = (XIndexContainer) UnoRuntime.queryInterface(XIndexContainer.class, forms.getByIndex(j));
                                for (int k = 0; k < controls.getCount(); k++) {
                                    XPropertySet xPropertySet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, controls.getByIndex(k));
                                    if (xPropertySet != null) {
                                        try {
                                            xPropertySet.setPropertyValue("Enabled", new Boolean(false));
                                        } catch (UnknownPropertyException e) {
                                            
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
            }

        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

}
