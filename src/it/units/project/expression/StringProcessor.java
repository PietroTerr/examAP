package it.units.project.expression;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.regex.Pattern;

import static it.units.project.expression.Parser.processExpression;

public class StringProcessor implements Function<String, String> {
    private final List<Long> responseTimes = new CopyOnWriteArrayList<>();
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("[a-z]([0-9]?)");

    @Override
    public String apply(String command) {
        long startTime = System.currentTimeMillis();
        String[] parts = command.split("_"); // Split the command into parts
        String operation = parts[0];  // "MAX", "MIN", "AVG", "COUNT", "STAT", "BYE"
        String result = "";

        try {
            if (operation.equals("STAT")) {
                result = handleStatCommand(parts);
            } else if (operation.equals("BYE")) {
                return null;  // Indicates the server to close the connection
            } else {
                result = handleComputeCommand(operation, parts);
            }
        } catch (IllegalArgumentException e) {
            result = e.getMessage();
        } catch (Exception e) {
            result = "ERR; Unexpected error: " + e.getMessage();
        }

        long finishTime = System.currentTimeMillis();
        double elapsedTime = (double) (finishTime - startTime) / 1000.0;
        responseTimes.add((long) elapsedTime);
        if (!result.startsWith("ERR;")) {
            result = String.format("OK;%.3f;%.6f%n", elapsedTime, Double.parseDouble(result));
        }
        return result;
    }

    private String handleStatCommand(String[] parts) {
        if (responseTimes.isEmpty()) {
            return "ERR; Invalid request, no computation done yet";
        }
        try {
            if (parts.length < 2) {
                System.err.println("Client made a 'Syntax Error', command not correct");
                return "ERR; Syntax Error, missing argument";
            }
            String result;
            String commandType = parts[1];
            if (commandType.equals("REQS") && parts.length == 2) {
                result = "OK;" + responseTimes.size();
            } else if (commandType.equals("AVG") && parts.length == 3 && parts[2].equals("TIME")) {
                double sum = 0.0;
                for (Long requestTime : responseTimes) {
                    sum += requestTime;
                }
                result = "OK;" + sum / responseTimes.size();
            } else if(commandType.equals("MAX") && parts.length == 3 && parts[2].equals("TIME")){
                result = "OK;" + Collections.max(responseTimes);
            }
            else {
                System.err.println("Client made a 'Syntax Error', command not correct");
                return "ERR; (Syntax Error) writing STAT request";
            }

           /*
            switch (commandType) {
                case "REQS":
                    result = "OK;" + responseTimes.size();
                    break;
                case "AVG_TIME":
                    double sum = 0.0;
                    for (Long requestTime : responseTimes) {
                        sum += requestTime;
                    }
                    result = "OK;" + sum / responseTimes.size();
                    break;
                case "MAX_TIME":
                    result = "OK;" + Collections.max(responseTimes);
                    break;
                default:
                    System.err.println("Syntax Error in the request");
                    return "ERR; Syntax Error";
            }*/
            return result;
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Missing argument in the request");
            return "ERR; Syntax Error, missing argument";
        }
    }


    private String handleComputeCommand(String operation, String[] parts) {
        try {
            String[] rest = parts[1].split(";", 3);
            if (rest.length < 3) {
                System.err.println("Client made a 'Syntax Error', command not correct");
                return "ERR; Syntax Error, missing parts";
            }
            String valuesKindString = rest[0];   // "GRID" or "LIST"
            String variableValues = rest[1];   // "x0:-1:0.1:1,x1:-10:1:20"
            String[] expressions = rest[2].split(";");   // "((x0+(2.0^x1))/(21.1-x0));(x1*x0)"

            // Validate expressions
            for (String expression : expressions) {
                Node parsedFunction = new Parser(expression).parse();
                if (!areAllVariablesDefined(parsedFunction)) {
                    return "ERR;(ComputationException) Invalid variable in expression";
                }
            }

            String result;
            switch (operation) {
                case "MAX":
                    result = String.valueOf(calculateMax(expressions, variableValues, valuesKindString));
                    break;
                case "MIN":
                    result = String.valueOf(calculateMin(expressions, variableValues, valuesKindString));
                    break;
                case "AVG":
                    result = String.valueOf(calculateAvg(expressions, variableValues, valuesKindString));
                    break;
                case "COUNT":
                    result = String.valueOf(calculateCount(expressions, variableValues, valuesKindString));
                    break;
                default:
                    throw new IllegalArgumentException("ERR; Invalid operation");
            }
            return result;
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Syntax Error made by client");
            return "ERR; Syntax Error";
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    private boolean areAllVariablesDefined(Node parsedFunction) {
        if (parsedFunction instanceof Variable) {
            String variableName = ((Variable) parsedFunction).getName();
            if (!VARIABLE_PATTERN.matcher(variableName).matches()) {
                return false;
            }
        } else if (parsedFunction instanceof Operator) {
            for (Node child : parsedFunction.getChildren()) {
                if (!areAllVariablesDefined(child)) {
                    return false;
                }
            }
        }
        return true;
    }

    private double calculateMax(String[] expressions, String variableValues, String valuesKindString) {
        double max = Double.NEGATIVE_INFINITY;
        for (String expression : expressions) {
            Node parsedFunction = new Parser(expression).parse();
            ValueTuplesHandler vtp = ValueTuplesHandler.getTuplesHandler();
            vtp.setValueTuples(variableValues, valuesKindString);
            Set<Map<String, Double>> tuples = vtp.getValueTuples();
            for (Map<String, Double> aa : tuples) {
                double val = processExpression(parsedFunction, aa);
                if (val > max) {
                    max = val;
                }
            }
        }
        return max;
    }

    private double calculateMin(String[] expressions, String variableValues, String valuesKindString) {
        double min = Double.POSITIVE_INFINITY;
        for (String expression : expressions) {
            Node parsedFunction = new Parser(expression).parse();
            ValueTuplesHandler vtp = ValueTuplesHandler.getTuplesHandler();
            vtp.setValueTuples(variableValues, valuesKindString);
            Set<Map<String, Double>> tuples = vtp.getValueTuples();
            for (Map<String, Double> aa : tuples) {
                double val = processExpression(parsedFunction, aa);
                if (val < min) {
                    min = val;
                }
            }
        }
        return min;
    }

    private double calculateAvg(String[] expressions, String variableValues, String valuesKindString) {
        double sum = 0;
        int count = 0;
        for (String expression : expressions) {
            Node parsedFunction = new Parser(expression).parse();
            ValueTuplesHandler vtp = ValueTuplesHandler.getTuplesHandler();
            vtp.setValueTuples(variableValues, valuesKindString);
            Set<Map<String, Double>> tuples = vtp.getValueTuples();
            for (Map<String, Double> aa : tuples) {
                double val = processExpression(parsedFunction, aa);
                sum += val;
                count++;
            }
        }
        return (count == 0) ? 0 : sum / count;
    }

    private double calculateCount(String[] expressions, String variableValues, String valuesKindString) {
        int totalCount = 0;
        for (String expression : expressions) {
            Node parsedFunction = new Parser(expression).parse();
            ValueTuplesHandler vtp = ValueTuplesHandler.getTuplesHandler();
            vtp.setValueTuples(variableValues, valuesKindString);
            Set<Map<String, Double>> tuples = vtp.getValueTuples();
            totalCount += tuples.size();
        }
        return totalCount;
    }
}
