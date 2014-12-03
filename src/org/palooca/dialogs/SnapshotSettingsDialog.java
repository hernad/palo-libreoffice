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

import com.sun.star.awt.ActionEvent;
import com.sun.star.awt.ItemEvent;
import com.sun.star.awt.XActionListener;
import com.sun.star.awt.XItemListener;
import com.sun.star.lang.EventObject;
import com.sun.star.uno.XComponentContext;
import java.util.ResourceBundle;
import org.palooca.PalOOCaManager;
import org.palooca.dialogs.controls.Button;
import org.palooca.dialogs.controls.CheckBox;
import org.palooca.dialogs.controls.CheckedState;
import org.palooca.dialogs.controls.Label;

/**
 *
 * @author Andreas Schneider
 */
public class SnapshotSettingsDialog extends BaseDialog {

    private ResourceBundle resourceBundle;
    private Label lblDescription;
    public CheckBox cbRemoveFunctions;
    public CheckBox cbRemovePaloFunctionsOnly;
    public CheckBox cbDisableFormFields;
    private Button btnOK;
    private Button btnCancel;

    public SnapshotSettingsDialog(XComponentContext context) {
        super(context, "", -1, -1, 150, 140);
        
        this.resourceBundle = PalOOCaManager.getInstance(context).getResourceBundle("org/palooca/dialogs/PalOOCaDialogs");
//        this.setIconImage(new ImageIcon(SnapshotSettingsDialog.class.getResource("/images/modeler.png")).getImage());
        setProperty("Title", resourceBundle.getString("SnapshotSettingsDialog_Caption"));
        
        lblDescription = new Label(this, "lblDescription");
        lblDescription.setPosition(5, 5);
        lblDescription.setSize(140, 50);
        lblDescription.setMultiLine(true);
        lblDescription.setLabel(resourceBundle.getString("SnapshotSettings_Description"));
        lblDescription.setParentControl(this);
        
        CheckBoxSettingsListener checkBoxSettingsListener = new CheckBoxSettingsListener();
        
        cbRemoveFunctions = new CheckBox(this, "cbRemoveFunctions");
        cbRemoveFunctions.setPosition(5, 65);
        cbRemoveFunctions.setSize(140, 20);
        cbRemoveFunctions.setMultiLine(true);
        cbRemoveFunctions.setLabel(resourceBundle.getString("Remove_functions"));
        cbRemoveFunctions.setState(CheckedState.Checked);
        cbRemoveFunctions.setParentControl(this);
        cbRemoveFunctions.addItemListener(checkBoxSettingsListener);
        
        cbRemovePaloFunctionsOnly = new CheckBox(this, "cbRemovePaloFunctionsOnly");
        cbRemovePaloFunctionsOnly.setPosition(15, 85);
        cbRemovePaloFunctionsOnly.setSize(130, 10);
        cbRemovePaloFunctionsOnly.setLabel(resourceBundle.getString("Remove_only_PALO_functions"));
        cbRemovePaloFunctionsOnly.setParentControl(this);
        cbRemovePaloFunctionsOnly.addItemListener(checkBoxSettingsListener);
        
        cbDisableFormFields = new CheckBox(this, "cbDisableFormFields");
        cbDisableFormFields.setPosition(5, 100);
        cbDisableFormFields.setSize(140, 10);
        cbDisableFormFields.setLabel(resourceBundle.getString("Disable_form_fields"));
        cbDisableFormFields.setState(CheckedState.Checked);
        cbDisableFormFields.setParentControl(this);
        cbDisableFormFields.addItemListener(checkBoxSettingsListener);
        
        btnOK = new Button(this, "btnOK");
        btnOK.setPosition(5, 120);
        btnOK.setSize(25, 15);
        btnOK.setLabel(resourceBundle.getString("OK"));
        btnOK.setParentControl(this);
        btnOK.addActionListener(new OKButtonListener());
        
        btnCancel = new Button(this, "btnCancel");
        btnCancel.setPosition(110, 120);
        btnCancel.setSize(35, 15);
        btnCancel.setLabel(resourceBundle.getString("Cancel"));
        btnCancel.setParentControl(this);
        btnCancel.addActionListener(new CancelButtonListener());
    }
    
    private class CheckBoxSettingsListener implements XItemListener {

        public void itemStateChanged(ItemEvent itemEvent) {
            cbRemovePaloFunctionsOnly.setEnabled(cbRemoveFunctions.isChecked());
            btnOK.setEnabled(cbRemoveFunctions.isChecked() || 
                    cbDisableFormFields.isChecked());
        }

        public void disposing(EventObject eventObject) {
            
        }
        
    }
    
    private class OKButtonListener implements XActionListener {

        public void actionPerformed(ActionEvent actionEvent) {
            modalState = ModalState.OK;
            close();
        }

        public void disposing(EventObject eventObject) {
        }
        
    }
    
    private class CancelButtonListener implements XActionListener {

        public void actionPerformed(ActionEvent actionEvent) {
            modalState = ModalState.Exit;
            close();
        }

        public void disposing(EventObject eventObject) {
        }

    }
        
}
