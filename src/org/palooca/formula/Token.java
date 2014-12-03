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

import com.sun.star.document.XActionLockable;
import com.sun.star.sheet.XCellRangeData;
import com.sun.star.sheet.XFunctionAccess;
import com.sun.star.sheet.XSpreadsheet;
import com.sun.star.sheet.XSpreadsheets;
import com.sun.star.table.XCell;
import com.sun.star.uno.UnoRuntime;
import java.util.ArrayList;

/**
 *
 * @author Andreas Schneider
 */
public abstract class Token extends ArrayList<Token> {
    
    protected String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Token(String content) {
        this.content = content;
    }

    public String getFormula(boolean first) {
        StringBuilder result = new StringBuilder();
        
        if (first)
            result.append('=');
        
        for (int i = 0; i < size(); i++) {
            result.append(get(i).getFormula(false));
        }
        
        return result.toString();
    }
    
    public Object calculate(XCell tempCell) {
        Object result = null;
        
        XActionLockable lock = (XActionLockable) UnoRuntime.queryInterface(XActionLockable.class, tempCell);
        if (lock != null)
            lock.addActionLock();
        
        String originalContent = tempCell.getFormula();
        tempCell.setFormula(this.getFormula(true));
        XCellRangeData rangeData = (XCellRangeData) UnoRuntime.queryInterface(XCellRangeData.class, tempCell);
        result = rangeData.getDataArray()[0][0];
        tempCell.setFormula(originalContent);
        
        if (lock != null)
            lock.removeActionLock();
        
        return result;
    }
    
    public Double calculateDouble(XCell tempCell) {
        Double result = null;
        try {
            Object o = calculate(tempCell);
            if (o instanceof Double)
                result = (Double)o;
            else
                result = Double.parseDouble(o.toString());
        } catch (NumberFormatException e) {}
        return result;
    }
    
    public String calculateString(XCell tempCell) {
        Object o = calculate(tempCell);
        if (o instanceof Double)
            return Integer.toString(((Double)o).intValue());
        else
            return o.toString();
    }
    
    // calculation
    
    private class Operator {
        public char operatorType;
        public ArrayList leftList = new ArrayList();
        public ArrayList rightList = new ArrayList();
        
        public Operator(char operatorType) {
            this.operatorType = operatorType;
        }
        
        public Object calculate() {
            Object left = leftList.get(0);
            Object right = rightList.get(0);
            
            Object leftValue;
            if (left instanceof Operator)
                leftValue = ((Operator)left).calculate();
            else
                leftValue = ((Operand)left).value;
            
            
            Object rightValue;
            if (right instanceof Operator)
                rightValue = ((Operator)right).calculate();
            else
                rightValue = ((Operand)right).value;
            
            if (mathOperators.indexOf(operatorType) > -1) {
                Double leftDouble = null;
                Double rightDouble = null;
                
                try {
                    if (leftValue instanceof Double) {
                        leftDouble = (Double)leftValue;
                    } else if (leftValue instanceof String) {
                        leftDouble = Double.parseDouble((String)leftValue);
                    }
                    
                    if (rightValue instanceof Double) {
                        rightDouble = (Double)rightValue;
                    } else if (rightValue instanceof String) {
                        rightDouble = Double.parseDouble((String)rightValue);
                    }
                } catch (NumberFormatException e) {}
                
                if (leftDouble != null && rightDouble != null) {
                    switch (operatorType) {
                        case '+': return new Double(leftDouble.doubleValue() + rightDouble.doubleValue());
                        case '-': return new Double(leftDouble.doubleValue() - rightDouble.doubleValue());
                        case '*': return new Double(leftDouble.doubleValue() * rightDouble.doubleValue());
                        case '/': return new Double(leftDouble.doubleValue() / rightDouble.doubleValue());
                    }
                }
            } else if (operatorType == '&') {
                String leftString;
                if (leftValue instanceof Double) {
                    double d = ((Double)leftValue).doubleValue();
                    if ((double)(int)d == d)
                        leftString = Integer.toString((int)d);
                    else
                        leftString = leftValue.toString();
                } else
                    leftString = leftValue.toString();
                
                String rightString;
                if (rightValue instanceof Double) {
                    double d = ((Double)rightValue).doubleValue();
                    if ((double)(int)d == d)
                        rightString = Integer.toString((int)d);
                    else
                        rightString = rightValue.toString();
                } else
                    rightString = rightValue.toString();
                
                return leftString + rightString;
            }
            
            return null;
        }
    }
    
    private class Operand {
        public Object value;
        
        public Operand(Object value) {
            this.value = value;
        }
    }
    
    private final static String[] operatorTypes = new String[]{ "&", "+-", "*/" };
    private final static String mathOperators = "+-*/";
    
    private ArrayList processList(ArrayList list) {
        if (list.size() == 1)
            return list;
        
        for (int i = 0; i < operatorTypes.length; i++) {
            for (int j = 1; j < list.size(); j += 2) {
                Operator operator = (Operator)list.get(j);
                if (operatorTypes[i].indexOf(operator.operatorType) > -1) {
                    for (int k = 0; k < j; k++) {
                        operator.leftList.add(list.get(0));
                        list.remove(0);
                    }
                    while (1 < list.size()) {
                        operator.rightList.add(list.get(1));
                        list.remove(1);
                    }
                    
                    if (operator.operatorType == '-') {
                        for (int k = 1; k < operator.rightList.size(); k += 2) {
                            Operator childOperator = (Operator) operator.rightList.get(k);
                            if (childOperator.operatorType == '+')
                                childOperator.operatorType = '-';
                            else if (childOperator.operatorType == '-')
                                childOperator.operatorType = '+';
                        }
                    }
                    
                    /*StringBuilder out = new StringBuilder();
                    for (int k = 0; k < operator.leftList.size(); k++) {
                        Object token = operator.leftList.get(k);
                        if (token instanceof Operand)
                            out.append(((Operand)token).value + "; ");
                        else
                            out.append(((Operator)token).operatorType + "; ");
                    }
                    out.append("#" + ((Operator)list.get(0)).operatorType + "#; ");
                    for (int k = 0; k < operator.rightList.size(); k++) {
                        Object token = operator.rightList.get(k);
                        if (token instanceof Operand)
                            out.append(((Operand)token).value + "; ");
                        else
                            out.append(((Operator)token).operatorType + "; ");
                    }
                    
                    System.out.println(out.toString());*/
                    
                    if ((operator.leftList = processList(operator.leftList)) == null)
                        return null;
                    
                    if ((operator.rightList = processList(operator.rightList)) == null)
                        return null;
                    
                    return list;
                }
            }
        }
        
        return null;
    }
    
    public Object calculate(XFunctionAccess functionAccess, XSpreadsheets spreadsheets, XSpreadsheet activeSheet) {
        if (size() > 1) {
            ArrayList tokenList = new ArrayList();
            for (int i = 0; i < size(); i++) {
                Token token = get(i);
                if (token instanceof OperatorToken) {
                    tokenList.add(new Operator(token.getContent().charAt(0)));
                } else if (!(token instanceof NoopToken)) {
                    Object value = token.calculate(functionAccess, spreadsheets, activeSheet);
                    if (value instanceof Double || value instanceof String)
                        tokenList.add(new Operand(value));
                    else
                        return null; //Invalid calculation
                }
            }
            
            //validate list and combine infix operators with operands
            for (int i = 0; i < tokenList.size(); i++) {
                Object token = tokenList.get(i);
                if (i % 2 == 0) {
                    if (token instanceof Operator) {
                        Operator operator = (Operator)token;
                        Object nextToken = (i+1 < tokenList.size()) ? tokenList.get(i+1) : null;
                        if (operatorTypes[1].indexOf(operator.operatorType) > -1) {
                            if (operator.operatorType == '-') {
                                if (nextToken instanceof Operator) {
                                    Operator nextOperator = (Operator)nextToken;
                                    if (operatorTypes[1].indexOf(nextOperator.operatorType) > -1) {
                                        if (nextOperator.operatorType == '-') {
                                            tokenList.remove(i+1);
                                        } else {
                                            nextOperator.operatorType = '-';
                                        }
                                    } else {
                                        return null; //Invalid
                                    }
                                } else if (nextToken instanceof Operand) {
                                    Operand nextOperand = (Operand)nextToken;
                                    if (nextOperand.value instanceof Double) {
                                        Double nextDouble = (Double) nextOperand.value;
                                        nextOperand.value = new Double(nextDouble.doubleValue() * -1);
                                    } else {
                                        return null; //Invalid
                                    }
                                } else {
                                    return null; //Invalid
                                }
                            }
                        } else {
                            return null; //Invalid
                        }
                        
                        tokenList.remove(i);
                        i--; //check the token on position i again
                    }
                } else {
                    if (!(token instanceof Operator))
                        return null; //Invalid
                }
            }
            
            if (tokenList.size() % 2 == 0) {
                return null; //a valid calculation would have an odd amount of tokens
            }
            
            tokenList = processList(tokenList);
            if (tokenList != null) {
                Object token = tokenList.get(0);
                if (token instanceof Operator)
                    return ((Operator)token).calculate();
                else if (token instanceof Operand)
                    return ((Operand)token).value;
            }
            
        } else if (size() == 1) {
            return get(0).calculate(functionAccess, spreadsheets, activeSheet);
        }
        
        return null;
    }
    
}
