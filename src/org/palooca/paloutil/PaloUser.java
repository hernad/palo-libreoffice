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
public class PaloUser {
	public static final String CUBE_USER_GROUP_ROLE = "#_USER_GROUP"; //$NON-NLS-1$
	public static final String CUBE_USER_USER_PROPERTIES = "#_USER_USER_PROPERTIES"; //$NON-NLS-1$

    IElement element;
    IDatabase sysDB;

    public PaloUser(IElement element, IDatabase sysDB) {
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

    public boolean isMemberOfGroup(String group)
    {
	ICube userGroupCube = sysDB.getCubeByName(CUBE_USER_GROUP_ROLE);
        if (userGroupCube == null)
            return false;

		String[] coords = new String[2];

		coords[0] = getName();
        coords[1] = group;

    	String val = (String) PaloLibUtil.getData(userGroupCube, coords);

        return val.equals("1");  //$NON-NLS-1$
    }

    public void setGroup(String group, String value)
    {
	ICube groupRoleCube = sysDB.getCubeByName(CUBE_USER_GROUP_ROLE);
        if (groupRoleCube == null)
            return;

		String[] coords = new String[2];

		coords[0] = getName();
        coords[1] = group;
        PaloLibUtil.setData(groupRoleCube, coords, value);

        return;
    }

    public String getPassword() {
	ICube groupRoleCube = sysDB.getCubeByName(CUBE_USER_USER_PROPERTIES);
        if (groupRoleCube == null)
            return "";

        String[] coords = new String[2];

		coords[0] = getName();
        coords[1] = new String("password");

    	Object value = PaloLibUtil.getData(groupRoleCube, coords);

        return value.toString();
    }

    public void setPassword(String newPassword) {
	ICube groupRoleCube = sysDB.getCubeByName(CUBE_USER_USER_PROPERTIES);
        if (groupRoleCube == null)
            return;

		String[] coords = new String[2];

		coords[0] = getName();
        coords[1] = new String("password");
        PaloLibUtil.setData(groupRoleCube, coords, newPassword);


    }

}

