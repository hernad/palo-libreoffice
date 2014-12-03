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

package org.palooca.dialogs;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.palooca.network.ConnectionInfo;
import org.palooca.network.ConnectionState;

/**
 *
 * @author MichaelRaue
 */
public class DatabaseListCellRenderer implements ListCellRenderer {
    protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                    boolean cellHasFocus) {

        JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected,
                                                                cellHasFocus);

        if (value instanceof ConnectionInfo) {
            ConnectionInfo connInfo = (ConnectionInfo)value;
            renderer.setText(connInfo.getName());
            if (connInfo.getState() == ConnectionState.Connected)
                renderer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/server.png"))); // NOI18N
            else
                renderer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/serverDis.png"))); // NOI18N
        } else if (value instanceof DatabaseInfo) {
            renderer.setText(((DatabaseInfo)value).getDatabase().getName());
            renderer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/database_1.png"))); // NOI18N
        }

        return renderer;
    }
}
