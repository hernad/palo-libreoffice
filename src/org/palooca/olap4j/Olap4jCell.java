package org.palooca.olap4j;

import com.jedox.palojlib.exceptions.PaloException;
import com.jedox.palojlib.exceptions.PaloJException;
import com.jedox.palojlib.interfaces.ICell;

public class Olap4jCell implements ICell {
	
	private String[] pathnames;
	private Object value;
	
	//private IDimension[] dimensions;
	
	public Olap4jCell(Object value) {
		this.value = value;
	}

	@Override
	public String[] getPathNames() {
	    return pathnames;
	}

	@Override
	public Object getValue() {
		return value;
	}
	
	public void setPathNames(String[] pathnames) {
		this.pathnames = pathnames;
	}

	@Override
	public CellType getType() {
		return CellType.CELL_NUMERIC;
	}

        @Override
        public String getPathNameAt(int i) throws PaloException, PaloJException {
            return getPathNames()[i];
        }

}
