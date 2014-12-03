/*
 * Palo Open Office Calc AddIn
 * Copyright (C) 2010 PalOOCa Team,  Tensegrity Software GmbH, 2010

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
 */

package org.palooca;

import com.sun.star.sheet.XCellRangeFormula;
import com.sun.star.uno.XComponentContext;
import com.jedox.palojlib.interfaces.ICell;
import com.jedox.palojlib.interfaces.ICell.CellType;
import com.jedox.palojlib.interfaces.ICube;
import com.jedox.palojlib.interfaces.IDatabase;
import com.jedox.palojlib.interfaces.IDimension;
import com.jedox.palojlib.interfaces.IElement;
import com.jedox.palojlib.main.Cell;
import java.util.HashMap;
import java.util.Map;
import org.palooca.network.ConnectionHandler;

/**
 *
 * @author pvanderm
 */
public class DataValueHandler {
    private static Map<String,Map<String,ICell>> cellLookup = new HashMap<String,Map<String,ICell>>();
    private static boolean readFromBatchResults = false;

    // To disable batching, set this to true
    private static boolean disableBatchFetch = false;
    private static boolean fixMissingCells = false;

    protected static void beginReadFromBatchResults(){
        readFromBatchResults = true;
    }

    protected static void endReadFromBatchResults(){
        readFromBatchResults = false;
    }

    private static boolean getReadFromBatchResults(){
        return readFromBatchResults;
    }

    public static void updateCellRangeBatchingValues(XComponentContext context,XCellRangeFormula cellRangeFormula, String[][] formula, Map<String,ICell> lookup, String cubeName){
        if (disableBatchFetch)
            cellRangeFormula.setFormulaArray(formula);
        else{
            long time = System.currentTimeMillis();
            PalOOCaManager manager = PalOOCaManager.getInstance(context);
            fixMissingCells = manager.isHideEmptyData();
            cellLookup.put(cubeName, lookup);
            // Now do a second pass to read results from the batch
            DataValueHandler.beginReadFromBatchResults();
            cellRangeFormula.setFormulaArray(formula);
            /* done in view now
            manager.recalculateDocument(true);
            DataValueHandler.endReadFromBatchResults();
            cellLookup.remove(cubeName);
             * */
        }
    }

    public static void endBatchUpdate(XComponentContext context,String cubeName) {
         DataValueHandler.endReadFromBatchResults();
         cellLookup.remove(cubeName);
    }

    public static String getServerCubeCombo(String servdb, String cubeName) {
       return servdb + ";" + cubeName;
    }

    public static Object getPaloDataValue(XComponentContext context, String servdb, String cubeName, Object[] coordinates){
        ICell cell = null;
        try {

            // Read results from batch if required
            if (DataValueHandler.getReadFromBatchResults()){
                Map<String,ICell> preCalcLookup = cellLookup.get(getServerCubeCombo(servdb,cubeName));
                if (preCalcLookup != null) {
                    cell = preCalcLookup.get(PaloLibUtil.getPathString(coordinates));
                    if (cell == null && fixMissingCells) {
                        cell = new Cell(new int[]{},0,CellType.CELL_NUMERIC,new IDimension[]{},new String[]{});
                    }
                }
            }
            // Otherwise calculate the value
            else {
                PalOOCaManager manager = PalOOCaManager.getInstance(context);
                ConnectionHandler connectionHandler = manager.getConnectionHandler();

                IDatabase database = connectionHandler.getDatabase(servdb);
                if ( database == null )
                    return null;

                ICube cube = database.getCubeByName(cubeName);
                if ( cube == null )
                    return null;

                IElement[] elementCoords = new IElement[coordinates.length];
                IDimension[] dimensions = cube.getDimensions();

                for (int i = 0; i < coordinates.length; i++) {
                    elementCoords[i] = dimensions[i].getElementByName(coordinates[i].toString(),false);
                    if (elementCoords[i] == null)
                        return null;
                }

                cell = cube.getCell(elementCoords);
            }

            if (cell == null)
                return null;

            if (cell.getType() == ICell.CellType.CELL_STRING) {
                if (cell.getValue() == null) {
                    return "";
                } else {
                    return cell.getValue();
                }
            } else {
                if (cell.getValue() == null) {
                    return new Double(0);
                } else {
                    return cell.getValue();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
