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

package org.palooca.dialogs.controls;

import com.sun.star.awt.XActionListener;
import com.sun.star.awt.XButton;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.palooca.dialogs.BaseDialog;

/**
 *
 * @author Andreas Schneider
 */
public class Button extends LabeledControl {
    
    private String name;
    private XButton xButton;
    
    public Button(BaseDialog owner, String name) {
        super(owner.context);
        try {
            setUnoModel(owner.getMultiServiceFactory().createInstance("com.sun.star.awt.UnoControlButtonModel"));
            this.name = name;
            setProperty("Name", name);
        } catch (Exception ex) {
            Logger.getLogger(Button.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setParentControl(BaseControl parentControl) {
        super.setParentControl(parentControl);
        xButton = (XButton) UnoRuntime.queryInterface(XButton.class, unoControl);
    }
    
    public void addActionListener(XActionListener actionListener) {
        xButton.addActionListener(actionListener);
    }
    
    public void setActionCommand(String actionCommand) {
        xButton.setActionCommand(actionCommand);
    }

}
