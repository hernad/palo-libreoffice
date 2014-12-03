/*
 * PalOOCaView.java
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
 * Created on 4. Oktober 2007, 15:20
 *
 */

package org.palooca;

import com.sun.star.awt.FontUnderline;
import com.sun.star.awt.FontWeight;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XNameContainer;
import com.sun.star.document.XActionLockable;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.sheet.XCellRangeFormula;
import com.sun.star.sheet.XNamedRange;
import com.sun.star.sheet.XSheetOperation;
import com.sun.star.sheet.XSpreadsheet;
import com.sun.star.sheet.XSpreadsheetDocument;
import com.sun.star.table.CellHoriJustify;
import com.sun.star.table.CellVertJustify;
import com.sun.star.table.XCell;
import com.sun.star.table.XCellRange;
import com.sun.star.table.XColumnRowRange;
import com.sun.star.table.XTableColumns;
import com.sun.star.table.XTableRows;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.XMergeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import com.jedox.palojlib.interfaces.*;
import com.jedox.palojlib.exceptions.*;
import com.jedox.palojlib.main.CellExportContext;
import com.sun.star.frame.XModel;
import com.sun.star.sheet.CellFlags;
import com.sun.star.sheet.XCalculatable;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.palooca.dialogs.DimensionSubsetListItem;
import org.palooca.dialogs.PaloDialogUtilities;
import org.palooca.formula.ArgumentToken;
import org.palooca.formula.FormulaParser;
import org.palooca.formula.FunctionToken;
import org.palooca.formula.NoopToken;
import org.palooca.formula.OperandToken;
import org.palooca.formula.OperandType;
import org.palooca.formula.Token;
import org.palooca.h2.OlapAggregator;
import org.palooca.network.ConnectionHandler;
import org.palooca.network.ConnectionInfo;

/**
 *
 * @author Andreas Schneider
 */
public class PalOOCaView {
    
    private XComponentContext context;
    private PalOOCaManager manager;
    private String servDB;
    private ICube cube;
    private Vector<DimensionSubsetListItem> filterDimensions = new Vector<DimensionSubsetListItem>();
    private Vector<DimensionSubsetListItem> rowDimensions = new Vector<DimensionSubsetListItem>();
    private Vector<DimensionSubsetListItem> columnDimensions = new Vector<DimensionSubsetListItem>();

    private long time;

    public Vector<DimensionSubsetListItem> getColumnDimensions() {
        return columnDimensions;
    }

    public Vector<DimensionSubsetListItem> getFilterDimensions() {
        return filterDimensions;
    }

    public Vector<DimensionSubsetListItem> getRowDimensions() {
        return rowDimensions;
    }

    public String getServDB() {
        return servDB;
    }
    
    public void setServDB(String servDB) {
        this.servDB = servDB;
    }
    
    public ICube getCube() {
        return cube;
    }
    
    public void setCube(ICube cube) {
        this.cube = cube;
    }
    
    public PalOOCaView(XComponentContext context) {
        this.context = context;
        this.manager = PalOOCaManager.getInstance(context);
        servDB = "";
        cube = null;
    }
    
    private class ElementInfo {
        private IElement element;
        private String path;
        private boolean isLeaf;
        private boolean isExpanded;
        
        public ElementInfo(IElement element, String path, boolean isLeaf, boolean isExpanded) {
            this.element = element;
            this.path = path;
            this.isLeaf = isLeaf;
            this.isExpanded = isExpanded;
        }
    }

    private class DataQuery implements Runnable {

        private boolean hideEmpty;
        private boolean countDistinct;
        private Map<String,ICell> result;

        public DataQuery(boolean hideEmpty, boolean countDistinct) {
            this.hideEmpty = hideEmpty;
            this.countDistinct = countDistinct;
        }

        @Override
        public void run() {
            result = queryData(hideEmpty, countDistinct);
        }

        public Map<String,ICell> getResult() {
            return result;
        }

    }
    
    private abstract class TitleWriter {
        protected boolean attributed = false;
        protected String servDB;
        protected XCellRange cellRange;
        protected int col, row, index, offsetRow, offsetCol;
        protected Vector<DimensionSubsetListItem> titleInfoList;
        protected DimensionSubsetListItem titleInfo;
        protected FunctionToken function;
        protected String[][] formulas;
        
        public TitleWriter(String servDB, XCellRange cellRange, int col, int row, 
                            Vector<DimensionSubsetListItem> titleInfoList,
                            int index, boolean attributed, int offestRow, int offSetCol, String[][] formulas) {
            this.servDB = servDB;
            this.cellRange = cellRange;
            this.col = col;
            this.row = row;
            this.titleInfoList = titleInfoList;
            this.index = index;
            this.titleInfo = titleInfoList.get(index);
            this.attributed = attributed;
            this.offsetCol = offSetCol;
            this.offsetRow = offestRow;
            this.formulas = formulas;
            
            function = new FunctionToken("org.palooca.PalOOCa.PALO_ENAME");
            function.add(new ArgumentToken(function, new OperandToken(OperandType.Range, servDB)));
            function.add(new ArgumentToken(function, new OperandToken(OperandType.Text, titleInfo.getDimension().getName())));
            function.add(new ArgumentToken(function, new OperandToken(OperandType.Text, "")));
            function.add(new ArgumentToken(function, new OperandToken(OperandType.Number, "")));
            function.add(new ArgumentToken(function, new OperandToken(OperandType.Text, "")));
            function.add(new ArgumentToken(function, new OperandToken(OperandType.Text, "")));
        }
        
        public void start() {
            ElementInfo[] info = new ElementInfo[titleInfo.getSelectedElementObjects().size()];
            IElement element;

            for (int i = 0; i < titleInfo.getSelectedElementObjects().size(); i++) {
                element = titleInfo.getSelectedElementObject(i);
                info[i] = new ElementInfo(element, titleInfo.getSelectedElementStringPath(i), element.getChildCount() == 0, false);
                if (i > 0) {
                    int depth = titleInfo.getSelectedElements().get(i-1).length;
                    int elementDepth = titleInfo.getSelectedElements().get(i).length;
                    if (depth < elementDepth)
                        info[i - 1].isExpanded = true;
                }
            }

            for (int i = 0; i < info.length; i++) {
                try {
                    processElement(info, i);
                } catch (UnknownPropertyException ex) {
                    ex.printStackTrace();
                }
            }
            titleInfo.setCurrentIndex(0);
        }
        
        protected void processElement(ElementInfo[] info, int elemIndex) throws UnknownPropertyException {
            try {
                int colIndex = col-offsetCol;
                int rowIndex = row-offsetRow;
                titleInfo.setCurrentIndex(elemIndex);
                if (rowIndex < formulas.length && colIndex < formulas[0].length) { //do not write out of sheet bounds!!
                    String type;
                    if (info[elemIndex].isLeaf) {
                        type = "0";
                    } else {
                        type = "3";
                    }

                    function.get(2).get(0).setContent(info[elemIndex].element.getName());
                    function.get(3).get(0).setContent(type);
                    function.get(4).get(0).setContent(info[elemIndex].path);
                    if (attributed && titleInfo.getAttribute() != null)
                        function.get(5).get(0).setContent(titleInfo.getAttribute().getName());
                    else
                        function.get(5).get(0).setContent("");
                    formulas[rowIndex][colIndex] = function.getFormula(true);
                }
                if (index + 1 < titleInfoList.size())
                    nextLayer();
                else
                    nextCell();
               
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        protected abstract void nextCell();
        protected abstract void nextLayer();
    }
    
    private class ColTitleWriter extends TitleWriter {
        public ColTitleWriter(String servDB, XCellRange cellRange, int col, int row, 
                                Vector<DimensionSubsetListItem>  titleInfoList,
                                int index, boolean attributed, int offsetRow, int offsetCol, String[][] formulas) {
            super(servDB, cellRange, col, row, titleInfoList, index, attributed, offsetRow, offsetCol, formulas);
        }
        
        protected void nextCell() {
            col++;
        }
        
        protected void nextLayer() {
            ColTitleWriter writer = new ColTitleWriter(servDB, cellRange, col, row + 1, titleInfoList, index + 1, attributed, offsetRow, offsetCol, formulas);
            writer.start();
            col = writer.col;
        }
    }
    
    private class RowTitleWriter extends TitleWriter {

        public RowTitleWriter(String servDB, XCellRange cellRange, int col, int row, 
                                Vector<DimensionSubsetListItem>  titleInfoList,
                                int index, boolean attributed, int offsetRow, int offsetCol, String[][] formulas) {
            super(servDB, cellRange, col, row, titleInfoList, index, attributed,offsetRow, offsetCol, formulas);
        }
        
        protected void nextCell() {
            row++;
        }
        
        protected void nextLayer() {
            RowTitleWriter writer = new RowTitleWriter(servDB, cellRange, col + 1, row, titleInfoList, index + 1, attributed,offsetRow, offsetCol, formulas);
            writer.start();
            row = writer.row;
        }
    }

     private void grayOut( XCellRange rowRange) {
        try {
            XPropertySet properties = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, rowRange);
            properties.setPropertyValue("CharColor", new Integer(0xDDDDDD));
        }
        catch (Exception e) {
             Logger.getLogger(PalOOCaView.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private int getTitleSize(Vector<DimensionSubsetListItem> dims) {
        int size = 1;
        for (int i=0; i<dims.size(); i++) {
            size = size * dims.get(i).selectedElementObjects.size();
        }
        return size;
    }
    
    private int generateColTitle(String servDB, XCellRange cellRange, int col, int row, boolean attributed) {
        System.err.println("Generate pre column titles in "+(System.currentTimeMillis()-time)+"ms");
         //init formulas
        int width = getTitleSize(columnDimensions);
        int height = columnDimensions.size();
         if (width > manager.getMaxSheetColumns()) {
            width = manager.getMaxSheetColumns();
            RunnableWarning modal = new RunnableWarning("Column range exeeds number of available columns. Result is truncated.","Worksheet restriction");
            Thread thread = new Thread(modal);
            thread.start();
        }
        String[][] formulas = new String[height][width];
        //inititalize with reference to previous column
        for (int i=0; i<height; i++) {
            for (int j=0; j<width; j++) formulas[i][j] = '=' + PalOOCaManager.getCellIdentifier(col+j-1, row+i, true, true);
        }
        grayOut(manager.getRange(col, row, col+width-1, row+height-1));
        ColTitleWriter writer = new ColTitleWriter(servDB, cellRange, col, row, columnDimensions, 0, attributed,row,col,formulas);
        writer.start();
        if (formulas.length > 0 && formulas[0].length > 0) {
            XCellRange targetRange = manager.getRange(col, row, col+width-1, row+height-1);
            XCellRangeFormula cellRangeFormula = (XCellRangeFormula) UnoRuntime.queryInterface(XCellRangeFormula.class, targetRange);
            cellRangeFormula.setFormulaArray(formulas);
            //fix cell styles for dimension element hierachy
            for (int i=0; i<columnDimensions.size(); i++) {
                List<IElement> dimElements = columnDimensions.get(i).getSelectedElementObjects();
                int step = 1;
                for (int j=i+1; j<columnDimensions.size(); j++) {
                    step = step * columnDimensions.get(j).getSelectedElementObjects().size();
                }
                int iteration = 1;
                for (int j=0; j<i; j++) {
                    iteration = iteration * columnDimensions.get(j).getSelectedElementObjects().size();
                }
                for (int k=0; k<iteration; k++) {
                    int bulkStart = col+(k*dimElements.size());
                    Boolean lastHasChildren = null;
                    for (int j=0; j<dimElements.size(); j++) {
                        IElement element = dimElements.get(j);
                        boolean hasChildren = (element.getChildCount() > 0);
                        //optimization on last column leaf elements.
                        if (step == 1) {
                            if (lastHasChildren == null) lastHasChildren = hasChildren;
                            int currentIndex = col+j+(k*dimElements.size());
                            if (currentIndex > col+width) break;
                            if (j > 0 && (!lastHasChildren.equals(hasChildren) || j==dimElements.size()-1 || currentIndex == col+width)) {//we have a change or are finished - write last bulk cells
                                 try {
                                    int bulkEnd = currentIndex-1; //write until last cell
                                    XCellRange bulkRange = manager.getRange(bulkStart, row+i,bulkEnd, row+i);
                                    XPropertySet properties = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, bulkRange);
                                    properties.setPropertyValue("CellStyle", Boolean.TRUE.equals(lastHasChildren) ? "PaloTitleParent" : "PaloTitleLeaf");
                                    if (manager.isColumnLineBreak()) {
                                        properties.setPropertyValue("IsTextWrapped", true);
                                    }
                                    bulkStart = col+j+(k*dimElements.size()); //start new range with current cell
                                 }
                                 catch (Exception e) {
                                     Logger.getLogger(PalOOCaView.class.getName()).log(Level.SEVERE, null, e);
                                 }
                            }
                        }
                        if (step > 1 || j==dimElements.size()-1) {//single cell style setting
                            try {
                                XCell cell = cellRange.getCellByPosition(col+(j*step)+(k*dimElements.size()*step), row+i);
                                XPropertySet properties = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, cell);
                                properties.setPropertyValue("CellStyle", hasChildren ? "PaloTitleParent" : "PaloTitleLeaf");
                                if (manager.isColumnLineBreak())
                                {
                                    properties.setPropertyValue("IsTextWrapped", true);
                                }
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        lastHasChildren = hasChildren;
                    }
                }
            }
            System.err.println("Generate pre column update in "+(System.currentTimeMillis()-time)+"ms");
            //updateRefs(cellRange,coords,false);
        }
        return col+width;
    }
    
    private int generateRowTitle(String servDB, XCellRange cellRange, int col, int row, boolean attributed) {
        System.err.println("Generate pre row titles in "+(System.currentTimeMillis()-time)+"ms");
         //init formulas
        int height = getTitleSize(rowDimensions);
        int width = rowDimensions.size();
        if (height > manager.getMaxSheetRows()) {
            height = manager.getMaxSheetRows();
            RunnableWarning modal = new RunnableWarning("Row range exeeds number of available rows. Result is truncated.","Worksheet restriction");
            Thread thread = new Thread(modal);
            thread.start();
        }
        String[][] formulas = new String[height][width];
        for (int i=0; i<height; i++) {
            for (int j=0; j<width; j++) formulas[i][j] = '=' + PalOOCaManager.getCellIdentifier(col+j, row+i-1, true, true);
        }
        grayOut(manager.getRange(col, row, col+width-1, row+height-1));
        RowTitleWriter writer = new RowTitleWriter(servDB, cellRange, col, row, rowDimensions, 0, attributed,row,col,formulas);
        writer.start();
        if (formulas.length > 0 && formulas[0].length > 0) {
            XCellRange targetRange = manager.getRange(col, row, col+width-1, row+height-1);
            XCellRangeFormula cellRangeFormula = (XCellRangeFormula) UnoRuntime.queryInterface(XCellRangeFormula.class, targetRange);
            cellRangeFormula.setFormulaArray(formulas);
            //fix cell styles for dimension element hierachy
            for (int i=0; i<rowDimensions.size(); i++) {
                List<IElement> dimElements = rowDimensions.get(i).getSelectedElementObjects();
                List<String[]> dimPath = rowDimensions.get(i).getSelectedElements();
                int step = 1;
                for (int j=i+1; j<rowDimensions.size(); j++) {
                    step = step * rowDimensions.get(j).getSelectedElementObjects().size();
                }
                int iteration = 1;
                for (int j=0; j<i; j++) {
                    iteration = iteration * rowDimensions.get(j).getSelectedElementObjects().size();
                }
                for (int k=0; k<iteration; k++) {
                    int lastDepth = -1;
                    int bulkStart = row+(k*dimElements.size());
                    Boolean lastHasChildren = null;
                    for (int j=0; j<dimElements.size(); j++) {
                        IElement element = dimElements.get(j);
                        int depth = dimPath.get(j).length;
                        boolean hasChildren = (element.getChildCount() > 0);
                        //optimization on last column leaf elements.
                        if (step == 1) {
                            if (lastDepth == -1) lastDepth = depth;
                            if (lastHasChildren == null) lastHasChildren = hasChildren;
                            int currentIndex = row+j+(k*dimElements.size());
                            if (currentIndex > row+height) break;
                            if (j > 0 && (depth != lastDepth || !lastHasChildren.equals(hasChildren) || j==dimElements.size()-1 || currentIndex == row+height)) {//we have a change or are finished - write last bulk cells
                                 try {
                                    int bulkEnd = currentIndex-1; //write until last cell
                                    XCellRange bulkRange = manager.getRange(col+i, bulkStart, col+i, bulkEnd);
                                    XPropertySet properties = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, bulkRange);
                                    properties.setPropertyValue("CellStyle", Boolean.TRUE.equals(lastHasChildren) ? "PaloTitleParent" : "PaloTitleLeaf");
                                    if (manager.isRowInset() && depth > 0) {
                                        properties.setPropertyValue("HoriJustify", CellHoriJustify.LEFT);
                                        properties.setPropertyValue("ParaIndent", (short)(200 * lastDepth));
                                    }
                                    bulkStart = row+j+(k*dimElements.size()); //start new range with current cell
                                 }
                                 catch (Exception e) {
                                     Logger.getLogger(PalOOCaView.class.getName()).log(Level.SEVERE, null, e);
                                 }
                            }
                        }
                        if (step > 1 || j==dimElements.size()-1) {//single cell style setting
                            try {
                                //apply single cell operation
                                XCell cell = cellRange.getCellByPosition(col+i, row+(j*step)+(k*dimElements.size()*step));
                                XPropertySet properties = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, cell);
                                properties.setPropertyValue("CellStyle", hasChildren ? "PaloTitleParent" : "PaloTitleLeaf");
                                if (manager.isRowInset() && depth > 0)
                                {
                                    properties.setPropertyValue("HoriJustify", CellHoriJustify.LEFT);
                                    properties.setPropertyValue("ParaIndent", (short)(200 * depth));
                                }
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        lastDepth = depth;
                        lastHasChildren = hasChildren;
                    }
                }
            }
            System.err.println("Generate pre row update in "+(System.currentTimeMillis()-time)+"ms");
            //updateRefs(cellRange,coords,true);
        }
        return row+height;
    }

    public void generate(XSpreadsheet sheet) {
        generate(sheet,true,true);
    }
    
    public void generate(XSpreadsheet sheet, boolean updateColumns, boolean updateRows) {
      try {
           // Lock
        XActionLockable actionLockable = (XActionLockable) UnoRuntime.queryInterface(XActionLockable.class, manager.getActiveDocument());
        actionLockable.addActionLock();
        XModel xSpreadsheetModel = (XModel)UnoRuntime.queryInterface(XModel.class, manager.getActiveDocument());
        xSpreadsheetModel.lockControllers();
        XCalculatable calc = (XCalculatable) UnoRuntime.queryInterface(XCalculatable.class,manager.getCurrentComponent());
        boolean autocalc = false;
        if (calc != null) {
            autocalc = calc.isAutomaticCalculationEnabled();
            calc.enableAutomaticCalculation(false);
        }
       
        XCellRange targetRange;
        XCellRangeFormula cellRangeFormula;
        String[][] formula;
        FunctionToken function = null;
        HashMap<IDimension, String> dimensionReference = new HashMap<IDimension, String>();
        time = System.currentTimeMillis();

        if (cube == null || servDB == null)
            return;

        for (IDimension d : cube.getDimensions()) {
            d.setCacheTrustExpiry(1000);
        }

        //we have to update all when using zero suppression
        updateRows = updateRows || manager.isHideEmptyData();
        updateColumns = updateColumns || manager.isHideEmptyData();

       
        manager.clearDimensionCache(); //Clear database dimension mapping

        int top = 2 + filterDimensions.size() + 1;
        int colSpan = Math.min(getTitleSize(columnDimensions)+rowDimensions.size()-1,manager.getMaxSheetColumns());
        int rowSpan = Math.min(getTitleSize(rowDimensions)+top+columnDimensions.size()-1,manager.getMaxSheetRows());

        // Clear Workhsheet
        if (updateColumns && updateRows) {
            XSheetOperation sheetOperation = (XSheetOperation) UnoRuntime.queryInterface(XSheetOperation.class, sheet);
            sheetOperation.clearContents(Integer.MAX_VALUE-CellFlags.OBJECTS);
        }
        else {
            //update filter
            targetRange = manager.getRange(0, 0, 3, 1 + filterDimensions.size());
            XSheetOperation sheetOperation = (XSheetOperation) UnoRuntime.queryInterface(XSheetOperation.class, targetRange);
            sheetOperation.clearContents(Integer.MAX_VALUE-CellFlags.OBJECTS);
            if (updateRows) {
                targetRange = manager.getRange(0, top+columnDimensions.size(), manager.getMaxSheetColumns()-1,manager.getMaxSheetRows()-1);
                sheetOperation = (XSheetOperation) UnoRuntime.queryInterface(XSheetOperation.class, targetRange);
                sheetOperation.clearContents(Integer.MAX_VALUE-CellFlags.OBJECTS);
            }
            if (updateColumns) {
                targetRange = manager.getRange(rowDimensions.size(), top, manager.getMaxSheetColumns()-1,manager.getMaxSheetRows()-1);
                sheetOperation = (XSheetOperation) UnoRuntime.queryInterface(XSheetOperation.class, targetRange);
                sheetOperation.clearContents(Integer.MAX_VALUE-CellFlags.OBJECTS);
            }
        }
        //updateColumns = true;
        //updateRows = true;

        com.sun.star.beans.XPropertySet xDocProp = (com.sun.star.beans.XPropertySet)
        UnoRuntime.queryInterface(com.sun.star.beans.XPropertySet.class, manager.getActiveDocument());
	Object aRangesObj;
        try {
            aRangesObj = xDocProp.getPropertyValue("NamedRanges");
            com.sun.star.sheet.XNamedRanges xNamedRanges = (com.sun.star.sheet.XNamedRanges) UnoRuntime.queryInterface(com.sun.star.sheet.XNamedRanges.class, aRangesObj);
            com.sun.star.table.CellAddress aRefPos = new com.sun.star.table.CellAddress();
            aRefPos.Sheet = 0;
            aRefPos.Column = 0;
            aRefPos.Row = 0;
            if (xNamedRanges.hasByName("_palopasteviewident") == true)
                xNamedRanges.removeByName("_palopasteviewident");
            if (xNamedRanges.hasByName("_palopastezerosuppression") == true)
                xNamedRanges.removeByName("_palopastezerosuppression");
            if (xNamedRanges.hasByName("_countDistinct") == true)
                xNamedRanges.removeByName("_countDistinct");
            xNamedRanges.addNewByName("_palopasteviewident", manager.isRowInset() ? "1" : "0", aRefPos, 0);
            xNamedRanges.addNewByName("_palopastezerosuppression", manager.isHideEmptyData() ? "1" : "0", aRefPos, 0);
            xNamedRanges.addNewByName("_countDistinct", manager.isCountDistinct() ? "1" : "0", aRefPos, 0);
        } catch (UnknownPropertyException ex) {
            Logger.getLogger(PalOOCaView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (WrappedTargetException ex) {
            Logger.getLogger(PalOOCaView.class.getName()).log(Level.SEVERE, null, ex);
        }

        prepareCellStyles();
        final String dbCell = PalOOCaManager.getCellIdentifier(1, 0, true, true);
        final String cubeCell = PalOOCaManager.getCellIdentifier(1, 1, true, true);
        // <editor-fold defaultstate="collapsed" desc="Build filter/data references">
        
        targetRange = manager.getRange(0, 0, 2, 1 + filterDimensions.size());
        cellRangeFormula = (XCellRangeFormula) UnoRuntime.queryInterface(XCellRangeFormula.class, targetRange);
        formula = cellRangeFormula.getFormulaArray();

        formula[0][0] = "Database";
        formula[0][1] = servDB;

        formula[1][0] = "Cube";
        formula[1][1] = cube.getName();

        function = new FunctionToken("org.palooca.PalOOCa.PALO_ENAME");
        function.add(new ArgumentToken(function, new OperandToken(OperandType.Range, dbCell)));
        function.add(new ArgumentToken(function, new NoopToken("")));
        function.add(new ArgumentToken(function, new NoopToken("")));
        function.add(new ArgumentToken(function, new OperandToken(OperandType.Number, "1")));
        function.add(new ArgumentToken(function, new NoopToken("")));
        function.add(new ArgumentToken(function, new OperandToken(OperandType.Text, "")));

        for (int i = 0; i < filterDimensions.size(); i++) {
            DimensionSubsetListItem info = filterDimensions.get(i);
            formula[2+i][0] = info.getDimension().getName();
            String [] filter = info.getSelectedFilter();
            if (filter != null) {
                function.get(1).set(0, new OperandToken(OperandType.Text, info.getDimension().getName()));
                function.get(2).set(0, new OperandToken(OperandType.Text, info.getSelectedFilterName()));
                function.get(4).set(0, new OperandToken(OperandType.Text, info.getSelectedFilterPath()));
                if (info.getAttribute() != null) {
                    function.get(5).set(0, new OperandToken(OperandType.Text, info.getAttribute().getName()));
                } else {
                    function.get(5).set(0, new OperandToken(OperandType.Text, ""));
                }
                dimensionReference.put(info.getDimension(), PalOOCaManager.getCellIdentifier(1, 2 + i, true, true));
                formula[2+i][1] = function.getFormula(true);
                if (info.getAttribute() != null) {
                    function.get(5).set(0, new OperandToken(OperandType.Text, ""));
                    dimensionReference.put(info.getDimension(), PalOOCaManager.getCellIdentifier(2, 2 + i, true, true));
                    formula[2+i][2] = function.getFormula(true);
                    //manager.setCellStyle(manager.getRange(2, 2 + i, 3, 3 + i), "PaloHidden");
                }
            } else {
                formula[2+i][1] = "no filter found";
            }
        }
        cellRangeFormula.setFormulaArray(formula);

        manager.setCellStyle(manager.getRange(0, 0, 1, 1), "PaloDefinition");
        manager.setCellStyle(manager.getRange(0, 2, 0, 1 + filterDimensions.size()), "PaloDefinition");
        manager.setCellStyle(manager.getRange(1, 2, 1, 1 + filterDimensions.size()), "PaloUserData");
        if (filterDimensions.size() > 0) {
            grayOut(manager.getRange(2, 2, 2, 2+filterDimensions.size()-1));
        }
       
        // </editor-fold>

        targetRange = (XCellRange) UnoRuntime.queryInterface(XCellRange.class, sheet);

        
        Map<String,ICell> cells = null;
        Thread queryThread = null;
        DataQuery dataQuery = null;
        if (!manager.isHideEmptyData() && !manager.isCountDistinct()) {
            dataQuery = new DataQuery(false,false);
            queryThread = new Thread(dataQuery);
            queryThread.start();
        }
        else {
            cells = queryData(manager.isHideEmptyData(),manager.isCountDistinct());
        }

        //System.err.println("Generate pre titles in "+(System.currentTimeMillis()-time)+"ms");
        // <editor-fold defaultstate="collapsed" desc="Build titles and data">
        targetRange = (XCellRange) UnoRuntime.queryInterface(XCellRange.class, sheet);


        if (updateColumns) {
            if (columnDimensions.size() > 0)
                colSpan = generateColTitle(dbCell, targetRange, rowDimensions.size(), top, true) - 1;
        }
        if (updateRows) {
            if (rowDimensions.size() > 0)
                rowSpan = generateRowTitle(dbCell, targetRange, 0, top + columnDimensions.size(), true) - 1;
        }
        if (anyAttributeInDimensionList(columnDimensions)) {
            if (columnDimensions.size() > 0)
                colSpan = generateColTitle(dbCell, targetRange, rowDimensions.size(), rowSpan + 1, false) - 1;
        }
        if (anyAttributeInDimensionList(rowDimensions)) {
            if (rowDimensions.size() > 0)
                rowSpan = generateRowTitle(dbCell, targetRange, colSpan + 1, top + columnDimensions.size(), false) - 1;
        }
        try {
            XCell cell = targetRange.getCellByPosition(0, 0);
            manager.setData(cell, "IsPaloView", "True");
            if (filterDimensions.size() > 0) {
                manager.setData(cell, "PaloData", PalOOCaManager.getCellIdentifier(1, 2, false, false) + ':' +
                        PalOOCaManager.getCellIdentifier(1, 1 + filterDimensions.size(), false, false));
            }
            manager.setData(cell, "PaloColTitle", PalOOCaManager.getCellIdentifier(rowDimensions.size(), top, false, false) + ':' +
                    PalOOCaManager.getCellIdentifier(colSpan, top + columnDimensions.size() - 1, false, false));
            manager.setData(cell, "PaloRowTitle", PalOOCaManager.getCellIdentifier(0, top + columnDimensions.size(), false, false) + ':' +
                    PalOOCaManager.getCellIdentifier(rowDimensions.size() - 1, rowSpan, false, false));
        } catch (com.sun.star.lang.IndexOutOfBoundsException ex) {
            ex.printStackTrace();
        }
        //System.err.println("Colspan: "+colSpan);
        //System.err.println("Rowspan: "+rowSpan);
        targetRange = manager.getRange(rowDimensions.size(), top + columnDimensions.size(), 
                                        Math.max(rowDimensions.size(), colSpan),
                                        Math.max(top + columnDimensions.size(), rowSpan));

        cellRangeFormula = (XCellRangeFormula) UnoRuntime.queryInterface(XCellRangeFormula.class, targetRange);
        formula = cellRangeFormula.getFormulaArray();
        switch (manager.getFunctionType()) {
            case 0:
                function = new FunctionToken("org.palooca.PalOOCa.PALO_DATA");
                break;
            case 1:
                function = new FunctionToken("org.palooca.PalOOCa.PALO_DATAC");
                break;
            case 2:
                function = new FunctionToken("org.palooca.PalOOCa.PALO_DATAV");
                break;
            default: function = new FunctionToken("org.palooca.PalOOCa.PALO_DATAC");
        }
        function.add(new ArgumentToken(function, new OperandToken(OperandType.Range, dbCell)));
        function.add(new ArgumentToken(function, new OperandToken(OperandType.Range, cubeCell)));
        IDimension[] dimensions = cube.getDimensions();
        ArrayList<Integer> variableDimensions = new ArrayList<Integer>();
        HashMap<Integer, Integer> rowReference = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> colReference = new HashMap<Integer, Integer>();

        //System.err.println("Generate pre loop in "+(System.currentTimeMillis()-time)+"ms");

        // get cell references for filter dimensions
        for (int i = 0; i < dimensions.length; i++) {
            String content = "";
            if (!dimensionReference.containsKey(dimensions[i])) {
                variableDimensions.add(i);
                boolean found = false;
                for (int j = 0; j < rowDimensions.size() && !found; j++) {
                    if (rowDimensions.get(j).getDimension() == dimensions[i]) {
                        found = true;
                        rowReference.put(i, j);
                    }
                }
                for (int j = 0; j < columnDimensions.size() && !found; j++) {
                    if (columnDimensions.get(j).getDimension() == dimensions[i]) {
                        found = true;
                        colReference.put(i, j);
                    }
                }
            } else {
                content = dimensionReference.get(dimensions[i]);
            }
            function.add(new ArgumentToken(function, new OperandToken(OperandType.Range, content)));
        }

        // add reference from vertical and horizontal dimensions
        for (int row = 0; row < formula.length; row++) {
            for (int col = 0; col < formula[row].length; col++) {
                for (int i = 0; i < variableDimensions.size(); i++) {
                    String content;
                    if (rowReference.containsKey(variableDimensions.get(i))) {
                        DimensionSubsetListItem dim = rowDimensions.get(rowReference.get(variableDimensions.get(i)));
                        if (dim.getAttribute() == null) {
                            content = PalOOCaManager.getCellIdentifier(rowReference.get(variableDimensions.get(i)), top + columnDimensions.size() + row, true, false);
                        } else {
                            content = PalOOCaManager.getCellIdentifier(colSpan + 1 + rowReference.get(variableDimensions.get(i)), top + columnDimensions.size() + row, true, false);
                        }
                    } else if (colReference.containsKey(variableDimensions.get(i))) {
                        DimensionSubsetListItem dim = columnDimensions.get(colReference.get(variableDimensions.get(i)));
                        if (dim.getAttribute() == null) {
                            content = PalOOCaManager.getCellIdentifier(rowDimensions.size() + col, top + colReference.get(variableDimensions.get(i)), false, true);
                        } else {
                            content = PalOOCaManager.getCellIdentifier(rowDimensions.size() + col, rowSpan + 1 + colReference.get(variableDimensions.get(i)), false, true);
                        }
                    } else
                        content = "";
                    function.get(2 + variableDimensions.get(i)).get(0).setContent(content);
                }
                formula[row][col] = function.getFormula(true);
            }
        }

        
        // Update cells while using a one batch call to fetch data values
        if (queryThread != null && dataQuery != null) {
            queryThread.join();
            cells = dataQuery.getResult();
        }
        System.err.println("Generate pre DataValueHandler in "+(System.currentTimeMillis()-time)+"ms");
        DataValueHandler.updateCellRangeBatchingValues(context, cellRangeFormula, formula, cells, servDB+";"+cube.getName());
        if (manager.isHideEmptyData()) { //restore original column / row elements
            for (DimensionSubsetListItem dim : rowDimensions) {
                dim.restoreElements();
            }
            for (DimensionSubsetListItem dim : columnDimensions) {
                dim.restoreElements();
            }
        }
        
        if (calc != null) {
            calc.enableAutomaticCalculation(autocalc);
            if (!autocalc) {
                calc.calculate();
            }
        }
        DataValueHandler.endBatchUpdate(context,servDB+";"+cube.getName());
        System.err.println("Generate post DataValueHandler in "+(System.currentTimeMillis()-time)+"ms");
        postprocessUpdateStyles(targetRange, top, rowSpan, colSpan);
        System.err.println("Generate finished in "+(System.currentTimeMillis()-time)+"ms");
        actionLockable.removeActionLock();
        xSpreadsheetModel.unlockControllers();
      }
      catch (Exception e) {
           RunnableWarning modal = new RunnableWarning("Error building worksheet view: "+e.getMessage(),"Worksheet error");
           Thread thread = new Thread(modal);
           thread.start();
           Logger.getLogger(PalOOCaView.class.getName()).log(Level.SEVERE, null, e);
      }
    }

    private List<IElement> getLeafs(IElement element) {
        List<IElement> result = new ArrayList<IElement>();
        if (element.getChildCount() == 0) {
           result.add(element);
        } else {
            for (IElement c : element.getChildren()) {
               result.addAll(getLeafs(c));
            }
        }
        return result;
    }

    private Map<String,ICell> queryData(boolean hideEmptyCells, boolean countDistinct) {
        hideEmptyCells = hideEmptyCells || countDistinct;
        Map<String,ICell> cells = new HashMap<String,ICell>();

        // collect elements for cell export
        IDimension[] dimensions = cube.getDimensions();
        IElement[][] elems = new IElement[dimensions.length][];

        for (int k = 0; k < dimensions.length;k++) {
            boolean found = false;
            IDimension dim = dimensions[k];
            for (int l = 0; l < filterDimensions.size(); l++) {
                if (filterDimensions.get(l).getDimension() == dim) {
                    IElement elem = null;
                    List<IElement> filterElements = filterDimensions.get(l).getSelectedElementObjects();
                    if (filterElements.size() > 0) {
                        elem = filterElements.get(0);
                    } else if (dim.getRootElements(false).length > 0) {
                        elem = dim.getRootElements(false)[0];
                    }
                    if (countDistinct && l == 0) { //experimental client aggregation
                       if (elem != null) {
                           List<IElement> leafs = getLeafs(elem);
                           elems[k] = leafs.toArray(new IElement[leafs.size()]);
                           found = true;
                           break;
                       }
                    } else {
                        if (elem != null) {
                            elems[k] = new IElement[1];
                            elems[k][0] = elem;
                            found = true;
                            break;
                        }
                    }
                }
            }
            if (!found) {
                for (int l = 0; l < rowDimensions.size(); l++) {
                    if (rowDimensions.get(l).getDimension() == dim) {
                        elems[k] = new IElement[rowDimensions.get(l).getSelectedElementObjects().size()];
                        elems[k] = (IElement[])rowDimensions.get(l).getSelectedElementObjects().toArray(elems[k]);
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                for (int l = 0; l < columnDimensions.size(); l++) {
                    if (columnDimensions.get(l).getDimension() == dim) {
                        elems[k] = new IElement[columnDimensions.get(l).getSelectedElementObjects().size()];
                        elems[k] = (IElement[])columnDimensions.get(l).getSelectedElementObjects().toArray(elems[k]);
                        found = true;
                        break;
                    }
                }
            }
        }

        // do cell export
        ICellExportContext exContext = new CellExportContext(ICube.CellsExportType.BOTH, 10000, true, false, manager.isHideEmptyData());
        ICellsExporter result = cube.getCellsExporter(elems, exContext);
        ICell         exCell;

        // now collect rows for display
        Map<IDimension,Set<String>> keep = new HashMap<IDimension,Set<String>>();
        Set<String> consideredDimensions = new HashSet<String>();
        for (int k = 0; k < rowDimensions.size(); k++) {
            consideredDimensions.add(rowDimensions.get(k).getDimension().getName());
        }
        for (int k = 0; k < columnDimensions.size(); k++) {
           consideredDimensions.add(columnDimensions.get(k).getDimension().getName());
        }
        while (result.hasNext()) {
            exCell = result.next();
            String[] cellPath = exCell.getPathNames();
            if (cellPath != null) {
                if (hideEmptyCells) {
                    for (int i = 0; i < dimensions.length; i++) {
                        IElement cellElem = dimensions[i].getElementByName(cellPath[i], false);
                        if (consideredDimensions.contains(dimensions[i].getName())) {
                            Set<String> elementKeep = keep.get(dimensions[i]);
                            if (elementKeep == null) {
                                elementKeep = new LinkedHashSet<String>();
                                keep.put(dimensions[i], elementKeep);
                            }
                            elementKeep.add(cellElem.getName());
                        }
                    }
                }
                cells.put(PaloLibUtil.getPathString(exCell.getPathNames()), exCell);
            }
        }

        if (countDistinct && filterDimensions.size() > 0) {
            OlapAggregator aggregator = new OlapAggregator(dimensions,cells,filterDimensions.get(0).getDimension(), filterDimensions.get(0).getSelectedFilterName());
            cells = aggregator.aggregate();
        }


        if (hideEmptyCells) { //only keep non zero elements
            for (int k = 0; k < rowDimensions.size(); k++) {
                Set<String> elementKeep = keep.get(rowDimensions.get(k).getDimension());
                rowDimensions.get(k).updateElements(elementKeep, true, false);
            }

            for (int k = 0; k < columnDimensions.size(); k++) {
                Set<String> elementKeep = keep.get(columnDimensions.get(k).getDimension());
                columnDimensions.get(k).updateElements(elementKeep, true, false);
            }
        }
        if (cells.isEmpty() && hideEmptyCells) {
            //restore original column / row elements
            for (DimensionSubsetListItem dim : rowDimensions) {
                dim.restoreElements();
            }
            for (DimensionSubsetListItem dim : columnDimensions) {
                dim.restoreElements();
            }
            return queryData(false,false);
        } //if we have no result do a run without zero suppression
        return cells;
    }
    
    private void prepareCellStyles() {
        // <editor-fold defaultstate="collapsed" desc="Prepare cell styles">
        try {
            XNameContainer cellStyles = manager.getCellStyles();
            XPropertySet cellStyle;
            if (!cellStyles.hasByName("PaloDefinition")) {
                cellStyle = manager.createCellStyle(cellStyles, "PaloDefinition");
                cellStyle.setPropertyValue("IsCellBackgroundTransparent", Boolean.FALSE);
                cellStyle.setPropertyValue("CellBackColor", new Integer(0xDCE6F1));
                cellStyle.setPropertyValue("CharColor", new Integer(0x000000));
            }
            if (!cellStyles.hasByName("PaloUserData")) {
                cellStyle = manager.createCellStyle(cellStyles, "PaloUserData");
                cellStyle.setPropertyValue("IsCellBackgroundTransparent", Boolean.FALSE);
            //   cellStyle.setPropertyValue("CellBackColor", new Integer(0xFFFFCC));
                cellStyle.setPropertyValue("CharWeight", new Double(FontWeight.BOLD));
                cellStyle.setPropertyValue("CharColor", new Integer(0x000000));
            }
            if (!cellStyles.hasByName("PaloTitleLeaf")) {
                cellStyle = manager.createCellStyle(cellStyles, "PaloTitleLeaf");
                cellStyle.setPropertyValue("IsCellBackgroundTransparent", Boolean.FALSE);
                cellStyle.setPropertyValue("CellBackColor", new Integer(0xDCE6F1));
                cellStyle.setPropertyValue("CharColor", new Integer(0x000000));
            }
            if (!cellStyles.hasByName("PaloTitleParent")) {
                cellStyle = manager.createCellStyle(cellStyles, "PaloTitleParent");
                cellStyle.setPropertyValue("IsCellBackgroundTransparent", Boolean.FALSE);
                cellStyle.setPropertyValue("CellBackColor", new Integer(0xDCE6F1));
                cellStyle.setPropertyValue("CharWeight", new Double(FontWeight.BOLD));
                cellStyle.setPropertyValue("CharColor", new Integer(0x000000));
            }
            if (!cellStyles.hasByName("PaloData")) {
                cellStyle = manager.createCellStyle(cellStyles, "PaloData");
                cellStyle.setPropertyValue("IsCellBackgroundTransparent", Boolean.FALSE);
           //     cellStyle.setPropertyValue("CellBackColor", new Integer(0xFFFFCC));
                cellStyle.setPropertyValue("NumberFormat", new Integer(manager.getNumberFormat(2)));
                cellStyle.setPropertyValue("CharColor", new Integer(0x000000));
            }
            if (!cellStyles.hasByName("PaloDataOdd")) {
                cellStyle = manager.createCellStyle(cellStyles, "PaloDataOdd");
                cellStyle.setPropertyValue("IsCellBackgroundTransparent", Boolean.FALSE);
                cellStyle.setPropertyValue("CellBackColor", new Integer(0xF7F7F2));
                cellStyle.setPropertyValue("NumberFormat", new Integer(manager.getNumberFormat(2)));
                cellStyle.setPropertyValue("CharColor", new Integer(0x000000));
            }
            if (!cellStyles.hasByName("PaloHidden")) {
                cellStyle = manager.createCellStyle(cellStyles, "PaloHidden");
                cellStyle.setPropertyValue("NumberFormat", new Integer(manager.getNumberFormat(";;;")));
            }
            if (!cellStyles.hasByName("PaloControl")) {
                cellStyle = manager.createCellStyle(cellStyles, "PaloControl");
                cellStyle.setPropertyValue("IsCellBackgroundTransparent", Boolean.FALSE);
                cellStyle.setPropertyValue("CellBackColor", new Integer(0xE0E0E0));
                cellStyle.setPropertyValue("HoriJustify", CellHoriJustify.CENTER);
                cellStyle.setPropertyValue("VertJustify", CellVertJustify.CENTER);
                Float height = new Float(0.75) * ((Float) cellStyle.getPropertyValue("CharHeight"));
                cellStyle.setPropertyValue("CharHeight", height);
                cellStyle.setPropertyValue("CharUnderline", FontUnderline.DOTTED);
                cellStyle.setPropertyValue("CharColor", new Integer(0x000000));
            }
        } catch (com.sun.star.uno.Exception ex) {
        }
    }

    private void postprocessUpdateStyles(XCellRange targetRange, int top, int rowSpan, int colSpan) {
        if (filterDimensions.size() > 0) {
            manager.setCellStyle(targetRange, "PaloData");
        }

        try {
            for (int i=1; i < rowSpan-top; i+=2) {
                XCellRange oddRange = manager.getRange(rowDimensions.size(), top+i+1, colSpan, top+i+1);
                manager.setCellStyle(oddRange, "PaloDataOdd");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }


        if (columnDimensions.size() > 0 && rowDimensions.size() > 0) {
            targetRange = manager.getRange(0, top, rowDimensions.size() - 1, top + columnDimensions.size() - 1);
            XMergeable mergeable = (XMergeable) UnoRuntime.queryInterface(XMergeable.class, targetRange);
            mergeable.merge(true);
            try {
                XCell cell = targetRange.getCellByPosition(0, 0);
                manager.setData(cell, "IsPaloView", "True");
                manager.setCellStyle(cell, "PaloControl");
                cell.setFormula("Edit");
            } catch (com.sun.star.lang.IndexOutOfBoundsException ex) {
                ex.printStackTrace();
            }
        }
        // </editor-fold>
        //Adjust columns

        try {
            XPropertySet properties;

            if (true) { //always reset visibility since we never use if attrubutes has been in use before.
                targetRange = manager.getRange(0, 0, colSpan+rowDimensions.size(), rowSpan+columnDimensions.size());
                XColumnRowRange colRowRange = (XColumnRowRange) UnoRuntime.queryInterface(XColumnRowRange.class, targetRange);
                XTableRows rows = colRowRange.getRows();

                for (int i = 0; i < rows.getCount(); i++) {
                    properties = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, rows.getByIndex(i));
                    properties.setPropertyValue("IsVisible", true);
                }
                XTableColumns columns = colRowRange.getColumns();

                for (int i = 0; i < columns.getCount(); i++) {
                    properties = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, columns.getByIndex(i));
                    properties.setPropertyValue("IsVisible", true);
                }
           }

            if (manager.isHideIdsOnAttributeUse()) {
                if (anyAttributeInDimensionList(columnDimensions)) {
                    targetRange = manager.getRange(0, rowSpan + 1, colSpan, rowSpan + 1 + columnDimensions.size());
                    XColumnRowRange colRowRange = (XColumnRowRange) UnoRuntime.queryInterface(XColumnRowRange.class, targetRange);
                    XTableRows rows = colRowRange.getRows();

                     for (int i = 0; i < columnDimensions.size(); i++) {
                        properties = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, rows.getByIndex(i));
                        //properties.setPropertyValue("CharColor", new Integer(0xDDDDDD));
                        properties.setPropertyValue("IsVisible", false);
                    }
                }

                if (anyAttributeInDimensionList(rowDimensions)) {
                    targetRange = manager.getRange(colSpan + 1, 0, colSpan + 1 + rowDimensions.size(), rowSpan);
                    XColumnRowRange colRowRange = (XColumnRowRange) UnoRuntime.queryInterface(XColumnRowRange.class, targetRange);
                    XTableColumns cols = colRowRange.getColumns();

                    for (int i = 0; i < rowDimensions.size(); i++) {
                        properties = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, cols.getByIndex(i));
                        //properties.setPropertyValue("CharColor", new Integer(0xDDDDDD));
                        properties.setPropertyValue("IsVisible", false);
                    }
                }
            }

            targetRange = manager.getRange(0, 0, colSpan, rowSpan);
            XColumnRowRange colRowRange = (XColumnRowRange) UnoRuntime.queryInterface(XColumnRowRange.class, targetRange);
            XTableColumns columns = colRowRange.getColumns();

            for (int i = 0; i < columns.getCount(); i++) {
                properties = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, columns.getByIndex(i));
                if (manager.isColumnCustomSize() && i >= rowDimensions.size()) {
                    properties.setPropertyValue("Width", (int)(manager.getColumnWidth() * 1000.0));
                    //properties.setPropertyValue("CharColor", new Integer(0x000000));
                    properties.setPropertyValue("IsVisible", true);
                } else {
                    properties.setPropertyValue("OptimalWidth", Boolean.TRUE);
                    //properties.setPropertyValue("CharColor", new Integer(0x000000));
                    properties.setPropertyValue("IsVisible", true);
                }
            }

            

        } catch (com.sun.star.uno.Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean anyAttributeInDimensionList(Vector<DimensionSubsetListItem> items) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getAttribute() != null)
                return true;
        }

        return false;
    }
    
    public void handleDoubleClick(XSpreadsheet spreadsheet, String dbName, String dimensionName,
                                  String elementName, String elementPath) {

        int elementIndex = -1;
        boolean unchecked = true;
        boolean expanded = false;
        boolean changed = false;
        boolean changedColumns = false;
        boolean changedRows = false;
        DimensionSubsetListItem dimItem = null;

        long time = System.currentTimeMillis();
        System.err.println("Handle double click start.");

        //First we check if it is a column element
        for (int i = 0; i < columnDimensions.size() && changed == false; i++) {
            dimItem = columnDimensions.get(i);
            if (dimItem.getDimension().getName().equalsIgnoreCase(dimensionName)) {
                for (int j = 0; j < dimItem.getSelectedElements().size(); j++) {

                    String   stringPath = dimItem.getSelectedElementStringPath(j);

                    if (elementPath.equals(stringPath)) {
                        elementIndex = j;
                        if (unchecked) {
                            expanded = isElementExpanded(dimItem, elementPath, elementIndex);
                            unchecked = false;
                        }
                        if (expanded) {
                            collapseElement(dimItem, elementPath, elementIndex);
                        } else {
                            expandElement(dimItem, elementPath, elementIndex);
                        }
                        changedColumns = changed = true;
                    }
                }
            }
        }
        
        //... if it isn't we check the row elements
        for (int i = 0; i < rowDimensions.size() && changed == false; i++) {
            dimItem = rowDimensions.get(i);
            if (dimItem.getDimension().getName().equalsIgnoreCase(dimensionName)) {
                for (int j = 0; j < dimItem.getSelectedElements().size(); j++) {
                    String   stringPath = dimItem.getSelectedElementStringPath(j);

                    if (elementPath.equals(stringPath)) {
                        elementIndex = j;
                        if (unchecked) {
                            expanded = isElementExpanded(dimItem, elementPath, elementIndex);
                            unchecked = false;
                        }
                        if (expanded) {
                            elementIndex = collapseElement(dimItem, elementPath, elementIndex);
                        } else {
                            elementIndex = expandElement(dimItem, elementPath, elementIndex);
                        }
                        changedRows = changed = true;
                    }
                }
            }
        }
        
        if (dimItem == null || !dimItem.getDimension().getName().equals(dimensionName) ||
                               !dimItem.getSelectedElementObject(elementIndex).getName().equals(elementName))
            return; //Invalid parameters or invalid view
        
        //We modified something, so we have to rebuild the view
        System.err.println("Handle double click end. Starting generation. "+(System.currentTimeMillis()-time)+"ms");
        if (changed)
            generate(spreadsheet,changedColumns,changedRows);
    }

    private boolean isElementExpanded(DimensionSubsetListItem dimItem, String elementPath, int elementIndex) {
        String path = elementPath + '\\';

        for (int i = 0; i < dimItem.getSelectedElements().size(); i++) {
            String stringPath = dimItem.getSelectedElementStringPath(i);
            if (i != elementIndex && stringPath.indexOf(path) == 0) {
                return true;
            }
        }

        return false;
    }

    private int collapseElement(DimensionSubsetListItem dimItem, String elementPath, int elementIndex)
    {
        String path = elementPath + '\\';

        //Search children and remove them (= collapsing)
        for (int i = 0; i < dimItem.getSelectedElements().size(); ) {
            String stringPath = dimItem.getSelectedElementStringPath(i);
            if (i != elementIndex && stringPath.indexOf(path) == 0) {
                dimItem.getSelectedElementObjects().remove(i);
                dimItem.getSelectedElements().remove(i);
                if (i < elementIndex)
                    elementIndex--;
            } else {
                i++;
            }
        }
        return elementIndex;
    }

    private int expandElement(DimensionSubsetListItem dimItem, String elementPath, int elementIndex)
    {
        String path = elementPath + '\\';
                                                            //If we didn't find any, we are maybe expanding
        IElement element = dimItem.getSelectedElementObject(elementIndex);

        if (element.getChildCount() > 0) {
            IElement[] children = element.getChildren();
            for (int i = 0; i < children.length; i++) {
                String[] stringPath = PaloDialogUtilities.stringPathToStringArray(path + children[i].getName());
                if (stringPath != null && stringPath.length > 0) {
                    dimItem.getSelectedElementObjects().add(elementIndex + 1 + i, children[i]);
                    dimItem.getSelectedElements().add(elementIndex + 1 + i, stringPath);
                }
            }
        }
        
        return elementIndex;
    }

    public void showCube(ConnectionInfo connInfo, IDatabase db, ICube cube) {
        try {
            XSpreadsheetDocument xNewDocument = manager.createNewSheetComponent();
            if (xNewDocument == null)
                return;

            // Get access to the sheets and synchronize them

            manager.setActiveDocument(xNewDocument);

            setServDB(connInfo.getName() + "/" + db.getName());
            setCube(cube);

            IDimension[] dims = cube.getDimensions();

            manager.setColumnLineBreak(true);
            manager.setColumnCustomSize(true);
            manager.setRowInset(true);
            manager.setHideEmptyData(false);
            manager.setShowElementOnDblClk(false);
            manager.setFunctionType(0);

            DimensionSubsetListItem     dimItem = new DimensionSubsetListItem(connInfo,db,dims[0], true, false);

            columnDimensions.add(dimItem);

            dimItem = new DimensionSubsetListItem(connInfo,db,dims[1], true, false);

            rowDimensions.add(dimItem);

            //Apply view to the active spreadsheet
            generate(manager.getActiveSpreadSheet());
        } catch (PaloException pe) {
            JOptionPane.showMessageDialog(null, pe.getDescription() + ", " + pe.getReason(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (PaloJException pe) {
            JOptionPane.showMessageDialog(null, pe.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


     static public String[] stringPathToStringArray(String path) {
        String[] result = null;

        String rep = path.replace('\\', ':');
        String[] tmpResult = rep.split(new String(":"));

        result = new String[tmpResult.length + 1];

        result[0] = null;
        for (int i = 0; i < tmpResult.length; i++) {
            result[i + 1] = tmpResult[i];
        }

        return result;
    }

    public static PalOOCaView throwViewParseError(String message, String type, boolean showErrors) {
         if (showErrors) {
            RunnableWarning modalWarning = new RunnableWarning(message,type);
            Thread thread = new Thread(modalWarning);
            thread.start();
         }
         return null;
    }

    public static PalOOCaView parseSpreadsheet(XComponentContext context, XSpreadsheet sheet, boolean showErrors) {
        PalOOCaView view = new PalOOCaView(context);
        PalOOCaManager manager = PalOOCaManager.getInstance(context);
        ConnectionHandler connectionHandler = manager.getConnectionHandler();
        String errorType = connectionHandler.getResourceBundle().getString("Connection_Failed_Caption");
        long time = System.currentTimeMillis();

        String val = getNamedRangeValue(manager, "_palopasteviewident");
        if (val.equalsIgnoreCase("1"))
            manager.setRowInset(true);
        else
            manager.setRowInset(false);

        val = getNamedRangeValue(manager, "_palopastezerosuppression");
        if (val.equalsIgnoreCase("1"))
            manager.setHideEmptyData(true);
        else
            manager.setHideEmptyData(false);
        val = getNamedRangeValue(manager, "_countDistinct");
        if (val.equalsIgnoreCase("1"))
            manager.setCountDistinct(true);
        else
            manager.setCountDistinct(false);

        try {
            XCell cell = sheet.getCellByPosition(0, 0);
            if (!manager.getData(cell, "IsPaloView", "False").equals("True"))
                return null;

            XCellRange range = sheet.getCellRangeByPosition(1, 0, 1, 1);
            XCellRangeFormula rangeFormula = (XCellRangeFormula) UnoRuntime.queryInterface(XCellRangeFormula.class, range);
            String[][] formulaData = rangeFormula.getFormulaArray();

            IDatabase database = connectionHandler.getDatabase(formulaData[0][0]);
            if (database == null) {
                return throwViewParseError("Could not connect to "+formulaData[0][0], errorType, showErrors);
            }
            view.setServDB(formulaData[0][0]);

            ICube cube = database.getCubeByName(formulaData[1][0]);
            if (cube == null) {
                return throwViewParseError("Could not connect to cube "+formulaData[1][0], errorType, showErrors);
            }
            view.setCube(cube);

            Token formula;
            FunctionToken function;
            IDimension dimension = null;
            IElement element;
            String path = null;
            Map<String,IDimension> dimensionLookUp = new HashMap<String,IDimension>();

            //build dimension lookup
            for (IDimension dim : cube.getDimensions()) {
                dimensionLookUp.put(dim.getName(), dim);
                dim.setCacheTrustExpiry(10000);
            }

            // <editor-fold defaultstate="collapsed" desc="Analyse data section">
            String name = manager.getData(cell, "PaloData", "");
            if (name.length() > 0) {
                range = sheet.getCellRangeByName(name);
                if (range != null) {
                    rangeFormula = (XCellRangeFormula) UnoRuntime.queryInterface(XCellRangeFormula.class, range);
                    formulaData = rangeFormula.getFormulaArray();

                    Vector<DimensionSubsetListItem> filterDimensions = view.getFilterDimensions();
                    for (int i = 0; i < formulaData.length; i++) {
                        element = null;
                        formula = FormulaParser.parseFormula(formulaData[i][0]);
                        if (formula != null && formula.size() > 0) {
                            function = (FunctionToken) formula.get(0);
                            if (function != null && function.getContent().equals("org.palooca.PalOOCa.PALO_ENAME") &&
                                    function.size() > 3) {
                                String type = function.get(3).get(0).getContent();
                                if (type.equals("1")) {
                                    dimension = dimensionLookUp.get(function.get(1).get(0).getContent());
                                    if (dimension != null) {
                                        String[] stringArray;
                                        element = dimension.getElementByName(function.get(2).get(0).getContent(),false);
                                        DimensionSubsetListItem dimItem = new DimensionSubsetListItem(connectionHandler.getLastConnectionInfo(),database,dimension, false, false);
                                        if (element != null) {
                                            if (function.size() > 4) {
                                                path = function.get(4).get(0).getContent();
                                                stringArray = stringPathToStringArray(path);
                                            } else {                // in case path is not available (old version)
                                                IElement[] parents = null;
                                                int level = 0;
                                                IElement parent = element;
                                                Vector<String>  parentStrings = new Vector<String>();
                                                do {
                                                     parents = parent.getParents();
                                                     if (parents != null && parents.length != 0) {
                                                         parent = parents[0];
                                                         level++;
                                                         parentStrings.add(parent.getName());
                                                     }
                                                } while (parents != null && parents.length != 0);

                                                stringArray = new String[level + 2];
                                                stringArray[0] = null;
                                                for (int j = 0; j < level; j++) {
                                                    stringArray[j + 1] = parentStrings.get(j);
                                                }
                                                stringArray[level + 1] = element.getName();
                                            }
                                            dimItem.setSelectedFilterPath(stringArray);
                                            dimItem.setSelectedFilterElementObject(element);
                                            if (function.size() > 5) {
                                                String attrName = function.get(5).get(0).getContent();
                                                IAttribute attr = dimension.getAttributeByName(attrName);
                                                dimItem.setAttribute(attr);
                                            }
                                        } else if (dimension.getRootElements(false).length > 0) { //filter element not found any more. perhaps renamed or removed
                                            element = dimension.getRootElements(false)[0];
                                            dimItem.setSelectedFilterElementObject(element);
                                            dimItem.setSelectedFilterPath(new String[]{element.getName()});
                                        }
                                        filterDimensions.add(dimItem);
                                    }
                                }
                            }
                        }

                        if (dimension == null) {
                            return throwViewParseError("Unknown dimension in use.", errorType, showErrors);
                        }
                        if (element == null)
                            continue;
                    }
                }
            }
            // </editor-fold>

            //System.err.println("parse filter dimension finished: "+(System.currentTimeMillis()-time));

            // <editor-fold defaultstate="collapsed" desc="Analyse col titles">
            range = sheet.getCellRangeByName(manager.getData(cell, "PaloColTitle", ""));
            if (range == null)
                return null;
            rangeFormula = (XCellRangeFormula) UnoRuntime.queryInterface(XCellRangeFormula.class, range);
            formulaData = rangeFormula.getFormulaArray();
            if (formulaData.length == 0 || formulaData[0].length == 0)
                return null;

            int maxLength = Integer.MAX_VALUE;
            Vector<DimensionSubsetListItem> columnDimensions = view.getColumnDimensions();
            DimensionSubsetListItem dimItem = null;

            for (int i = 0; i < formulaData.length; i++) {
                boolean found = false;
                IDimension lastDimension = null;
                dimItem = null;
                int length = Math.min(maxLength, formulaData[i].length);

                for (int j = 0; j < length; j++) {
                    formula = FormulaParser.parseFormula(formulaData[i][j]);
                    if (formula != null && formula.size() > 0) {
                        function = (FunctionToken) formula.get(0);
                        if (function != null && function.getContent().equals("org.palooca.PalOOCa.PALO_ENAME") &&
                                function.size() > 4) {
                            String type = function.get(3).get(0).getContent();
                            if (type.equals("0") || type.equals("3") || type.equals("2")) {
                                found = true;
                                dimension = dimensionLookUp.get(function.get(1).get(0).getContent());
                                if (lastDimension == null) {
                                    if (dimItem == null)
                                        dimItem = new DimensionSubsetListItem(connectionHandler.getLastConnectionInfo(),database,dimension, false, false);
                                    lastDimension = dimension;
                                }

                                if (dimension == null || dimension != lastDimension) {
                                     return throwViewParseError("Unknown dimension in use.", errorType, showErrors);
                                }

                                element = dimension.getElementByName(function.get(2).get(0).getContent(),false);
                                if (element == null)
                                    continue;

                                dimItem.selectedElementObjects.add(element);
                                String elementPath = function.get(4).get(0).getContent();
                                if (element == null)
                                    return throwViewParseError("Element not found.", errorType, showErrors);
                                String[] stringPath = stringPathToStringArray(elementPath);
                                if (stringPath == null)
                                    return throwViewParseError("Illegal path.", errorType, showErrors);
                                dimItem.selectedElements.add(stringPath);

                                if (j > 0 && j < maxLength)
                                    maxLength = j;
                            }
                            if (function.size() > 5) {
                                String attribute = function.get(5).get(0).getContent();
                                if (attribute != null && attribute.length() > 0) {
                                    if (dimension != null) {
                                        IAttribute attr = dimension.getAttributeByName(attribute);
                                        if (dimItem != null) {
                                            dimItem.setAttribute(attr);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (j == 0 && !found)
                        return throwViewParseError("View parse error.", errorType, showErrors);
                }

                if (!found)
                    return throwViewParseError("View parse error.", errorType, showErrors);

                columnDimensions.add(dimItem);
            }
            // </editor-fold>

            //System.err.println("parse column dimension finished: "+(System.currentTimeMillis()-time));

            // <editor-fold defaultstate="collapsed" desc="Analyse row titles">
            range = sheet.getCellRangeByName(manager.getData(cell, "PaloRowTitle", ""));
            if (range == null)
                return null;
            rangeFormula = (XCellRangeFormula) UnoRuntime.queryInterface(XCellRangeFormula.class, range);
            formulaData = rangeFormula.getFormulaArray();
            if (formulaData.length == 0 || formulaData[0].length == 0)
                return null;

            maxLength = Integer.MAX_VALUE;
            Vector<DimensionSubsetListItem> rowDimensions = view.getRowDimensions();
            dimItem = null;

            for (int j = 0; j < formulaData[0].length; j++) {
                boolean found = false;
                IDimension lastDimension = null;
                dimItem = null;
                int length = Math.min(maxLength, formulaData.length);

                for (int i = 0; i < length; i++) {
                    formula = FormulaParser.parseFormula(formulaData[i][j]);
                    if (formula != null && formula.size() > 0) {
                        function = (FunctionToken) formula.get(0);
                        if (function != null && function.getContent().equals("org.palooca.PalOOCa.PALO_ENAME") &&
                                function.size() > 4) {
                            String type = function.get(3).get(0).getContent();
                            if (type.equals("0") || type.equals("3") || type.equals("2")) {
                                found = true;
                                dimension = dimensionLookUp.get(function.get(1).get(0).getContent());
                                if (lastDimension == null) {
                                    if (dimItem == null)
                                        dimItem = new DimensionSubsetListItem(connectionHandler.getLastConnectionInfo(),database,dimension, false, false);
                                    lastDimension = dimension;
                                }

                                if (dimension == null || dimension != lastDimension)
                                    return null;

                                element = dimension.getElementByName(function.get(2).get(0).getContent(),false);
                                if (element == null)
                                    continue;

                                dimItem.selectedElementObjects.add(element);
                                String elementPath = function.get(4).get(0).getContent();
                                if (element == null)
                                    return null;
                                String[] stringPath = stringPathToStringArray(elementPath);
                                if (stringPath == null)
                                    return null;
                                dimItem.selectedElements.add(stringPath);

                                if (i > 0 && i < maxLength)
                                    maxLength = i;
                            }
                            if (function.size() > 5) {
                                String attribute = function.get(5).get(0).getContent();
                                if (attribute != null && attribute.length() > 0) {
                                    if (dimension != null) {
                                        IAttribute attr = dimension.getAttributeByName(attribute);
                                        if (dimItem != null) {
                                            dimItem.setAttribute(attr);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (i == 0 && !found)
                        return null;
                }

                if (!found)
                    return null;

                rowDimensions.add(dimItem);
            }

            System.err.println("parse row dimension finished: "+(System.currentTimeMillis()-time));

            range = sheet.getCellRangeByPosition(view.getRowDimensions().size(),
                                                 view.getColumnDimensions().size() + view.getFilterDimensions().size() + 3, 
                                                 view.getRowDimensions().size() + 1,
                                                 view.getColumnDimensions().size() + view.getFilterDimensions().size() + 4);
            if (range != null) {
                rangeFormula = (XCellRangeFormula) UnoRuntime.queryInterface(XCellRangeFormula.class, range);
                if (rangeFormula != null) {
                    formulaData = rangeFormula.getFormulaArray();
                    formula = FormulaParser.parseFormula(formulaData[0][0]);
                    if (formula != null && formula.size() > 0) {
                        function = (FunctionToken) formula.get(0);
                        if (function != null) {
                            if (function.getContent().equals("org.palooca.PalOOCa.PALO_DATA")) {
                                manager.setFunctionType(0);
                            } else if (function.getContent().equals("org.palooca.PalOOCa.PALO_DATAC")) {
                                manager.setFunctionType(1);
                            } else if (function.getContent().equals("org.palooca.PalOOCa.PALO_DATAV")) {
                                manager.setFunctionType(2);
                            }
                        }
                    }
                }
            }

            //System.err.println("parse formulas finished: "+(System.currentTimeMillis()-time));


            // </editor-fold>
        } catch (Exception ex) {
            ex.printStackTrace();
            return throwViewParseError("View parse error.: "+ex.getMessage(), errorType, showErrors);
        }

        return view;
    }

    private static String getNamedRangeValue(PalOOCaManager manager, String name)
    {
        com.sun.star.beans.XPropertySet xDocProp = (com.sun.star.beans.XPropertySet)
                        UnoRuntime.queryInterface(com.sun.star.beans.XPropertySet.class, manager.getActiveDocument());
	Object aRangesObj;
        try {
            aRangesObj = xDocProp.getPropertyValue("NamedRanges");
            com.sun.star.sheet.XNamedRanges xNamedRanges = (com.sun.star.sheet.XNamedRanges)UnoRuntime.queryInterface(com.sun.star.sheet.XNamedRanges.class, aRangesObj);
            com.sun.star.table.CellAddress aRefPos = new com.sun.star.table.CellAddress();
            aRefPos.Sheet = 0;
            aRefPos.Column = 0;
            aRefPos.Row = 0;
            if (xNamedRanges.hasByName(name) == true) {
                try {
                    XNamedRange namedRange = (XNamedRange) UnoRuntime.queryInterface(XNamedRange.class,
                                                                    xNamedRanges.getByName(name));
                    String val = namedRange.getContent();
                    if (val instanceof String)
                        return val;
                } catch (NoSuchElementException ex) {
                    Logger.getLogger(PalOOCaView.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (UnknownPropertyException ex) {
            Logger.getLogger(PalOOCaView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (WrappedTargetException ex) {
            Logger.getLogger(PalOOCaView.class.getName()).log(Level.SEVERE, null, ex);
        }

        return "";
    }
}
