/*
 * XMLHelper.java
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
 * Created on 29.08.2007, 21:15:44
 * 
 */

package org.palooca.config;

import org.dom4j.Element;


/**
 *
 * @author Andreas Schneider
 */
public class XMLHelper {
    
    public static void WriteString(Element element, String name, String value) {
        Element child = element.addElement(name);
        child.setText(value);
    }
    
    public static String ReadString(Element element, String name, String defaultValue) {
        Element child = element.element(name);
        if (child != null) {
            return child.getText();
        } else {
            return defaultValue;
        }
    }

}
