/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.palooca.h2;

import com.jedox.palojlib.interfaces.ICell;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import org.h2.tools.SimpleRowSource;

/**
 *
 * @author chris
 */
public class OlapRowSource implements SimpleRowSource {

    private Collection<ICell> sourceData;
    private Iterator<ICell> iterator;


    public OlapRowSource(Collection<ICell> sourceData) {
        this.sourceData = sourceData;
        iterator = sourceData.iterator();
    }

    @Override
    public void close() {
       // do nothing
    }

    @Override
    public Object[] readRow() throws SQLException {
        if (iterator.hasNext())
            return iterator.next().getPathNames();
        return null;
    }

    @Override
    public void reset() throws SQLException {
        iterator = sourceData.iterator();
    }

}
