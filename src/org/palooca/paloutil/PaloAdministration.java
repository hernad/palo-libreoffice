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

import com.jedox.palojlib.interfaces.IDatabase;
import com.jedox.palojlib.interfaces.IDimension;
import com.jedox.palojlib.interfaces.IElement;

/**
 *
 * @author MichaelRaue
 */
public class PaloAdministration {
	public static final String DIMENSION_GROUP = "#_GROUP_"; //$NON-NLS-1$
	public static final String DIMENSION_ROLE = "#_ROLE_"; //$NON-NLS-1$
	public static final String DIMENSION_USER = "#_USER_"; //$NON-NLS-1$
	public static final String DIMENSION_RIGHTS = "#_RIGHT_OBJECT_"; //$NON-NLS-1$

    private IDatabase    sysDB;

    public PaloAdministration(IDatabase sysDB) {
        this.sysDB = sysDB;
    }

    public PaloGroup[] getGroups() {
        IDimension dim = sysDB.getDimensionByName(DIMENSION_GROUP);
        if (dim == null)                    // maybe no rights to view this dimension
            return null;

        IElement[] groups = dim.getElements(false);
        if (groups == null || groups.length == 0)
            return null;

        PaloGroup[] paloGroups = new PaloGroup[groups.length];

        for (int i = 0; i < groups.length; i++) {
            paloGroups[i] = new PaloGroup(groups[i], sysDB);
        }
        
        return paloGroups;
    }

    public void removeGroup(PaloGroup group) {
	IDimension dim = sysDB.getDimensionByName(DIMENSION_GROUP);
        if (dim == null)                    // maybe no rights to view this dimension
            return;
        dim.removeElements(new IElement[]{group.getElement()});
    }

    public PaloUser[] getUsers() {
        IDimension dim = sysDB.getDimensionByName(DIMENSION_USER);
        if (dim == null)                    // maybe no rights to view this dimension
            return null;

	IElement[] users = dim.getElements(false);
        if (users == null || users.length == 0)
            return null;

        PaloUser[] paloUsers = new PaloUser[users.length];

        for (int i = 0; i < users.length; i++) {
            paloUsers[i] = new PaloUser(users[i], sysDB);
        }

        return paloUsers;
    }

    public PaloRole[] getRoles() {
        IDimension dim = sysDB.getDimensionByName(DIMENSION_ROLE);
        if (dim == null)                    // maybe no rights to view this dimension
            return null;
    
        IElement[] users = dim.getElements(false);
        if (users == null || users.length == 0)
            return null;

        PaloRole[] paloRoles = new PaloRole[users.length];

        for (int i = 0; i < users.length; i++) {
            paloRoles[i] = new PaloRole(users[i], sysDB);
        }

        return paloRoles;
    }

    public String[] getRights() {
        IDimension dim = sysDB.getDimensionByName(DIMENSION_RIGHTS);
        if (dim == null)                    // maybe no rights to view this dimension
            return null;
	IElement[] rights = dim.getElements(false);
        if (rights == null || rights.length == 0)
            return null;

        String[] paloRights = new String[rights.length];

        for (int i = 0; i < rights.length; i++) {
            paloRights[i] = rights[i].getName();
        }

        return paloRights;
    }

    public IDimension getDimensionByName(String name) {
        return sysDB.getDimensionByName(name);
    }

}
