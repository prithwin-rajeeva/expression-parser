package org.winbandhu.parser;

import java.util.*;


public class PrecedenceParser {
    private static final String LEFT_PARENTHESES = "(";
    private static final String RIGHT_PARENTHESES = ")";
    Map<String,Integer> operandPriority = getOperandTable();

    private Map data;

    private Map<String, Integer> getOperandTable() {
        Map<String,Integer> operands = new HashMap();
        operands.put("/",1);
        operands.put("*",2);
        operands.put("+",3);
        operands.put("-",3);
        operands.put("<",4);
        operands.put(">",4);
        operands.put("<>",4);
        operands.put("<=",4);
        operands.put("&",4);
        operands.put("|",4);
        return operands;
    }

    public Object parse(Map data,String expression) {
        this.data = data;
        List<String> tokenizedExpression = lexp(expression);
        List<String> postFixExpression = toRPN(tokenizedExpression);
        return evaluate(postFixExpression);
    }

    public List<String> toRPN(List<String> infix){
        Stack<String> operandStack = new Stack<>();
        List<String> rpn = new ArrayList<>();
        operandStack.push(LEFT_PARENTHESES);
        infix.add(RIGHT_PARENTHESES);
        int i = 0;
        while(!operandStack.isEmpty()) {
            String item = infix.get(i);
            if(item.equals(LEFT_PARENTHESES)) {
                operandStack.push(item);
            } else if(isAlphaNumeric(item)) {
                rpn.add(item);
            } else if(isOperator(item)) {
                while(!isSameOrHigherPrecedence(operandStack,item)) {
                    rpn.add(operandStack.pop());
                }
                operandStack.push(item);

            } else {
                while(true) {
                    String operand = operandStack.pop();
                    if(operand.equals(LEFT_PARENTHESES)) break;
                    rpn.add(operand);
                }
            }
            i++;
        }
        return rpn;
    }

    private boolean isSameOrHigherPrecedence(Stack<String> operandStack , String operator){

        if(operandStack.peek().equals(LEFT_PARENTHESES)){
            return true;
        }
        if(operandPriority.get(operandStack.peek()) >= operandPriority.get(operator)){
            return true;
        }
        return false;

    }

    private boolean isOperator(String item) {
        if(operandPriority.containsKey(item)) return true;
        return false;
    }

    private boolean isAlphaNumeric(String item) {
        return item.matches("[a-zA-Z0-9]+") || item.matches("\\d+.{1}\\d+");
    }

    /**
     * basic lexer implementation.
     * @param expression
     * @return
     */
    private List<String> lexp(String expression) {
        List<String> response = new ArrayList<>();
        Queue<Character> operandStack = new LinkedList<>();
        char[] expressionCharArray = expression.toCharArray();
        for (int i = 0; i < expressionCharArray.length; i++) {
            char c = expressionCharArray[i];
            if (c == ' ') {
                emptyStack(operandStack , response);
            } else if (isArithmaticOperator(c)) {
                emptyStack(operandStack , response);
                response.add(c + "");
            } else if (isGreaterThanOrLessThan(c)) {
                emptyStack(operandStack , response);
                char n = expressionCharArray[i+1];
                if(isCompoundOperatorComponent(n)) {
                    response.add(c+""+n);i++;
                }
                response.add(c+"");
            } else {
                operandStack.offer(c);
            }
        }
        emptyStack(operandStack , response);
        return response;
    }
    private void emptyStack(Queue<Character> queue , List<String> target) {
        StringBuilder sb = new StringBuilder();
        while(!queue.isEmpty()) {
            sb.append(queue.poll());
        }
        if(sb.toString().equals("")) return;
        target.add(sb.toString());
    }

    private boolean isGreaterThanOrLessThan(char c) {
        if(c == '<' || c == '>') return true;
        return false;
    }

    private boolean isArithmaticOperator(char c) {
        if(c == '+' || c == '-' || c == '/' || c == '*' || c == '(' || c == ')')
            return true;
        return false;
    }

    private boolean isCompoundOperatorComponent(char c) {
        if(c == '=' || c == '>') return true;
        return false;
    }

    public Object evaluate(List<String> postfix) {
        Stack<Object> evaluationStack = new Stack<>();
        for(String item : postfix) {
            if(!isOperatorTd(item)){
                evaluationStack.push(item);
            } else {
                Object result;
                Object b = evaluationStack.pop();
                Object a = evaluationStack.pop();
                a = transformIfLookup(a);
                b = transformIfLookup(b);
                result = applyOperator(a,b,item);
                evaluationStack.push(result);
            }
        }
        return evaluationStack.pop();
    }

    private Object transformIfLookup (Object target) {
        if(target == null) return target;
        if(!(target instanceof String)) return target;
        String sTarg= (String) target;
        if(target.equals("true") || target.equals("false")) return Boolean.parseBoolean(sTarg);
        if(sTarg.charAt(0)>= 48 && sTarg.charAt(0) <= 57) {
            return target;
        }
        return data.get(sTarg);
    }

    private boolean isOperatorTd(String op) {
        if(isArithmaticOperator(op.charAt(0)) || isLogicalOperator(op) || isComparisonOperator(op)) return true;
        return false;
    }

    private boolean isNumeric(Object item) {
        if(item instanceof java.lang.Double || item instanceof java.lang.Integer) return true;
        else if(item instanceof java.lang.String && (((String)item).matches("[0-9]+.*[0-9]*") ||
                ((String) item).matches("[0-9]+"))) return true;
        else return false;
    }

    private boolean isComparisonOperator(String op) {
        if(op.equals("<") || op.equals("<=") || op.equals(">") || op.equals(">=") || op.equals("=") || op.equals("<>"))
            return true;
        return false;
    }

    private Object applyOperator(Object a, Object b, String operator) {

        if(isArithmaticOperator(operator.charAt(0))) {
            if(!(isNumeric(a) && isNumeric(b))) throw new RuntimeException("something went wrong");
            double thiz = 0.0;
            double that = 0.0;
            thiz = processNumber(a);
            that = processNumber(b);
            double temp;
            switch (operator) {
                case "+" :
                    temp = thiz + that ; return temp;

                case "-" :
                    temp = thiz - that ; return temp;

                case "*" :
                    temp = thiz * that ; return temp;

                case "/" :
                    temp = thiz / that ; return temp;

            }
        }

        if(isComparisonOperator(operator)) {
            if(!(isNumeric(a) && isNumeric(b))) throw new RuntimeException("soemthing went wrong");
            double thiz = 0.0;
            double that = 0.0;
            thiz = processNumber(a);
            that = processNumber(b);

            switch (operator) {
                case "<" : return thiz < that;
                case "<=" : return thiz <= that;
                case ">" : return thiz > that;
                case ">=" : return thiz >= that;
                case "=" : return thiz==that;
                case "<>" : return thiz!=that;
            }
        }

        if(isLogicalOperator(operator)) {
            if(!(isBoolean(a) && isBoolean(b))) throw new RuntimeException("something went wrong");
            Boolean thiz;
            Boolean that;

            if(a instanceof String) {
                thiz = Boolean.parseBoolean((String)a);
            } else {
                thiz = (Boolean) a;
            }

            if(b instanceof String) {
                that = Boolean.parseBoolean((String)b);
            } else {
                that = (Boolean) b;
            }

            switch (operator) {
                case "|" :  return thiz || that;
                case "&" :  return thiz && that;
            }
        }
        return 0;
    }

    private double processNumber(Object something) {
        if(isNumeric(something)) {
            if(something instanceof String)
                return Double.parseDouble((String)something);
            else {
                if(something instanceof Double)
                    return (Double) something;
                if(something instanceof Integer)
                    return (Integer) something;
            }
        }
        return 0.0;
    }

    private boolean isLogicalOperator(String op) {
        if(op.equals("&")|| op.equals("|")) {
            return true;
        }
        return false;
    }

    private boolean isBoolean(Object item) {
        if(item instanceof Boolean) return true;
        else if(item.equals("true") || item.equals("false")) return true;
        else return false;
    }
}
