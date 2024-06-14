package it.units.project.expression;

import it.units.project.expression.Node;
import it.units.project.expression.Parser;
import it.units.project.expression.ValueTuplesHandler;

import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import static it.units.project.expression.Parser.processExpression;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        long startTime = System.currentTimeMillis();
        String operation = input.split("_")[0];
        switch (operation) {
            case "STAT":
                switch (input.split("_")[1]){
                    case "REQS":

                        break;
                    case "AVG":

                        break;
                    case "MAX":

                        break;
                    default:
                        System.out.println("Invalid stat operation");
                }
                break;
            case "MAX":

                break;
            case "MIN":

                break;
            case "AVG":

                break;
            case "COUNT":

                break;
            case "BYE":/* CLIENT SI DISCONNETTE */ return;
            default:
                System.out.println("Invalid operation");;
        }



        /*double max = -100;
        for (Map<String, Double> aa : prova) {
            double val = processExpression(parsedFunction, aa);
            if (val > max) {
                max = val;
            }
        }
    
        System.out.printf("%.6f", max);
        
         */
        
         
    }
}