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

import com.sun.star.awt.XControlContainer;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.XNameContainer;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andreas Schneider
 */
public abstract class BaseControl {
    
    protected XComponentContext context;
    private Object unoModel;
    protected Object unoControl;
    protected XPropertySet properties;
    protected BaseControl parentControl;
    
    public abstract String getName();
    
    public Object getUnoModel() {
        return unoModel;
    }
    
    /**
     * This is used <b>internally</b> to update the UnoModel and refresh the
     * associated PropertySet.
     * @param unoModel The new UnoModel for this control.
     */
    protected void setUnoModel(Object unoModel) {
        this.unoModel = unoModel;
        properties = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, unoModel);
    }
    
    public Object getUnoControl() {
        return unoControl;
    }
    
    public void setParentControl(BaseControl parentControl) {
        //TODO : remove from existing parentControl
        try {
            String name = getName();
            XNameContainer nameContainer = (XNameContainer) UnoRuntime.queryInterface(XNameContainer.class, parentControl.unoModel);
            nameContainer.insertByName(name, unoModel);
            
            XControlContainer controlContainer = (XControlContainer) UnoRuntime.queryInterface(XControlContainer.class, parentControl.unoControl);
            unoControl = controlContainer.getControl(name);
            
            this.parentControl = parentControl;
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(BaseControl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ElementExistException ex) {
            Logger.getLogger(BaseControl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (WrappedTargetException ex) {
            Logger.getLogger(BaseControl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public BaseControl(XComponentContext context) {
        this.context = context;
        unoModel = null;
        unoControl = null;
        parentControl = null;
    }
    
    protected void setProperty(String name, Object value) {
        try {
            properties.setPropertyValue(name, value);
        } catch (UnknownPropertyException ex) {
            Logger.getLogger(BaseControl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(BaseControl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(BaseControl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (WrappedTargetException ex) {
            Logger.getLogger(BaseControl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    protected Object getProperty(String name) {
        try {
            return properties.getPropertyValue(name);
        } catch (UnknownPropertyException ex) {
            Logger.getLogger(BaseControl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (WrappedTargetException ex) {
            Logger.getLogger(BaseControl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    // <editor-fold defaultstate="collapsed" desc="Uno Properties">
    
    public void setPosition(int x, int y) {
        setProperty("PositionX", new Integer(x));
        setProperty("PositionY", new Integer(y));
    }
    
    public void setSize(int width, int height) {
        setProperty("Width", new Integer(width));
        setProperty("Height", new Integer(height));
    }
    
    public void setEnabled(boolean enabled) {
        setProperty("Enabled", new Boolean(enabled));
    }
    
    // </editor-fold>

}
