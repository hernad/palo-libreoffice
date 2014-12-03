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

package org.palooca.formula;

import com.sun.star.container.NoSuchElementException;
import com.sun.star.sheet.XFunctionAccess;
import com.sun.star.sheet.XSpreadsheet;
import com.sun.star.sheet.XSpreadsheets;
import java.util.Iterator;

/**
 *
 * @author Andreas Schneider
 */
public class FunctionToken extends Token {

    public FunctionToken(String content) {
        super(content);
    }
    
    public String getFormula(boolean first) {
        StringBuilder result = new StringBuilder();
        
        if (first)
            result.append('=');
        
        result.append(content);
        result.append('(');
        for (int i = 0; i < size(); i++) {
            if (i > 0) {
                result.append(';');
            }
            result.append(get(i).getFormula(false));
        }
        result.append(')');
        
        return result.toString();
    }
    
    public Object calculate(XFunctionAccess functionAccess, XSpreadsheets spreadsheets, XSpreadsheet activeSheet) {
        try {
            Object[] args = new Object[size()];
            for (int i = 0; i < size(); i++)
                args[i] = get(i).calculate(functionAccess, spreadsheets, activeSheet);

            return functionAccess.callFunction(content, args);
        } catch (NoSuchElementException ex) {
            ex.printStackTrace();
        } catch (com.sun.star.lang.IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        
        return null;
    }

}
