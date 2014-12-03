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

import javax.swing.tree.DefaultMutableTreeNode;
import com.jedox.palojlib.interfaces.IDimension;

/**
 *
 * @author MichaelRaue
 */
public class DimensionRightsTreeNode  extends DefaultMutableTreeNode {
    private IDimension dimension;

    public IDimension getDimension() {
        return dimension;
    }

    public void setDimension(IDimension dimension) {
        this.dimension = dimension;
    }

    public DimensionRightsTreeNode(IDimension dimension, String title) {
        super(title);
        this.dimension = dimension;
    }
}
