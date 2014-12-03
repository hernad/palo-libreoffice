/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.palooca.h2;

import com.jedox.palojlib.interfaces.ICell;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.h2.api.TableEngine;
import org.h2.command.ddl.CreateTableData;
import org.h2.table.TableBase;

/**
 *
 * @author chris
 */
public class OlapTableEngine implements TableEngine {

    private static Map<String,Collection<ICell>> olapData = new HashMap<String,Collection<ICell>>();

    public static void addData(String tableName, Collection<ICell> cells) {
        olapData.put(tableName, cells);
    }

    @Override
    public TableBase createTable(CreateTableData data) {
        return new OlapTable(data,olapData.remove(data.tableName.toUpperCase()));
    }

}
