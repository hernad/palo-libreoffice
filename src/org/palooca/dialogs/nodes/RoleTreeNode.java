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

package org.palooca.dialogs.nodes;

import com.jedox.palojlib.interfaces.IDimension;
import javax.swing.tree.DefaultMutableTreeNode;
import org.palooca.paloutil.PaloRole;

/**
 *
 * @author MichaelRaue
 */
public class RoleTreeNode  extends DefaultMutableTreeNode {
    private PaloRole role;
    private IDimension dimension;

    public PaloRole getRole() {
        return role;
    }

    public RoleTreeNode(PaloRole role, IDimension dimension) {
        this.dimension = dimension;
        this.role = role;
    }

    public IDimension getDimension() {
        return dimension;
    }

    @Override
    public String toString() {
        if (role != null)
            return role.getName();
        else
            return "No Role Set";
    }

}
