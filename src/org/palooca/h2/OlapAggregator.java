/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.palooca.h2;

import com.jedox.palojlib.interfaces.ICell;
import com.jedox.palojlib.interfaces.ICell.CellType;
import com.jedox.palojlib.interfaces.IDimension;
import com.jedox.palojlib.main.Cell;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.palooca.PaloLibUtil;

/**
 *
 * @author chris
 */
public class OlapAggregator {

    private Map<String,ICell> cells;
    private IDimension[] dimensions;
    private Connection connection;
    private String tableName;
    private IDimension aggregateDimension;
    private String aggregateElementName;
    private String quote = "\"";

    public OlapAggregator(IDimension[] dimensions, Map<String,ICell> cells, IDimension aggregateDimension, String aggregateElementName) {
        this.cells = cells;
        this.dimensions = dimensions;
        this.aggregateDimension = aggregateDimension;
        this.aggregateElementName = aggregateElementName;
        try {
            Class.forName("org.h2.Driver");
            StringBuffer columns = new StringBuffer();
            for (IDimension d : dimensions) {
                columns.append(","+quote(d.getName())+" varchar");
            }
            columns.deleteCharAt(0);
            tableName = "agg"+System.currentTimeMillis();
            OlapTableEngine.addData(tableName.toUpperCase(), cells.values());
            connection = DriverManager.getConnection("jdbc:h2:mem:", "sa", "");
            connection.createStatement().execute("CREATE TABLE "+tableName+"("+columns.toString()+") ENGINE "+quote(OlapTableEngine.class.getName()));
        } catch (ClassNotFoundException cnfe) {
                throw new RuntimeException("H2 Driver not found: "+cnfe.getMessage());
        } catch (SQLException sqle) {
                sqle.printStackTrace();
                throw new RuntimeException("Failed to open internal h2 connection "+sqle.getMessage());
        }
    }

    public Map<String,ICell> aggregate() {
         Map<String,ICell> result = new HashMap<String,ICell>();
         try {
            int aggDimPos = 0;
            StringBuffer selects = new StringBuffer();
            for (int i=0; i<dimensions.length; i++) {
                IDimension d = dimensions[i];
                if (!d.getName().equals(aggregateDimension.getName())) {
                    selects.append(","+quote(d.getName()));
                } else {
                    aggDimPos = i;
                }
            }
            String groupBy = selects.substring(1);
            selects.insert(0,"count( distinct "+quote(aggregateDimension.getName())+")");
            String query = buildQuery(tableName,selects.toString(), "", groupBy,"");
            ResultSet rs = connection.createStatement().executeQuery(query);
            while (rs.next()) {
                double value = rs.getDouble(1);
                String[] coords = new String[dimensions.length];
                for (int i = 2; i <= dimensions.length; i++) {
                    int cpos = i-2;
                    if (cpos < aggDimPos) {
                        coords[cpos] = rs.getString(i);
                    }
                    else if (cpos == aggDimPos) {
                        coords[aggDimPos] = aggregateElementName;
                        coords[cpos+1] = rs.getString(i);
                    } else {
                        coords[cpos+1] = rs.getString(i);
                    }
                }
                //fix for aggDim = LastDim
                if (coords[dimensions.length-1] == null) coords[dimensions.length-1] = aggregateElementName;
                String key = PaloLibUtil.getPathString(coords);
                result.put(key, new Cell(new int[]{},value,CellType.CELL_NUMERIC,new IDimension[]{}, new String[]{}));
            }
         } catch (SQLException sqle) {
            sqle.printStackTrace();
            throw new RuntimeException("Failed to open internal h2 connection "+sqle.getMessage());
         }
         return result;
    }
    
    private String quote(String name) {
        return (quote+name.replaceAll(quote, "")+quote);
    }

    private String buildQuery(String table, String selects, String where, String groupby, String orderby) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("select ");
            buffer.append(selects);
            buffer.append(" from ");
            buffer.append(table);
            if (!where.equalsIgnoreCase("")) {
                    buffer.append(" where ");
                    buffer.append(where);
            }
            if (!groupby.equalsIgnoreCase("")) {
                    buffer.append(" group by ");
                    buffer.append(groupby);
            }
            if (!orderby.equalsIgnoreCase("")) {
                    buffer.append(" order by ");
                    buffer.append(orderby);
            }
            return buffer.toString();
    }

}
