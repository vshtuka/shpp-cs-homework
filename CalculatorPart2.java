package com.shpp.p2p.cs.vshtuka.assignment11;

import java.util.*;

/**
 * CalculatorPart2 class consists console program which by the arguments of the method main,
 * takes the first parameter as a mathematical expression, and the remaining parameters as variables
 * of a mathematical expression. Expression is solved using the shunting-yard algorithm.
 * Supports operators + - / * ^, functions sin, cos, tan, atan, log10, log2, sqrt,
 * brackets, floating point numbers and negative numbers
 */
public class CalculatorPart2 {

    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                throw new IllegalArgumentException("No arguments");
            }
            /* Remove all spaces of arguments and replaces commas with dots */
            for (int i = 0; i < args.length; i++) {
                args[i] = args[i].replace(" ", "").replace(",", ".");
            }
            String formula = args[0];
            if (formula.length() == 0) {
                throw new IllegalArgumentException("Formula is empty");
            }
            ArrayList<String> variables = new ArrayList<>(Arrays.asList(args).subList(1, args.length));

            /* check if the formula contains invalid characters */
            for (int i = 0; i < formula.length(); i++) {
                if (!isValidExpressionChar(formula.charAt(i))) {
                    throw new IllegalArgumentException("Formula contains an invalid character");
                }
            }

            HashMap<String, String> variablesMap = parseVariables(variables);
            ArrayList<String> formulaTokens = parseFormula(formula);
            transformFormulaTokens(formulaTokens, variablesMap);
            HashMap<String, Integer> mathOperations = setOperationsPriority();
            double result = calculate(formulaTokens, mathOperations);
            if (variables.size() > 0) {
                System.out.println("Result of mathematical expression " + formula + " with variables " +
                        variables + " is " + result);
            } else {
                System.out.println("Result of mathematical expression " + formula + " is " + result);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Parses variables that come through the main method into tokens
     * (name and value) and puts the data in a hash map
     *
     * @param variables is arguments that come through the main method
     */
    private static HashMap<String, String> parseVariables(ArrayList<String> variables) {
        HashMap<String, String> variablesMap = new HashMap<>();
        for (String variable : variables) {
            try {
                /* splits string into two parts (name and value) */
                StringTokenizer stringTokenizer = new StringTokenizer(variable, "=");
                String variableName = stringTokenizer.nextToken();
                String variableValue = stringTokenizer.nextToken();
                /* if both parts are correct, adds a variable to hash map */
                if (isVariable(variableName) && isNumber(variableValue)) {
                    variablesMap.put(variableName, variableValue);
                } else {
                    throw new IllegalArgumentException("Invalid variable");
                }
            } catch (NoSuchElementException e) {
                throw new NoSuchElementException("Invalid variable");
            }
        }
        return variablesMap;
    }

    /**
     * Parses mathematical expression that come through the main method into tokens
     * (into operators and operands)
     *
     * @param formula is the first argument that come through the main method
     */
    private static ArrayList<String> parseFormula(String formula) {
        ArrayList<String> tokens = new ArrayList<>();
        StringTokenizer stringTokenizer = new StringTokenizer(formula, "+-/*^()", true);
        while (stringTokenizer.hasMoreTokens()) {
            tokens.add(stringTokenizer.nextToken());
        }
        return tokens;
    }

    /**
     * Changes all function brackets of an expression ("(" to a symbol "[" and ")" to a symbol "]",
     * all unary minuses of an expression to a symbol "#" and substitutes variables into expression
     */
    private static void transformFormulaTokens(ArrayList<String> tokens, HashMap<String, String> variablesMap) {
        try {
            String brackets = ""; //variable for storing copies of open brackets
            for (int i = 0; i < tokens.size(); i++) {
                String currentToken = tokens.get(i);
                String previousToken = i > 0 ? tokens.get(i - 1) : "";
                String nextToken = i < tokens.size() - 1 ? tokens.get(i + 1) : "";
                /* changes all function brackets of an expression ("(" to a symbol "[" and ")" to a symbol "]") */
                if (isFunction(currentToken) && nextToken.equals("(")) {
                    tokens.set(i + 1, "[");
                    brackets = "[" + brackets;
                } else if (currentToken.equals("(")) {
                    brackets = "(" + brackets;
                } else if (currentToken.equals(")") && brackets.charAt(0) == '(') {
                    brackets = brackets.replaceFirst("\\(", "");
                } else if (currentToken.equals(")") && brackets.charAt(0) == '[') {
                    tokens.set(i, "]");
                    brackets = brackets.replaceFirst("\\[", "");
                }
                /* changes all unary minuses of an expression to a symbol "#" */
                else if (currentToken.equals("-") && (i == 0 || isOperator(previousToken))) {
                    tokens.set(i, "#");
                } else if (currentToken.equals("-") && (previousToken.equals("(") || previousToken.equals("["))) {
                    tokens.set(i, "#");
                }
                /* substitutes variables into expression */
                else if (isVariable(currentToken) && !isFunction(currentToken)) {
                    String value = variablesMap.get(currentToken);
                    if (value != null) {
                        tokens.set(i, value);
                    } else {
                        throw new NoSuchElementException("There is no needed variable in the arguments");
                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException("Misplaced brackets");
        }
    }

    /**
     * Calculates a mathematical expression using the shunting-yard algorithm,
     * which uses 2 lines to convert to Reverse Polish notation: input and output,
     * and a stack to store operators that have not yet been added to the output queue.
     * During the conversion, the algorithm reads 1 token and performs actions depending on the given token.
     */
    private static double calculate(ArrayList<String> formulaTokens, HashMap<String, Integer> mathOperations) throws Exception {
        Stack<Double> outputLine = new Stack<>();
        Stack<String> operationStack = new Stack<>();
        try {
            /* go through each token and performs suitable operations */
            int index = 0;
            while (index < formulaTokens.size()) {
                String currentToken = formulaTokens.get(index);
                String previousToken = index > 0 ? formulaTokens.get(index - 1) : "";
                if (isNumber(currentToken) && previousToken.equals("#")) {
                    outputLine.push(0 - (Double.parseDouble(currentToken)));
                    operationStack.pop();
                } else if (isNumber(currentToken)) {
                    outputLine.push(Double.parseDouble(currentToken));
                } else if (currentToken.equals("]")) {
                    if (operationStack.peek().equals("[")) {
                        operationStack.pop();
                        outputLine.push(makeFunctionOperation(outputLine.pop(), operationStack.pop()));
                    } else {
                        outputLine.push(makeOperation(outputLine.pop(), outputLine.pop(), operationStack.pop()));
                        index -= 1; //checks the same token
                    }
                } else if (isOperator(currentToken) && !operationStack.isEmpty() &&
                        mathOperations.get(currentToken) <= mathOperations.get(operationStack.peek())) {
                    outputLine.push(makeOperation(outputLine.pop(), outputLine.pop(), operationStack.pop()));
                    index -= 1; //checks the same token
                } else if (currentToken.equals(")")) {
                    if (operationStack.peek().equals("(")) {
                        operationStack.pop();
                    } else {
                        outputLine.add(makeOperation(outputLine.pop(), outputLine.pop(), operationStack.pop()));
                        index -= 1; //checks the same token
                    }
                } else {
                    operationStack.push(currentToken);
                }
                index++;
            }
            /* pops the remaining operations from the stack */
            while (operationStack.size() != 0) {
                outputLine.add(makeOperation(outputLine.pop(), outputLine.pop(), operationStack.pop()));
            }
            return outputLine.get(0);
        } catch (ArithmeticException e) {
            throw new ArithmeticException("Division by zero");
        } catch (Exception e) {
            throw new Exception("Misplaced token");
        }
    }

    /**
     * Takes the last number from the output line and performs the current operation on them
     */
    private static Double makeFunctionOperation(Double number, String function) {
        return function.equals("sin") ? Math.sin(number) :
                function.equals("cos") ? Math.cos(number) :
                        function.equals("tan") ? Math.tan(number) :
                                function.equals("atan") ? Math.atan(number) :
                                        function.equals("log10") ? Math.log10(number) :
                                                function.equals("log2") ? Math.log(number) / Math.log(2.0) :
                                                        Math.sqrt(number);
    }

    /**
     * Takes the last two numbers from the output line and performs the current operation on them
     */
    private static Double makeOperation(Double num1, Double num2, String operation) {
        if (operation.equals("/") && num1 == 0) {
            throw new ArithmeticException(); //division by zero
        }
        return operation.equals("+") ? num1 + num2 :
                operation.equals("-") ? num2 - num1 :
                        operation.equals("*") ? num1 * num2 :
                                operation.equals("/") ? num2 / num1 : Math.pow(num2, num1);
    }

    /**
     * Checks if a string is a function
     */
    private static boolean isFunction(String token) {
        return token.equals("sin") || token.equals("cos") || token.equals("tan") || token.equals("atan") ||
                token.equals("log10") || token.equals("log2") || token.equals("sqrt");
    }

    /**
     * Checks if a string is an operator
     */
    private static boolean isOperator(String token) {
        char operator = token.charAt(0);
        return "+-*/^".indexOf(operator) != -1;
    }

    /**
     * Checks if a string is a number
     */
    private static boolean isNumber(String token) {
        try {
            Double.parseDouble(token);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Checks the name of the variable for correctness (must begin with a letter and contain only letters, numbers or '_')
     *
     * @param variableName is string name
     */
    private static boolean isVariable(String variableName) {
        if (!Character.isLetter(variableName.charAt(0))) {
            return false;
        }
        for (int i = 1; i < variableName.length(); i++) {
            char ch = variableName.charAt(i);
            if (!Character.isLetter(ch) && !Character.isDigit(ch) && ch != '_') {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if formula can contains a character
     */
    private static boolean isValidExpressionChar(char ch) {
        return Character.isDigit(ch) || Character.isLetter(ch) || "+-*/^._()".indexOf(ch) != -1;
    }

    /**
     * Prioritizes mathematical operations, zero is the lowest priority
     */
    private static HashMap<String, Integer> setOperationsPriority() {
        HashMap<String, Integer> mathOperations = new HashMap<>();
        mathOperations.put("(", 0);
        mathOperations.put("[", 0); //function bracket
        mathOperations.put("sin", 0);
        mathOperations.put("cos", 0);
        mathOperations.put("tan", 0);
        mathOperations.put("atan", 0);
        mathOperations.put("log10", 0);
        mathOperations.put("log2", 0);
        mathOperations.put("sqrt", 0);
        mathOperations.put("+", 1);
        mathOperations.put("-", 1);
        mathOperations.put("*", 2);
        mathOperations.put("/", 2);
        mathOperations.put("^", 3);
        mathOperations.put("#", 4); //unary minus
        return mathOperations;
    }
}