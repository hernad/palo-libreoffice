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

import com.jedox.palojlib.interfaces.IDatabase;
import javax.swing.tree.DefaultMutableTreeNode;
import com.jedox.palojlib.interfaces.IDimension;

/**
 *
 * @author MichaelRaue
 */
public class AttributesTreeNode  extends DefaultMutableTreeNode {
    private IDimension dim;
    private IDatabase database;
    private boolean initialized;

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public IDimension getDimension() {
        return dim;
    }

    public void setDimension(IDimension dim) {
        this.dim = dim;
    }

    public AttributesTreeNode(IDimension dim, String name, IDatabase database) {
        super(name);
        this.dim = dim;
        this.database = database;
    }

    public IDatabase getDatabase() {
        return database;
    }
}
