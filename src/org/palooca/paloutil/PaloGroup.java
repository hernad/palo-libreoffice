/*
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
 */

package org.palooca.paloutil;

import com.jedox.palojlib.interfaces.ICube;
import com.jedox.palojlib.interfaces.IDatabase;
import com.jedox.palojlib.interfaces.IElement;
import org.palooca.PaloLibUtil;

/**
 *
 * @author MichaelRaue
 */
public class PaloGroup {
	public static final String CUBE_GROUP_ROLE = "#_GROUP_ROLE"; //$NON-NLS-1$
	public static final String CUBE_GROUP_RIGHT = "#_GROUP_CUBE_DATA"; //$NON-NLS-1$
    IElement element;
    IDatabase sysDB;

    public PaloGroup(IElement element, IDatabase sysDB) {
        this.element = element;
        this.sysDB = sysDB;
    }

    public IDatabase getSystemDatabase() {
        return sysDB;
    }

    public IElement getElement() {
        return element;
    }

    public String getName() {
        return element.getName();
    }

    public boolean hasRole(String role)
    {
	ICube groupRoleCube = sysDB.getCubeByName(CUBE_GROUP_ROLE);
        if (groupRoleCube == null)
            return false;

		String[] coords = new String[2];

		coords[0] = getName();
        coords[1] = role;

    	Object val = PaloLibUtil.getData(groupRoleCube, coords);
        if (val != null)
            return val.toString().equals("1");  //$NON-NLS-1$
        else
            return false;
    }

    public void setRole(String role, String value)
    {
	ICube groupRoleCube = sysDB.getCubeByName(CUBE_GROUP_ROLE);
        if (groupRoleCube == null)
            return;

		String[] coords = new String[2];

		coords[0] = getName();
        coords[1] = role;

        PaloLibUtil.setData(groupRoleCube, coords, value);

        return;
    }

    static public void setCubeRight(IDatabase database, ICube cube, String group, String value)
    {
	ICube groupCubeRight = database.getCubeByName(CUBE_GROUP_RIGHT);
        if (groupCubeRight == null)
            return;

		String[] coords = new String[2];

		coords[0] = group;
        coords[1] = cube.getName();

        //Value check. S is only valid for Cell data
        value = PaloLibUtil.checkRights(value, true);
        PaloLibUtil.setData(groupCubeRight, coords, value);

        return;
    }

    public String getCubeRight(IDatabase database, ICube cube)
    {
	ICube groupCubeRight = database.getCubeByName(CUBE_GROUP_RIGHT);
        if (groupCubeRight == null)
            return "";

		String[] coords = new String[2];

		coords[0] = getName();
        coords[1] = cube.getName();

        Object  value = PaloLibUtil.getData(groupCubeRight, coords);
        if (value != null)
            return value.toString();
        else
            return new String("");
    }
}
