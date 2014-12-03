/*
 * SetDataResult.java
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
 * Created on 31. Oktober 2007, 16:14
 *
 */

package org.palooca.dataimport;

import com.sun.star.uno.XComponentContext;
import java.util.Arrays;
import com.jedox.palojlib.interfaces.ICube;
import com.jedox.palojlib.interfaces.IDatabase;
import com.jedox.palojlib.exceptions.*;
import org.palooca.PalOOCaImpl;
import org.palooca.PaloLibUtil;

/**
 *
 * @author Andreas Schneider
 */
public class SetDataResult extends ImportResult{
    
    private static boolean initialized = false;
    
    protected boolean splash;
    protected String servdb;
    protected String cubeName;
    protected Object[] coordinate;
    
    public SetDataResult(XComponentContext context, Object value, Double splash, String servdb, String cubeName, Object[] coordinate) {
        super(context);
        this.value = value;
        this.splash = (splash != 0.0);
        this.servdb = servdb;
        this.cubeName = cubeName;
        this.coordinate = coordinate;

        // Initialize the database to overcome a bug where the first
        // setData functions fails if the connectoin hasn't been made before
        if (SetDataResult.initialized == false){
            IDatabase database = connectionHandler.getDatabase(servdb);
            if (database != null)
                SetDataResult.initialized = true;
        }
    }
    
    @Override
    protected boolean execute() {
        try {
            IDatabase database = connectionHandler.getDatabase(servdb);
            if (database == null)
                return false;
            
            ICube cube = database.getCubeByName(cubeName);
            if (cube == null)
                return false;
            
            String[] coords = PalOOCaImpl.processCoordinates(coordinate);

            PaloLibUtil.setData(cube, coords, value, splash ? ICube.SplashMode.SPLASH_DEFAULT : ICube.SplashMode.SPLASH_NOSPLASHING);

            return true;
        } catch (PaloException pe) {
            error = pe.getLocalizedMessage();
            return false;
        } catch (PaloJException e) {
            error = e.getLocalizedMessage();
            return false;
        } catch( Exception e ) {
            return false;
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
        final SetDataResult other = (SetDataResult) obj;
        if ((this.context == null) ? (other.context != null) : !this.context.equals(other.context)) {
            return false;
        }
        if (this.splash != other.splash) {
            return false;
        }
        if ((this.cubeName == null) ? (other.cubeName != null) : !this.cubeName.equals(other.cubeName)) {
            return false;
        }
        if (!Arrays.deepEquals(this.coordinate, other.coordinate)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (this.context != null ? this.context.hashCode() : 0);
        hash = 67 * hash + (this.splash ? 1 : 0);
        hash = 67 * hash + (this.servdb != null ? this.servdb.hashCode() : 0);
        hash = 67 * hash + (this.cubeName != null ? this.cubeName.hashCode() : 0);
        hash = 67 * hash + Arrays.deepHashCode(this.coordinate);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("PALO.SETDATA(");
        result.append(value);
        result.append("; ");
        result.append(splash ? "TRUE" : "FALSE");
        result.append("; \"");
        result.append(servdb);
        result.append("\"; \"");
        result.append(cubeName);
        result.append("\"");
        for (int i = 0; i < coordinate.length; i++) {
            result.append("; \"");
            result.append(coordinate[i]);
            result.append('\"');
        }
        result.append(')');
        
        return result.toString();
    }
    
}