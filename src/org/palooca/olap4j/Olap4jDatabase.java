package org.palooca.olap4j;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jedox.palojlib.exceptions.PaloException;
import com.jedox.palojlib.exceptions.PaloJException;
import com.jedox.palojlib.interfaces.ICube;
import com.jedox.palojlib.interfaces.IDatabase;
import com.jedox.palojlib.interfaces.IDimension;
import com.jedox.palojlib.main.DatabaseInfo;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import org.olap4j.OlapException;
import org.olap4j.metadata.*;

public class Olap4jDatabase implements IDatabase {

	
	private Catalog catalog;
	private Map<String,Olap4jDimension> dimensionLookup = new LinkedHashMap<String,Olap4jDimension>();
	private Map<String,Olap4jCube> cubeLookup = new LinkedHashMap<String,Olap4jCube>();
        private Map<String,List<Olap4jDimension>> cubeDimLookup = new LinkedHashMap<String,List<Olap4jDimension>>();
        private Integer id;
        private String name;
        private Schema schema;
	
	public Olap4jDatabase(Catalog catalog) {
		this.catalog = catalog;
	}

	public Schema getSchema() throws OlapException {
            if (schema == null) {
		schema = catalog.getSchemas().get(0);
            }
            return schema;
	}
	
	@Override
	public int getId() {
            if (id == null) {
		try {
			id = catalog.getDatabase().getCatalogs().indexOf(catalog);
		}
		catch (OlapException e) {
			id = 0;
		}
            }
            return id;
	}

	@Override
	public String getName() {
            if (name == null) {
		name = catalog.getName();
            }
            return name;
	}

	@Override
	public DatabaseType getType() {
		return DatabaseType.DATABASE_NORMAL;
	}

	@Override
	public IDimension[] getDimensions() throws PaloException {
            if (dimensionLookup.isEmpty()) {
		try {
                    Set<Dimension> set = new LinkedHashSet<Dimension>();
                    for (Cube c : getSchema().getCubes()) {
                        List<Olap4jDimension> cubeDimList = new ArrayList<Olap4jDimension>();
                        for (Dimension d: c.getDimensions()) {
                            if (!set.contains(d)) {
                                Olap4jDimension wrapper = new Olap4jDimension(d,getSchema());
                                cubeDimList.add(wrapper);
                                dimensionLookup.put(wrapper.getName(), wrapper);
                                set.add(d);
                            } else {
                                cubeDimList.add(dimensionLookup.get(d.getName()));
                            }
                        }
                        cubeDimLookup.put(c.getName(), cubeDimList);
                    }
		}
		catch (OlapException e) {
			throw new PaloException(e.getMessage());
		}
             } 
             return dimensionLookup.values().toArray(new IDimension[dimensionLookup.values().size()]);
	}

        protected List<Olap4jDimension> getCubeDimensions(String cubeName) {
            if (cubeDimLookup.isEmpty()) {
                getDimensions();
            }
            List<Olap4jDimension> result = cubeDimLookup.get(cubeName);
            return result == null ? new ArrayList<Olap4jDimension>() : result;
        }

	@Override
	public ICube[] getCubes() throws PaloException {
            if (cubeLookup.isEmpty()) {
		try {
			NamedList<Cube> schemaCubes = getSchema().getCubes();
			for (int i=0; i<schemaCubes.size();i++) {
                            Olap4jCube wrapper =  new Olap4jCube(this,schemaCubes.get(i));
                            cubeLookup.put(wrapper.getName(), wrapper);
			}
		}
		catch (OlapException e) {
			throw new PaloException(e.getMessage());
		}
           } 
           return cubeLookup.values().toArray(new ICube[cubeLookup.values().size()]);
	}

	@Override
	public IDimension addDimension(String name) throws PaloException {
		throw new PaloException("Adding dimension is not supported by OLAP4j provider.");
	}

	@Override
	public ICube addCube(String name, IDimension[] dimensionsNames) throws PaloException {
		throw new PaloException("Adding cube is not supported by OLAP4j provider.");
	}

	@Override
	public Olap4jDimension getDimensionByName(String name) throws PaloJException {
                if (dimensionLookup.isEmpty()) {
			getDimensions();
		}
		Olap4jDimension result = dimensionLookup.get(name);
		return result;
	}
	
	@Override
	public ICube getCubeByName(String name) throws PaloJException {
                if (cubeLookup.isEmpty()) {
                    getCubes();
                }
		ICube result = cubeLookup.get(name);
		return result;
	}

	@Override
	public void removeCube(ICube cube) throws PaloException {
		throw new PaloException("Renaming cube is not supported by OLAP4j provider.");
	}

	@Override
	public void removeDimension(IDimension dimension) throws PaloException {
		throw new PaloException("Removing dimension is not supported by OLAP4j provider.");
	}

	@Override
	public void save() throws PaloException {
		//do nothing here, since we cannot create any objects using this wrapper anyway
	}

	@Override
	public void rename(String newname) throws PaloException {
		throw new PaloException("Renaming database is not supported by OLAP4j provider.");
	}
	
	public void clearLookup() {
		for (Olap4jDimension wrapper : dimensionLookup.values()) {
			wrapper.getElementCache().clear();
		}
		dimensionLookup.clear();
		for (Olap4jCube wrapper: cubeLookup.values()) {
			wrapper.clearLookup();
		}
		cubeLookup.clear();
	}

	@Override
	public DatabaseInfo getDatabaseInfo() {
		throw new PaloException("getDatabaseInfo is not supported by OLAP4j provider.");
	}

	@Override
	public void setCacheTrustExpiries(int databaseExpiry, int cubeExpiry,
			int dimensionExpiry) {
		// TODO Auto-generated method stub
		
	}

    @Override
    public ICube[] getCubes(IDimension id) throws PaloException, PaloJException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void resetCaches() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

        /*
        @Override
        public void resetCaches() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
         * 
         */

}
