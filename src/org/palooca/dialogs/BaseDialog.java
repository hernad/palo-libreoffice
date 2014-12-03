/*
 * BaseDialog.java
 *
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
 *
 * Created on 28. August 2008, 17:43
 *
 */

package org.palooca.dialogs;

import com.sun.star.awt.PosSize;
import com.sun.star.awt.Rectangle;
import org.palooca.dialogs.controls.BaseControl;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XControlModel;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XToolkit;
import com.sun.star.awt.XWindow;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.XNameContainer;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.palooca.PalOOCaManager;

/**
 * The BaseDialog represents the base for all dialogs used within the addon.
 * It automatically loads the necessary interfaces to access OpenOffice.org dialogs.
 * @author Andreas Schneider
 */
public abstract class BaseDialog extends BaseControl {
    
    private XMultiComponentFactory xMCF;
    private Object toolkit;
    private XMultiServiceFactory xMSF;
    protected XWindow xWindow;
    protected XDialog xDialog;
    protected ModalState modalState;
    
    @Override
    public String getName() {
        return null;
    }
    
    public XMultiServiceFactory getMultiServiceFactory() {
        return xMSF;
    }
    
    public BaseDialog(XComponentContext context, String title, int x, int y, int width, int height) {
        super(context);
        modalState = ModalState.Exit;
        try {
            xMCF = context.getServiceManager();
            setUnoModel(xMCF.createInstanceWithContext("com.sun.star.awt.UnoControlDialogModel", context));
            xMSF = (XMultiServiceFactory) UnoRuntime.queryInterface(XMultiServiceFactory.class, getUnoModel());
            
            setProperty("Title", title);
            setPosition(x, y);
            setSize(width, height);

            unoControl = xMCF.createInstanceWithContext("com.sun.star.awt.UnoControlDialog", context);
            XControl xControl = (XControl)UnoRuntime.queryInterface(XControl.class, unoControl);
            XControlModel xControlModel = (XControlModel)UnoRuntime.queryInterface(XControlModel.class, getUnoModel());
            xControl.setModel(xControlModel);
            
            toolkit = xMCF.createInstanceWithContext("com.sun.star.awt.Toolkit", context);
            XToolkit xToolkit = (XToolkit) UnoRuntime.queryInterface(XToolkit.class, toolkit);
            xWindow = (XWindow) UnoRuntime.queryInterface(XWindow.class, unoControl);
            xWindow.setVisible(false);
            xControl.createPeer(xToolkit, null);
            
            xDialog = (XDialog) UnoRuntime.queryInterface(XDialog.class, unoControl);
            
            //center if necessary
            if (x < 0 || y < 0) {
                Rectangle workspacePosSize = PalOOCaManager.getInstance(context).getWorkspaceDimensions();
                Rectangle dialogPosSize = xWindow.getPosSize();
                if (x < 0)
                    dialogPosSize.X = workspacePosSize.X + (workspacePosSize.Width / 2) - (dialogPosSize.Width / 2);
                if (y < 0)
                    dialogPosSize.Y = workspacePosSize.Y + (workspacePosSize.Height / 2) - (dialogPosSize.Height / 2);
                
                xWindow.setPosSize(dialogPosSize.X, dialogPosSize.Y,
                        dialogPosSize.Width, dialogPosSize.Height, PosSize.POS);
            }
            
        } catch (Exception ex) {
            Logger.getLogger(BaseDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        XComponent xComponent = (XComponent) UnoRuntime.queryInterface(XComponent.class, unoControl);
        xComponent.dispose();
        super.finalize();
    }
    
    public ModalState showModal() {
        xWindow.setVisible(true);
        xDialog.execute();
        return modalState;
    }
    
    public void close() {
        xDialog.endExecute();
        xWindow.setVisible(false);
    }
       
}