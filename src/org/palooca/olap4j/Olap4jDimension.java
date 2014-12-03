package org.palooca.olap4j;

import java.util.HashMap;


import org.olap4j.OlapException;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.NamedList;
import org.olap4j.metadata.Schema;
import org.olap4j.metadata.Member;

import com.jedox.palojlib.exceptions.PaloException;
import com.jedox.palojlib.exceptions.PaloJException;
import com.jedox.palojlib.interfaces.IAttribute;
import com.jedox.palojlib.interfaces.IConsolidation;
import com.jedox.palojlib.interfaces.IDimension;
import com.jedox.palojlib.interfaces.IElement;
import com.jedox.palojlib.interfaces.IElement.ElementType;
import com.jedox.palojlib.main.Consolidation;
import com.jedox.palojlib.main.DimensionInfo;
import java.sql.ResultSet;
import java.util.logging.Logger;

public class Olap4jDimension implements IDimension {

	private class Olap4jDimensionInfo extends DimensionInfo {
		public Olap4jDimensionInfo(String maxLevel, String maxDepth) {
			super("","",maxLevel,"0",maxDepth,"0","0");
		}
	}
	
	private Dimension dimension;
	private Schema schema;
	private Olap4jDimensionInfo info;
	private ElementCache elements;
        private static Logger log = Logger.getLogger(Olap4jDimension.class.getName());
        private Integer id;
	
	public Olap4jDimension(Dimension dimension, Schema schema) throws PaloException {
		this.dimension = dimension;
		this.schema = schema;
		NamedList<Level> levels = dimension.getDefaultHierarchy().getLevels();
		int maxDepth = 0;
		int maxLevel = levels.size();
		for (Level l : levels) {
			maxDepth = Math.max(maxDepth, l.getDepth());
		}
		info = new Olap4jDimensionInfo(String.valueOf(maxLevel),String.valueOf(maxDepth));
		elements = new ElementCache(this);
                
                try {
                    ResultSet rs = schema.getCatalog().getMetaData().getProperties(schema.getCatalog().getName(),schema.getName(),null,dimension.getUniqueName(),null,null,null,null);
                    while (rs.next()) {
                        for (int i=0; i<13; i++) {
                            System.err.print(rs.getObject(i+1)+";");
                        }
                        System.err.println();
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();;
                }
                 
	}

	@Override
	public String getName() {
		return dimension.getName();
	}

	@Override
	public int getId() {
            if (id == null) {
		try {
			id = schema.getSharedDimensions().indexOf(dimension);
		}
		catch (OlapException e) {
			id = 0;
		}
             }
            return id;
	}

	@Override
	public DimensionType getType() {
		return DimensionType.DIMENSION_NORMAL;
	}

	@Override
	public DimensionInfo getDimensionInfo() throws PaloException {
		return info;
	}

	@Override
	public IElement[] getElements(boolean withAttributes) throws PaloException {
		return getElementCache().getElements();
	}

	@Override
	public IElement[] getRootElements(boolean withAttributes) throws PaloException {
		return elements.getRootElements();
	}

	@Override
	public Olap4jElement getElementByName(String name, boolean withAttributes) throws PaloException {
		return getElementCache().getElement(name);
	}

	@Override
	public Olap4jAttribute getAttributeByName(String name) throws PaloException {
		return getElementCache().getAttribute(name);
	}

	@Override
	public void addElements(String[] names, ElementType[] types) {
		throw new PaloException("Adding elements is not supported by OLAP4j provider.");
	}

	@Override
	public IElement addBaseElement(String name, ElementType type) {
		throw new PaloException("Adding elements is not supported by OLAP4j provider.");
	}

	@Override
	public void addAttributes(String[] names, ElementType[] types) {
		throw new PaloException("Adding attributes is not supported by OLAP4j provider.");
	}

	@Override
	public void removeElements(IElement[] elements) {
		throw new PaloException("Removing elements is not supported by OLAP4j provider.");
	}

	@Override
	public void removeAttributes(IAttribute[] attributes) {
		throw new PaloException("Removing attributes is not supported by OLAP4j provider.");
	}

	@Override
	public void removeAttributeValues(IAttribute attribute, IElement[] elements) {
		for (IElement e : elements) {
			for (Member m : getElementCache().getMembers(e.getName())) {
				try {
					m.setProperty(getElementCache().getAttribute(attribute.getName()).getProperty(), null);
				} catch (OlapException ex) {
					log.log(java.util.logging.Level.SEVERE, "Cannot remove attribute value: "+ex.getMessage());
				}
				catch (UnsupportedOperationException ex1) {
					log.log(java.util.logging.Level.SEVERE,"Removing attribute values is not supported by OLAP4j provider.");
				}
			}
		}
	}

	@Override
	public void addAttributeValues(IAttribute attribute, IElement[] elements, Object[] values) {
		int i = 0;
		for (IElement e : elements) {
			for (Member m : getElementCache().getMembers(e.getName())) {
				try {
					m.setProperty(getElementCache().getAttribute(attribute.getName()).getProperty(), values[i]);
				} catch (OlapException ex) {
					log.log(java.util.logging.Level.SEVERE,"Cannot add attribute value: "+ex.getMessage());
				}
				catch (UnsupportedOperationException ex1) {
					log.log(java.util.logging.Level.SEVERE,"Adding attribute values is not supported by OLAP4j provider.");
				}
			}
			i++;
		}

	}

	@Override
	public void removeConsolidations(IElement[] elements) {
		throw new PaloException("Removing consolidations is not supported by OLAP4j provider.");
	}


	@Override
	public void removeAttributeConsolidations(IAttribute attribute) {
		throw new PaloException("Removing attribute consolidations is not supported by OLAP4j provider.");

	}

	@Override
	public void addAttributeConsolidation(IAttribute attribute, IAttribute child) {
		throw new PaloException("Adding attribute consolidations is not supported by OLAP4j provider.");
	}

	@Override
	public Consolidation newConsolidation(IElement parent, IElement child,
			double weight) {
		throw new PaloException("Adding consolidations is not supported by OLAP4j provider.");
	}

	@Override
	public IAttribute[] getAttributes() {
		return getElementCache().getAttributes(true);
	}

	@Override
	public void rename(String newname) throws PaloException {
		throw new PaloException("Renaming dimension is not supported by OLAP4j provider.");
	}

	@Override
	public void updateConsolidations(IConsolidation[] consolidations) {
		throw new PaloException("Updateing consolidation is not supported by OLAP4j provider.");	
	}
	
	public ElementCache getElementCache() {
		return elements;
	}
	
	public Dimension getDimension() {
		return dimension;
	}

	@Override
	public HashMap<String, IElement[]> getChildrenMap() throws PaloException {
		throw new PaloException("Not supported by OLAP4j provider.");
	}

	@Override
	public HashMap<String, HashMap<String, Object>> getAttributesMap()throws PaloException {
		throw new PaloException("Not supported by OLAP4j provider.");
	}

	@Override
	public HashMap<String, HashMap<String, Double>> getWeightsMap() throws PaloException {
		throw new PaloException("Not supported by OLAP4j provider");
	}

	@Override
	public void setCacheTrustExpiry(int arg0) {
		// throw new PaloException("Not supported by OLAP4j provider");
		
	}

	@Override
	public void updateElementsType(IElement[] arg0, ElementType arg1)
			throws PaloException, PaloJException {
		// TODO Auto-generated method stub
		
	}

        @Override
    public IElement[] getBasesElements(boolean bln) throws PaloException, PaloJException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public IElement[] getElementsByName(String[] strings, boolean bln) throws PaloException, PaloJException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasConsolidatedElements() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setWithElementPermission(boolean bln) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void resetCache() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int removeAllConsolidations() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void appendElements(IElement[] ies) throws PaloException, PaloJException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void moveElements(IElement[] ies, Integer[] intgrs) throws PaloException, PaloJException {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public IAttribute addAttribute(String string, ElementType et) throws PaloJException, PaloException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public IElement getSingleElement(String name, boolean withAttributes) throws PaloException, PaloJException {
        return getElementByName(name, withAttributes);
    }
	


}
