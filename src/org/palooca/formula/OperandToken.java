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

package org.palooca.formula;

import com.sun.star.sheet.XCellRangeData;
import com.sun.star.sheet.XFunctionAccess;
import com.sun.star.sheet.XSpreadsheet;
import com.sun.star.sheet.XSpreadsheets;
import com.sun.star.table.XCellRange;
import com.sun.star.uno.UnoRuntime;

/**
 *
 * @author Andreas Schneider
 */
public class OperandToken extends Token {
    
    protected OperandType type;
    protected Float floatValue;

    public OperandToken(OperandType type, String content) {
        super(content);
        this.type = type;
    }
    
    public static final String[] BOOLEAN = new String[] {"TRUE", "FALSE"};
    
    public OperandToken(String content) {
        this(OperandType.Unknown, content);
        
        floatValue = null;
        try {
            floatValue = Float.parseFloat(content);
            if (Float.isNaN(floatValue))
                floatValue = null;
        } catch (NumberFormatException e) {
        }
        
        if (floatValue == null) {
            if (FormulaParser.arrayIndexOf(BOOLEAN, content.toUpperCase()) > -1) {
                this.type = OperandType.Logical;
            } else {
                this.type = OperandType.Range;
            }
        } else {
            this.type = OperandType.Number;
        }
    }

    public OperandType getType() {
        return type;
    }

    public Float getFloatValue() {
        return floatValue;
    }
    
    @Override
    public String getFormula(boolean first) {
        if (first && type != OperandType.Number && type != OperandType.Text)
            return "=" + content;
        else if (!first && type == OperandType.Text)
            return '"' + content.replaceAll("\\\"", "\\\"\\\"") + '"';
        else
            return content;
    }
    
    @Override
    public Object calculate(XFunctionAccess functionAccess, XSpreadsheets spreadsheets, XSpreadsheet activeSheet) {
        switch (type) {
            case Number:
                return Double.parseDouble(content);
            case Range:
                XSpreadsheet sheet;
                String cellInfo;
                int pos = content.lastIndexOf(".");
                if (pos > -1) {
                    String sheetInfo = content.substring(0, pos).replaceAll("\\$", "");
                    cellInfo = content.substring(pos + 1);
                    try {
                        sheet = (XSpreadsheet) UnoRuntime.queryInterface(XSpreadsheet.class, spreadsheets.getByName(sheetInfo));
                    } catch (Exception e) {
                        sheet = null;
                    }
                } else {
                    sheet = activeSheet;
                    cellInfo = content;
                }
                cellInfo = cellInfo.replaceAll("\\$", "");
                
                if (cellInfo.indexOf(":") < 0) {
                    cellInfo = cellInfo + ':' + cellInfo;
                }
                
                XCellRange range = sheet.getCellRangeByName(cellInfo);
                XCellRangeData cellData = (XCellRangeData) UnoRuntime.queryInterface(XCellRangeData.class, range);
                if (cellData != null) {
                    return cellData.getDataArray()[0][0];
                }
                
                return activeSheet.getCellRangeByName(content);
            case Text:
                return content;
        } 
        return null;
    }

}
