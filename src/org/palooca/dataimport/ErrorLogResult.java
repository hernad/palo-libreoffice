/*
 * ErrorLogResult.java
 *
 * Palo Open Office Calc AddIn
 * Copyright (C) 2008 PalOOCa Team,  Tensegrity Software GmbH, 2009

 * The software is licensed under an Open-Source License (GPL).
 * If you want to redistribute the software you must observe the regulations of
 * the GPL . If you want to redistribute the software without the
 * restrictions of the GPL, you have to contact Tensegrity Software GmbH
 * (Tensegrity) for written consent to do so.
 * Tensegrity may offer commercial licenses for redistribution (Dual Licensing)
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 *
 */

package org.palooca.dataimport;

import com.sun.star.uno.XComponentContext;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Arrays;

/**
 *
 * @author Pieter
 */
public class ErrorLogResult extends ImportResult {

    protected String errorFieldValue;
    protected String path;
    protected String cube;
    protected Object[] coordinate;

    public ErrorLogResult(XComponentContext context, String errorFieldValue, String path, String value, String cube, Object[] coordinate) {
        super(context);

        this.errorFieldValue = errorFieldValue;
        this.path = path;
        this.value = value;
        this.cube = cube;
        this.coordinate = coordinate;
    }

    @Override
    protected boolean execute() {
        PrintWriter out = null;
        try{
            // If the error value that was passed in, is registered as an error
            if (ImportResult.isImportError(errorFieldValue) == true){
                out = new PrintWriter(new FileWriter(path, true));
                out.print(this.value);
                
                if (cube.length() > 0){
                    out.print(ImportResult.getFileDelimiter());
                    out.print(cube);
                }

                for (Object chord : coordinate ){
                    out.print(ImportResult.getFileDelimiter());
                    out.print(chord);
                }

                out.print(ImportResult.getFileDelimiter());
                out.print(errorFieldValue);
                out.println();
            }
            return true;
        }
        catch (Exception e){
            error = e.getLocalizedMessage();
            return false;
        }
        finally{
            if (out != null)
                try{out.close();} catch (Exception e) {}
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ErrorLogResult other = (ErrorLogResult) obj;
        if ((this.context == null) ? (other.context != null) : !this.context.equals(other.context)) {
            return false;
        }
        if ((this.errorFieldValue == null) ? (other.errorFieldValue != null) : !this.errorFieldValue.equals(other.errorFieldValue)) {
            return false;
        }
        if ((this.path == null) ? (other.path != null) : !this.path.equals(other.path)) {
            return false;
        }
        if ((this.cube == null) ? (other.cube != null) : !this.cube.equals(other.cube)) {
            return false;
        }
        if (!Arrays.deepEquals(this.coordinate, other.coordinate)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + (this.context != null ? this.context.hashCode() : 0);
        hash = 47 * hash + (this.errorFieldValue != null ? this.errorFieldValue.hashCode() : 0);
        hash = 47 * hash + (this.path != null ? this.path.hashCode() : 0);
        hash = 47 * hash + (this.cube != null ? this.cube.hashCode() : 0);
        hash = 47 * hash + Arrays.deepHashCode(this.coordinate);
        return hash;
    }


    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("PALO.ERROR_LOG(\"");
        result.append(errorFieldValue);
        result.append("\"; \"");
        result.append(path);
        result.append("\"; \"");
        result.append(value);
        result.append("\"");

        if (coordinate.length > 0 || cube.length() > 0){
            result.append("; \"");
            result.append(cube);
            result.append("\"");
        }

        for (int i = 0; i < coordinate.length; i++) {
            result.append("; \"");
            result.append(coordinate[i]);
            result.append('\"');
        }

        result.append(')');
        
        return result.toString();
    }
}
