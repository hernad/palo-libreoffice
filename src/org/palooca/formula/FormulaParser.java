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

import java.util.ArrayList;
import java.util.Stack;

/**
 *
 * @author Andreas Schneider
 */
public class FormulaParser {

    //public static String OPERATORS_SN = "+-";
    public static final String OPERATORS = "+-*/^&=<>"; //0 to 4 are math operators, 5 is the concat operator, 6 to 8 are logical operators
    public static final String[] BOOLOPERATORS = new String[]{ ">=", "<=", "<>" };
    
    protected enum ParserState {
        GetChar,
        GetString
    }
    
    public static int arrayIndexOf(String[] stringArray, String string) {
        int result = -1;
        int i = 0;
        while (i < stringArray.length && result == -1) {
            if (stringArray[i].compareTo(string) == 0)
                result = i;
            i++;
        }
        return result;
    }
    
    public static Token parseFormula(String formula) {
        ParserState state = ParserState.GetChar;
        
        Token root = new NoopToken("=");
        ArrayList<Token> parentToken = root;
        Stack<Token> stack = new Stack<Token>();
        
        if (formula.length() >= 2 && formula.charAt(0) == '=') {
            String value = "";
            int i = 1;
            while ( i < formula.length() ) {
                char c = formula.charAt(i);
                switch ( state ) {
                    case GetChar:
                        if (c == '"') {
                            state = ParserState.GetString;
                            i++;
                            break;
                        }
                        
                        if (c == ' ') {
                            if (value.length() > 0) {
                                parentToken.add(new OperandToken(value));
                                value = "";
                            }
                            parentToken.add(new WhitespaceToken());
                            i++;
                            while (i < formula.length() && formula.charAt(i) == ' ') {
                                i++;
                            }
                            break;
                        }
                        
                        if (i + 1 < formula.length() && arrayIndexOf(BOOLOPERATORS, formula.substring(i, i+1)) > -1) {
                            if (value.length() > 0) {
                                parentToken.add(new OperandToken(value));
                                value = "";
                            }
                            parentToken.add(new OperatorToken(OperatorType.Logical, formula.substring(i, i+1)));
                            i += 2;
                            break;
                        }
                        
                        int operatorId;
                        if ( (operatorId = OPERATORS.indexOf(c)) > -1) {
                            if (value.length() > 0) {
                                parentToken.add(new OperandToken(value));
                                value = "";
                            }
                            OperatorType operatorType = OperatorType.Unknown;
                            if (operatorId >= 0 && operatorId <= 4) { //math operator
                                operatorType = OperatorType.Math;
                            } else if (operatorId == 5) { //concat operator
                                operatorType = OperatorType.Concat;
                            } else if (operatorId >= 6) { //logical operator
                                operatorType = OperatorType.Logical;
                            }
                            parentToken.add(new OperatorToken(operatorType, String.valueOf(c)));
                            i++;
                            break;
                        }
                        
                        if (c == '(') {
                            if (value.length() > 0) {
                                FunctionToken functionToken = new FunctionToken(value);
                                parentToken.add(stack.push(functionToken));
                                parentToken = functionToken;
                                parentToken.add(stack.push(new ArgumentToken(functionToken)));
                                value = "";
                            } else {
                                parentToken.add(stack.push(new SubexpressionToken()));
                            }
                            
                            parentToken = stack.peek();
                            i++;
                            break;
                        }
                        
                        if (c == ';') {
                            if (value.length() > 0) {
                                parentToken.add(new OperandToken(value));
                                value = "";
                            }
                            if (stack.peek() instanceof ArgumentToken) {
                                FunctionToken functionToken = ((ArgumentToken)stack.peek()).getFunctionToken();
                                parentToken = functionToken;
                                parentToken.add(stack.push(new ArgumentToken(functionToken)));
                                parentToken = stack.peek();
                            }
                            i++;
                            break;
                        }
                        
                        if (c == ')') {
                            if (value.length() > 0) {
                                parentToken.add(new OperandToken(value));
                                value = "";
                            }
                            
                            Token token = stack.pop();
                            if (token instanceof ArgumentToken) {
                                Token functionToken = stack.pop();
                                if (token.size() == 0) { //there were no arguments, so that token is useless
                                    functionToken.remove(token); //... therefore we remove it
                                }
                            }
                            
                            if (!stack.isEmpty()) {
                                parentToken = stack.peek();
                            } else {
                                parentToken = root;
                            }
                            i++;
                            break;
                        }
                        
                        value += c;
                        i++;
                        break;
                    case GetString:
                        if (c == '"') {
                            if (i + 1 < formula.length() && formula.charAt(i + 1) == '"') {
                                value += c;
                                i++;
                            } else {
                                state = ParserState.GetChar;
                                parentToken.add(new OperandToken(OperandType.Text, value));
                                value = "";
                            }
                        } else {
                            value += c;
                        }
                        i++;
                        break;
                };
            }
        }
        return root;
    }
    
    public static void printToken(Token token, int depth) {
        String indent = new String();
        for (int i = 0; i < depth; i++)
            indent = indent + " ";
        
        if (token instanceof OperandToken) {
            OperandToken operand = (OperandToken)token;
            System.out.println(indent + token.getClass() + " (" + operand.getType() + "): " + token.getContent());
        } else {
            System.out.println(indent + token.getClass() + ": " + token.getContent());
        }
        
        for (int i = 0; i < token.size(); i++)
            printToken(token.get(i), depth + 1);
    }

}
