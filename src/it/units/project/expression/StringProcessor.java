package it.units.project.expression;
import org.w3c.dom.ls.LSOutput;

import java.util.*;
import java.util.function.Function;

import static it.units.project.expression.Parser.processExpression;

public class StringProcessor implements Function<String, String> {

    @Override
    public String apply(String command) {
        long startTime = System.currentTimeMillis();
        String[] parts = command.split("_");
        String operation = parts[0];  // "MAX"

        String[] rest = parts[1].split(";", 3);
        String valuesKindString = rest[0];   // "GRID"
        String variableValues = rest[1];   // "x0:-1:0.1:1,x1:-10:1:20"
        String[] expressions = rest[2].split(";");   // "((x0+(2.0^x1))/(21.1-x0)) --- (x1*x0)"
        long finishTime;
        double elapsedTime;

        if (operation.equals("BYE")) {
            return null;  // Indica al server di chiudere la connessione
        }

        double result;
        switch (operation) {

            case "MAX":
                result = calculateMax(expressions, variableValues, valuesKindString);
                break;
            case "MIN":
                result = calculateMin(expressions, variableValues, valuesKindString);
                break;
            case "AVG":
                result = calculateAvg(expressions, variableValues, valuesKindString);
                break;
            case "COUNT":
                result = calculateCount(expressions, variableValues, valuesKindString);
                break;
            default:
                throw new IllegalArgumentException("ERR; Invalid operation");
        }
        finishTime = System.currentTimeMillis();
        elapsedTime = (double) (finishTime - startTime) / 1000.0;
        return System.out.printf("OK;%.3f;%.6f%n", elapsedTime, result).toString();

    }

    private double calculateMax(String[] expressions, String variableValues, String valuesKindString) {
        double max = Double.NEGATIVE_INFINITY;
        for (String expression : expressions) {
            Node parsedFunction = (new Parser(expression)).parse();
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
            Node parsedFunction = (new Parser(expression)).parse();
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
            Node parsedFunction = (new Parser(expression)).parse();
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
            Node parsedFunction = (new Parser(expression)).parse();
            ValueTuplesHandler vtp = ValueTuplesHandler.getTuplesHandler();
            vtp.setValueTuples(variableValues, valuesKindString);
            Set<Map<String, Double>> tuples = vtp.getValueTuples();
            totalCount += tuples.size();
        }
        return totalCount;
    }
}