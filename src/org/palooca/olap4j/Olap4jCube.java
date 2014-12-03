package org.palooca.olap4j;

import com.jedox.palojlib.exceptions.PaloException;
import java.math.BigInteger;
import java.sql.SQLException;

import org.olap4j.OlapException;
import org.olap4j.OlapStatement;
import org.olap4j.metadata.Cube;

import com.jedox.palojlib.exceptions.PaloJException;
import com.jedox.palojlib.interfaces.ICell;
import com.jedox.palojlib.interfaces.ICellExportContext;
import com.jedox.palojlib.interfaces.ICellLoadContext;
import com.jedox.palojlib.interfaces.ICellsExporter;
import com.jedox.palojlib.interfaces.ICube;
import com.jedox.palojlib.interfaces.IDimension;
import com.jedox.palojlib.interfaces.IElement;
import com.jedox.palojlib.interfaces.IRule;
import com.jedox.palojlib.main.CellExportContext;
import java.util.List;

public class Olap4jCube implements ICube {
	
	private Cube cube;
	private Olap4jDatabase database;
        private Integer id;
	
	public Olap4jCube(Olap4jDatabase database, Cube cube) {
		this.cube = cube;
		this.database = database;
	}

	@Override
	public String getName() {
		return cube.getName();
	}

	@Override
	public int getId() {
            if (id == null) {
		try {
			id = cube.getSchema().getCubes().indexOf(cube);
		}
		catch (OlapException e) {
			id = 0;
		}
            }
            return id;
	}

	@Override
	public BigInteger getNumberOfCells() throws PaloJException {
		return BigInteger.ZERO;
	}

	@Override
	public BigInteger getNumberOfFilledCells() throws PaloJException {
		return BigInteger.ZERO;
	}

	@Override
	public IDimension[] getDimensions() throws PaloJException {
           List<Olap4jDimension> dims = database.getCubeDimensions(getName());
           return dims.toArray(new IDimension[dims.size()]);
	}

	@Override
	public ICellsExporter getCellsExporter(IElement[][] area, ICellExportContext context) throws PaloJException {
		return new Olap4jCellsExporter(area,context,this);
	}

	@Override
	public ICell getCell(IElement[] path) throws PaloJException {
		IElement[][] area = new IElement[getDimensions().length][1];
		for (int i=0; i<path.length; i++) {
			area[i][0] = path[i];
		}
		ICellExportContext context = new CellExportContext(CellsExportType.BOTH,1000,true,false,false);
		ICellsExporter exporter = new Olap4jCellsExporter(area,context,this);
		if (exporter.hasNext()) return exporter.next();
		return null;
	}

	@Override
	public void clearCells(IElement[][] area) throws PaloJException {
		throw new PaloJException("Clearing cells not implemented yet.");
	}

	@Override
	public void addRule(String definition, boolean activate,
			String externalIdentifier, String comment) throws PaloJException {
		throw new PaloJException("Adding rules is not supported by OLAP4j provider.");

	}

	@Override
	public IRule[] getRules() {
		return new IRule[0];
	}

	@Override
	public void convert(CubeType type) throws PaloJException {
		throw new PaloJException("Cube format conversion is not supported by OLAP4j provider.");
	}

	@Override
	public void clear() throws PaloJException {
		StringBuilder buffer = new StringBuilder("UPDATE CUBE "+cube.getName()+" SET ROOT() = Null NO_ALLOCATION");
		try {
			OlapStatement statement = getCube().getSchema().getCatalog().getDatabase().getOlapConnection().createStatement();
			statement.executeUpdate(buffer.toString());
		} catch (OlapException e) {
			throw new PaloJException("Error updating cube "+getName()+": "+e.getMessage());
		} catch (SQLException e) {
			throw new PaloJException("Error updating cube "+getName()+": "+e.getMessage());
		} catch (UnsupportedOperationException e) {
			throw new PaloJException("Updateing cube is not supported by OLAP4j provider.");
		}
	}

	@Override
	public CubeType getType() {
		return CubeType.CUBE_NORMAL;
	}

	@Override
	public void removeRules(IRule[] rules) throws PaloJException {
		throw new PaloJException("Removing rules is not supported by OLAP4j provider.");
	}

	@Override
	public void updateRule(int id, String definition, boolean activate,
			String externalIdentifier, String comment) throws PaloJException {
		throw new PaloJException("Updateing rules is not supported by OLAP4j provider.");
	}

	@Override
	public void save() throws PaloJException {
		throw new PaloJException("Saving cube is not supported by OLAP4j provider.");
	}

	@Override
	public void rename(String newname) throws PaloJException {
		throw new PaloJException("Renaming cube is not supported by OLAP4j provider.");
	}

	@Override
	public void commitLock(int lockId) throws PaloJException {
		throw new PaloJException("Locking is not supported by OLAP4j provider.");
	}

	@Override
	public int lockArea(IElement[][] area) throws PaloJException {
		throw new PaloJException("Locking is not supported by OLAP4j provider.");
	}

	@Override
	public int lockComplete() throws PaloJException {
		throw new PaloJException("Locking is not supported by OLAP4j provider.");
	}

	@Override
	public Olap4jDimension getDimensionByName(String name) throws PaloJException {
		return database.getDimensionByName(name);
	}
	
	public Cube getCube() {
		return cube;
	}
	
	public void clearLookup() {
            //nothing to do
	}

	@Override
	public void removeRules() throws PaloJException {
		throw new PaloJException("RemoveRules is not supported by OLAP4j provider.");
	}

	@Override
	public void activateRules(IRule[] arg0) throws PaloJException {
		throw new PaloJException("activateRules is not supported by OLAP4j provider.");
	}

	@Override
	public void deactivateRules(IRule[] arg0) throws PaloJException {
		throw new PaloJException("deactivateRules is not supported by OLAP4j provider.");
	}

	@Override
	public String parseRule(String definition) throws PaloJException,
			PaloJException {
		throw new PaloJException("parseRule is not supported by OLAP4j provider.");
	}

    @Override
    public void loadCells(IElement[][] ies, Object[] os, ICellLoadContext iclc, IElement[][] ies1) throws PaloException, PaloJException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long getCBToken() throws PaloException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long getCCToken() throws PaloException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public IElement[] getCellPath(String[] strings) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ICellLoadContext getCellLoadContext(SplashMode sm, int i, boolean bln, boolean bln1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
