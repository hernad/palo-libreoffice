package org.palooca.olap4j;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.olap4j.Cell;
import org.olap4j.CellSetAxis;
import org.olap4j.OlapStatement;
import org.olap4j.CellSet;
import org.olap4j.OlapException;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Member;

import com.jedox.palojlib.exceptions.PaloJException;
import com.jedox.palojlib.interfaces.ICell;
import com.jedox.palojlib.interfaces.ICellExportContext;
import com.jedox.palojlib.interfaces.ICellsExporter;
import com.jedox.palojlib.interfaces.ICube.CellsExportType;
import com.jedox.palojlib.interfaces.IDimension;
import com.jedox.palojlib.interfaces.IElement;
import com.jedox.palojlib.interfaces.IElement.ElementType;
import java.util.logging.Logger;


public class Olap4jCellsExporter implements ICellsExporter {
	
	
	private CellSet cellSet;
	private int ordinal = 0;
	private Olap4jCell cell;
	private boolean skipEmpty;
	private Olap4jCube cubewrapper;
	private CellsExportType exportType;
	private boolean useRules;
	private boolean onlyBases;
	private boolean aggregate;
	private Map<Dimension,List<Member>> cellSetMembers = new HashMap<Dimension, List<Member>>();
	private List<Integer> pathOrder;
	private List<String> baseSetAxisQuery;
	private static Logger log = Logger.getLogger(Olap4jCellsExporter.class.getName());
	
	public Olap4jCellsExporter(IElement[][] area, ICellExportContext context,
			Olap4jCube cubewrapper) throws PaloJException {
		
		try {
			this.aggregate = true;  
			this.skipEmpty = context.isSkipEmpty();
			this.cubewrapper = cubewrapper;
			this.useRules = context.isUseRules();
			this.onlyBases = context.isOnlyBases();
			this.exportType = context.getCellsExportType();
                        List<Dimension> dimensions = cubewrapper.getCube().getDimensions();
                        for (int i=0; i<area.length; i++) {
			   if (!aggregate || area[i] != null) cellSetMembers.put(dimensions.get(i), null);
			}
                        String query = getQuery(area);
                        cellSet = getCellSet(query);
		}
		catch (OlapException e) {
			e.printStackTrace();
			throw new PaloJException("Failed to execute cell export: "+e.getMessage());
		}		
	}
	
	
	private CellSet getCellSet(String query) throws OlapException {
		OlapStatement statement = cubewrapper.getCube().getSchema().getCatalog().getDatabase().getOlapConnection().createStatement();
		if (!query.isEmpty()) {
			log.log(java.util.logging.Level.INFO,"Starting MDX-Request ");
			log.log(java.util.logging.Level.INFO,"MDX-Statement: "+query);
			CellSet result = statement.executeOlapQuery(query);
			log.log(java.util.logging.Level.INFO,"Sucessfully got MDX result.");
			return result;
		}
		return null;
	}
	
	
	private boolean matchesExportType(ElementType elementType, CellsExportType exportType) {
		return true; //deactivate this until element types do work properly.
		/*
		switch (exportType) {
		case ONLY_NUMERIC : return elementType.equals(ElementType.ELEMENT_NUMERIC);
		case ONLY_STRING: return elementType.equals(ElementType.ELEMENT_STRING);
		default: return true;
		}
		*/
	}
	
	private List<Member> applyCubeRestrictions(Dimension dimension, IElement[] elements) {
		List<Member> result = new ArrayList<Member>();
		for (int j=0; j<elements.length; j++) {
			if ((!onlyBases || elements[j].getChildCount() == 0) && (matchesExportType(elements[j].getType(),exportType))) {
				Member m = cubewrapper.getDimensionByName(dimension.getName()).getElementCache().getMembers(elements[j].getName()).get(0);
				if (useRules || !m.isCalculated()) result.add(m);
			}
		}
		return result;
	}
	
	private String getDimensionMembersEnumeration(List<Member> members) {
		StringBuilder buffer = new StringBuilder();
		for (Member m : members) {
			buffer.append(m.getUniqueName()+",");
		}
		buffer.deleteCharAt(buffer.length()-1);
		return buffer.toString();
	}
	
	private String getNonSpecifiedDimensionMembersByMDX(Dimension dimension) throws OlapException {
		StringBuilder buffer = new StringBuilder();
		if (!onlyBases) { //take all memmbers of dimension
			buffer.append("["+dimension.getName()+"].MEMBERS");
		}
		else { //take only leaf members of dimension via mdx function (more performant that enumerating potentially all members explicitly in statement)
			List<Level> levels = dimension.getDefaultHierarchy().getLevels();
			int maxLevel = levels.size()+1;
			for (Member m : dimension.getDefaultHierarchy().getRootMembers()) {
				if (m.getChildMemberCount() > 0) { //root element is no leaf member. Take leafes
					buffer.append("Descendants("+m.getUniqueName()+","+maxLevel+",LEAVES),");
				}
				else { //root element is itself an leaf member
					buffer.append(m.getUniqueName()+",");
				}
			}
			buffer.deleteCharAt(buffer.length()-1);
		}
		return buffer.toString();
	}
	
	private String getQuery(IElement[][] area) throws OlapException {
		List<Dimension> dimensions = cubewrapper.getCube().getDimensions();
		StringBuilder buffer = new StringBuilder("SELECT ");
		int axisCount = 0;
		for (Dimension d : cellSetMembers.keySet()) {
			int i = dimensions.indexOf(d);
			if (!aggregate || area[i] != null) { //no aggregation. Try to detect suitable members for this dimension.
				if (skipEmpty) buffer.append("NON EMPTY ");
				if (!useRules) buffer.append("STRIPCALCULATEDMEMBERS(");
				buffer.append("{");
				if (area[i] == null) { // no dimension filter present
					buffer.append(getNonSpecifiedDimensionMembersByMDX(dimensions.get(i)));
				}
				else { //take elements from dimension filter.
					buffer.append(getDimensionMembersEnumeration(applyCubeRestrictions(dimensions.get(i), area[i])));
				}
				buffer.append("}");
				if (!useRules) buffer.append(")");
				buffer.append(" ON ");
				buffer.append(String.valueOf(axisCount));
				buffer.append(",");
				axisCount++;
			}
		}
		buffer.deleteCharAt(buffer.length()-1);
		buffer.append(" FROM ["+cubewrapper.getName()+"]");
		return buffer.toString();
	}
	

	@Override
	public ICell next() {
		//NOTE: This function delivers the actual cell and does not actually fetch the next cell. 
		return cell;
	}

	@Override
	public boolean hasNext() {
		//NOTE: This function also fetches the next cell. So make sure to call it only once
		if (cellSet == null) return cleanup();
		try {
			Object value = null;
			Cell olapCell = null;
			do {
				olapCell = cellSet.getCell(ordinal++);
				value = olapCell.getValue();
			}
			while (skipEmpty && (value == null || value.equals(""))); //final check for nonEmpty since MDX NON EMPTY works on tuples, not on cells and will return empty cells for non empty tuples.
			cell = new Olap4jCell(value);
			cell.setPathNames(getOrderedPath(olapCell));
			return true;
		}
		catch (Exception e) {
			return cleanup();
		}
	}
	
	private boolean cleanup() {
		cellSetMembers.clear();
		if (baseSetAxisQuery != null)
			baseSetAxisQuery.clear();
		if (cellSet != null) { 
			try {
				cellSet.close();
			} catch (SQLException e) {}
			cellSet = null;
		}
		return false;
	}

	
	private List<Integer> getPathDimensionOrder() {
		if (pathOrder == null) {
			List<Integer> result = new ArrayList<Integer>();
			List<Dimension> dims = new ArrayList<Dimension>();
			for (Dimension d : cubewrapper.getCube().getDimensions()) {
				if (cellSetMembers.keySet().contains(d)) {
					dims.add(d);
				}
			}
			Iterator<Dimension> iterator = cellSetMembers.keySet().iterator();
			while (iterator.hasNext()) {
				result.add(dims.indexOf(iterator.next()));
			}
			pathOrder = result;
		}
		return pathOrder;
	}
	
	private String[] getOrderedPath(Cell cell) {
	    List<Integer> pathDimOrder = getPathDimensionOrder();
	    List<Integer> coordinates = cell.getCoordinateList();
	    List<CellSetAxis> axes = cell.getCellSet().getAxes();
	    //String[] result = new String[coordinates.size()];
	    String[] result = new String[coordinates.size()];
	    for (int i=0; i<coordinates.size(); i++) {
	    	Integer coordinate = coordinates.get(i);
	    	result[pathDimOrder.get(i)] = axes.get(i).getPositions().get(coordinate).getMembers().get(0).getName();
	    }
	    
	    for (String s : result) {
	    	if (s == null) {
	    		log.log(java.util.logging.Level.WARNING,"Path element is null.");
	    	}
	    }
		return result;
	}

	@Override
	public IDimension[] getDimensions() {
		List<IDimension> result = new ArrayList<IDimension>();
		for (Dimension d : cubewrapper.getCube().getDimensions()) {
			if (cellSetMembers.keySet().contains(d)) {
				result.add(cubewrapper.getDimensionByName(d.getName()));
			}
		}
		return result.toArray(new IDimension[result.size()]);
	}
	
	}
