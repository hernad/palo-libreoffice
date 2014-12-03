/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.palooca;

import com.jedox.palojlib.interfaces.ICellLoadContext;
import com.jedox.palojlib.interfaces.ICube;
import com.jedox.palojlib.interfaces.IDimension;
import com.jedox.palojlib.interfaces.IElement;
import com.jedox.palojlib.exceptions.PaloJException;
import com.jedox.palojlib.exceptions.PaloException;
import com.jedox.palojlib.interfaces.ICell;
import com.jedox.palojlib.interfaces.ICellsExporter;
import com.jedox.palojlib.main.CellLoadContext;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author chris
 */
public class PaloLibUtil {

    public static int getElementDepth(IElement element) {
        int depth = 0;
        for (IElement parent: element.getParents()) {
            depth = Math.max(depth, getElementDepth(parent)+1);
        }
        return depth;
    }

    public static void setData(ICube cube, String[] coords, Object value) {
        setData(cube,coords,value,ICube.SplashMode.SPLASH_NOSPLASHING);
    }

    public static IElement[] getElements(ICube cube, String[] coords) {
        IElement[] path = new IElement[coords.length];
        IDimension[] dims = cube.getDimensions();
        for (int i=0; i<coords.length;i++) {
            path[i] = dims[i].getElementByName(coords[i], false);
        }
        return path;
    }

    public static void setData(ICube cube, String[] coords, Object value, ICube.SplashMode splashMode) {
        setData(cube,getElements(cube,coords),value,splashMode);
    }

    public static void setData(ICube cube, IElement[] coords, Object value, ICube.SplashMode splashMode) {
        ICellLoadContext context = new CellLoadContext(splashMode, 1, false, true);
        cube.loadCells(new IElement[][]{coords}, new Object[]{value}, context,null);
    }

    public static Object getData(ICube cube, String[] coords) {
        return cube.getCell(getElements(cube,coords)).getValue();
    }

    public static final boolean setInput(ICube cube, IElement [] coordinate, Object input, char thousandSeparator, char decimalPoint) {
        if (input == null) {
                return false;
        }

        // Are we in a text cell?
        for (IElement e: coordinate) {
            if (e.getType() == IElement.ElementType.ELEMENT_STRING) {
                // Yes, so just set the input.
                setData(cube,coordinate, input, ICube.SplashMode.SPLASH_NOSPLASHING);
                return true;
            }
        }

        // Trim input:
        String textInput = input.toString().trim();

        // Look for splash characters:
        if (textInput.startsWith("#")) {
                if (textInput.endsWith("%")) {
                        double newVal = convertNumber(textInput.substring(1, textInput.length() - 1), thousandSeparator, decimalPoint);
                        double oldValue = (Double) cube.getCell(coordinate).getValue();
                        newVal = oldValue * (1+(newVal/100));
                        setData(cube,coordinate, new Double(newVal), ICube.SplashMode.SPLASH_DEFAULT);
                } else {
                        setData(cube,coordinate, convertNumber(textInput.substring(1), thousandSeparator, decimalPoint), ICube.SplashMode.SPLASH_DEFAULT);
                }
        } else if (textInput.startsWith("!!")) {
                setData(cube,coordinate, convertNumber(textInput.substring(2), thousandSeparator, decimalPoint), ICube.SplashMode.SPLASH_ADD);
        } else if (textInput.startsWith("!")) {
                setData(cube,coordinate, convertNumber(textInput.substring(1), thousandSeparator, decimalPoint), ICube.SplashMode.SPLASH_SET);
        } else {
                // No splashing. Look for copy/like:
                String temp = textInput.toLowerCase();
                int index;
                if (temp.startsWith("copy") && temp.length() > 5) {
                        IElement [] source = parseCopyLikeParameter(cube,textInput.substring(5));
                        for (int i = 0; i < source.length; i++) {
                                if (source[i] == null) {
                                        source[i] = coordinate[i];
                                }
                        }
                        return copyCell(cube,source, coordinate);
                } else if ((index = temp.indexOf("like")) != -1 && (index + 5) < temp.length()) {
                        IElement [] source = parseCopyLikeParameter(cube,textInput.substring(index + 5));
                        for (int i = 0; i < source.length; i++) {
                                if (source[i] == null) {
                                        source[i] = coordinate[i];
                                }
                        }
                        String valText = textInput.substring(0, index).trim();
                        return copyCell(cube,source, coordinate);
                        //return copyCell(cube,source, coordinate, convertNumber(valText, thousandSeparator, decimalPoint)); //TODO implement real copy like
                } else {
                        // No copy/like, just set the data:
                        setData(cube,coordinate, convertNumber(textInput, thousandSeparator, decimalPoint),ICube.SplashMode.SPLASH_NOSPLASHING);
                }
        }

        return true;
    }

    public static final boolean copyCell(ICube cube, IElement[] src, IElement[] dest) { //TODO implement real copy like
        try {
            Object value = cube.getCell(src).getValue();
            setData(cube,dest,value,ICube.SplashMode.SPLASH_DEFAULT);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }


    public static final Double convertNumber(String str, char thsep, char dec) {
        try {
            StringBuffer buf = new StringBuffer();
            for (char c: str.toCharArray()) {
                    if (c == thsep) {
                            continue;
                    } else if (c == dec) {
                            buf.append(".");
                            continue;
                    }
                    buf.append(c);
            }
            return Double.parseDouble(buf.toString());
        } catch (Throwable t) {
            throw new PaloJException("Error converting '" + str + "' to a number.");
        }
    }

    private static final IElement[] parseCopyLikeParameter(ICube cube, String params) {
        try {
                String [] tokens = params.split(";");
                IDimension[] dimensions = cube.getDimensions();
                IElement [] coords = new IElement[dimensions.length];
                for (int i = 0; i < dimensions.length; i++) {
                        coords[i] = null;
                }

                for (String t: tokens) {
                        t = t.trim();
                        int index;
                        if ((index = t.indexOf(":")) != -1) {
                                String dimName = t.substring(0, index);
                                String elementName = t.substring(index + 1);
                                IDimension d = cube.getDimensionByName(dimName);
                                if (d == null) {
                                        throw new PaloJException("Dimension '" + dimName + "' does not exist in cube '" + cube.getName() + "'.");
                                }
                                boolean found = false;
                                for (int i = 0; i < dimensions.length; i++) {
                                        if (dimensions[i].equals(d)) {
                                            coords[i] = d.getElementByName(elementName,false);
                                            if (coords[i] != null) {
                                                    found = true;
                                            }
                                            break;
                                        }
                                }
                                if (!found) {
                                        throw new PaloJException("Element '" + elementName + "' does not exist in dimension '" + dimName + "'.");
                                }
                        } else {
                                boolean found = false;
                                for (int i = 0; i < dimensions.length; i++) {
                                        IElement e = dimensions[i].getElementByName(t,false);
                                        if (e != null) {
                                                if (found) {
                                                        throw new PaloJException("Element name '" + t + "' is ambiguous. Add a dimension name to disambiguate.");
                                                }
                                                coords[i] = e;
                                                found = true;
                                        }
                                }
                                if (!found) {
                                        throw new PaloJException("Unknown element '" + t + "'.");
                                }
                        }
                }

                return coords;
        } catch (PaloException e) {
                throw new PaloJException(e.getDescription() + ", " + e.getReason());
        } catch (PaloJException e) {
                throw e;
        } catch (Throwable t) {
                throw new PaloJException("Error parsing copy/like parameters in '" + params + "'.");
        }
    }
    
    public static String getPathString(Object[] path) {
        StringBuffer buffer = new StringBuffer();
        for (Object o : path) {
            buffer.append(o);
            buffer.append("|");
        }
        return buffer.toString();
    }

    public static long hash(String string) {
      long h = 1125899906842597L; // prime
      int len = string.length();

      for (int i = 0; i < len; i++) {
        h = 31*h + string.charAt(i);
      }
      return h;
    }

    public static synchronized Map<String,ICell> buildCellLookup(ICellsExporter export) {
        Map<String,ICell> result = new HashMap<String,ICell>();
        while (export.hasNext()) {
            ICell cell = export.next();
            if (cell != null && cell.getPathNames() != null)
                result.put(getPathString(cell.getPathNames()), cell);
        }
        return result;
    }

    public static String checkRights(String value, boolean needsChange) {
        if ("S".equalsIgnoreCase(value) && needsChange) return "D";
        return value;
    }


}
