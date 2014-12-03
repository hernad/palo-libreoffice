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

import com.sun.star.sheet.XFunctionAccess;
import com.sun.star.sheet.XSpreadsheet;
import com.sun.star.sheet.XSpreadsheets;

/**
 *
 * @author Andreas Schneider
 */
public class ArgumentToken extends Token {

    protected FunctionToken functionToken;
    
    public ArgumentToken(FunctionToken functionToken) {
        super(";");
        this.setFunctionToken(functionToken);
    }
    
    public ArgumentToken(FunctionToken functionToken, Token subExpression) {
        this(functionToken);
        this.add(subExpression);
    }

    public FunctionToken getFunctionToken() {
        return functionToken;
    }

    public void setFunctionToken(FunctionToken functionToken) {
        this.functionToken = functionToken;
    }
    
    public String getFormula(boolean first) {
        if (size() == 1)
            return get(0).getFormula(first);
        else
            return super.getFormula(first);
    }

}
