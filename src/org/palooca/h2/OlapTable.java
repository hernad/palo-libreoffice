/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.palooca.h2;

import com.jedox.palojlib.interfaces.ICell;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import org.h2.command.ddl.CreateTableData;
import org.h2.engine.Session;
import org.h2.index.Index;
import org.h2.index.IndexType;
import org.h2.message.DbException;
import org.h2.result.Row;
import org.h2.table.Column;
import org.h2.table.IndexColumn;
import org.h2.table.TableBase;
import org.h2.tools.SimpleResultSet;
import org.h2.value.DataType;

/**
 *
 * @author chris
 */
public class OlapTable extends TableBase {

    private final long rowCount = 0;
    private SimpleResultSet rs;


    public OlapTable(CreateTableData data, Collection<ICell> olapData) {
        super(data);
        rs = new SimpleResultSet(new OlapRowSource(olapData));
        for (Column c : data.columns) {
            rs.addColumn(c.getName(), DataType.convertTypeToSQLType(c.getType()), (int)c.getPrecision(), c.getScale());
        }
        setColumns(data.columns.toArray(new Column[data.columns.size()]));
    }

    @Override
    public void lock(Session sn, boolean bln, boolean bln1) {
        //nothing to to
    }

    @Override
    public void close(Session sn) {
        //nothing to to
    }

    @Override
    public void unlock(Session sn) {
        //nothing to to
    }

    @Override
    public Index addIndex(Session sn, String string, int i, IndexColumn[] ics, IndexType it, boolean bln, String string1) {
        throw DbException.getUnsupportedException("ALIAS");
    }

    @Override
    public void removeRow(Session sn, Row row) {
        throw DbException.getUnsupportedException("ALIAS");
    }

    @Override
    public void truncate(Session sn) {
        throw DbException.getUnsupportedException("ALIAS");
    }

    @Override
    public void addRow(Session sn, Row row) {
        throw DbException.getUnsupportedException("ALIAS");
    }

    @Override
    public void checkSupportAlter() {
        throw DbException.getUnsupportedException("ALIAS");
    }

    @Override
    public String getTableType() {
        return null;
    }

    @Override
    public Index getScanIndex(Session sn) {
        return new OlapIndex(this, IndexColumn.wrap(columns));
    }

    @Override
    public Index getUniqueIndex() {
       return null;
    }

    @Override
    public ArrayList<Index> getIndexes() {
         return null;
    }

    @Override
    public boolean isLockedExclusively() {
         return false;
    }

    @Override
    public long getMaxDataModificationId() {
        return Long.MAX_VALUE;
    }

    @Override
    public boolean isDeterministic() {
        return true;
    }

    @Override
    public boolean canGetRowCount() {
        return rowCount != Long.MAX_VALUE;
    }

    @Override
    public boolean canDrop() {
        throw DbException.throwInternalError();
    }

    @Override
    public long getRowCount(Session sn) {
        return rowCount;
    }

    @Override
    public long getRowCountApproximation() {
        return rowCount;
    }

    @Override
    public long getDiskSpaceUsed() {
        return 0;
    }

    @Override
    public void checkRename() {
        throw DbException.getUnsupportedException("ALIAS");
    }

    @Override
    public boolean canReference() {
        return false;
    }

    public ResultSet getResultSet() {
       return rs;
    }

}
