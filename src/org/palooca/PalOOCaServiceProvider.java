/*
 * PalOOCaServiceProvider.java
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
 * Created on 2. September 2007, 19:49
 *
 */

package org.palooca;

import com.sun.star.comp.loader.FactoryHelper;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.lang.XSingleServiceFactory;
import com.sun.star.registry.XRegistryKey;

/**
 *
 * @author Andreas Schneider
 */
public class PalOOCaServiceProvider {
   
    public static XSingleComponentFactory __getComponentFactory(String implementationName) {
        if (implementationName.equals(PalOOCaImpl.class.getName())) {
            return PalOOCaImpl.__getComponentFactory(implementationName);
        } else if (implementationName.equals(PalOOCaUI.class.getName())) {
            return PalOOCaUI.__getComponentFactory(implementationName);
        }
        
        return null;
    }
    
    public static boolean __writeRegistryServiceInfo( XRegistryKey registryKey ) {
        return PalOOCaImpl.__writeRegistryServiceInfo(registryKey)
            && PalOOCaUI.__writeRegistryServiceInfo(registryKey);
    }
    
}