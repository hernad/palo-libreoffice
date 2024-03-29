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

import com.sun.star.awt.XCheckBox;
import com.sun.star.awt.XItemListener;
import com.sun.star.uno.UnoRuntime;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.palooca.dialogs.BaseDialog;

/**
 *
 * @author Andreas Schneider
 */
public class CheckBox extends LabeledControl {

    private String name;
    private XCheckBox xCheckBox;

    public CheckBox(BaseDialog owner, String name) {
        super(owner.context);
        try {
            setUnoModel(owner.getMultiServiceFactory().createInstance("com.sun.star.awt.UnoControlCheckBoxModel"));
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
        xCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class, unoControl);
    }
    
    public void addItemListener(XItemListener itemListener) {
        xCheckBox.addItemListener(itemListener);
    }
    
    public void setState(CheckedState state) {
        setProperty("State", new Short(state.getUnoValue()));
    }
    
    public boolean isChecked() {
        return ((Short)getProperty("State") == CheckedState.Checked.getUnoValue());
    }
    
}
