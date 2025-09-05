package com.example.expression;

import com.example.expression.ast.*;
import com.example.expression.evaluator.ExpressionEvaluator;
import com.example.expression.printer.PrettyPrinter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Main application demonstrating the expression evaluator with visitor pattern.
 */
public class ExpressionEvaluatorApp {
    
    public static void main(String[] args) {
        System.out.println("Expression Evaluator Demo");
        System.out.println("========================");
        System.out.println();
        
        // Demo with hardcoded expressions
        runDemo();
        
        // Interactive mode
        if (args.length == 0 || !args[0].equals("--no-interactive")) {
            runInteractiveMode();
        }
    }
    
    private static void runDemo() {
        System.out.println("Demo: Evaluating sample expressions");
        System.out.println("-----------------------------------");
        
        ExpressionEvaluator evaluator = new ExpressionEvaluator();
        PrettyPrinter printer = new PrettyPrinter();
        
        // Set some variables
        evaluator.setVariable("x", 5.0);
        evaluator.setVariable("y", 3.0);
        
        // Create sample expressions manually (normally these would come from parsing)
        ASTNode[] expressions = {
            // 2 + 3 * 4
            new BinaryOperationNode(
                new NumberNode(2),
                "+",
                new BinaryOperationNode(
                    new NumberNode(3),
                    "*",
                    new NumberNode(4)
                )
            ),
            
            // sin(pi / 2)
            new FunctionCallNode("sin", Arrays.asList(
                new BinaryOperationNode(
                    new ConstantNode("pi"),
                    "/",
                    new NumberNode(2)
                )
            )),
            
            // sqrt(x^2 + y^2)
            new FunctionCallNode("sqrt", Arrays.asList(
                new BinaryOperationNode(
                    new BinaryOperationNode(
                        new IdentifierNode("x"),
                        "^",
                        new NumberNode(2)
                    ),
                    "+",
                    new BinaryOperationNode(
                        new IdentifierNode("y"),
                        "^",
                        new NumberNode(2)
                    )
                )
            )),
            
            // log(10, 100)
            new FunctionCallNode("log", Arrays.asList(
                new NumberNode(10),
                new NumberNode(100)
            )),
            
            // -abs(-5.5)
            new UnaryOperationNode("-",
                new FunctionCallNode("abs", Arrays.asList(
                    new UnaryOperationNode("-", new NumberNode(5.5))
                ))
            )
        };
        
        for (int i = 0; i < expressions.length; i++) {
            ASTNode expr = expressions[i];
            try {
                String formatted = printer.format(expr);
                double result = evaluator.evaluate(expr);
                System.out.printf("%d. %s = %.6f%n", i + 1, formatted, result);
            } catch (Exception e) {
                System.out.printf("%d. %s = ERROR: %s%n", i + 1, printer.format(expr), e.getMessage());
            }
        }
        
        System.out.println();
        System.out.println("Variables used: x = " + evaluator.getVariable("x") + 
                          ", y = " + evaluator.getVariable("y"));
        System.out.println();
    }
    
    private static void runInteractiveMode() {
        System.out.println("Interactive Mode");
        System.out.println("----------------");
        System.out.println("Note: This demo uses manually constructed AST nodes.");
        System.out.println("In a real application, you would parse input strings to create the AST.");
        System.out.println();
        System.out.println("Available operations:");
        System.out.println("  Arithmetic: +, -, *, /, %, ^");
        System.out.println("  Functions: sin, cos, tan, ln, log, sqrt, abs");
        System.out.println("  Constants: pi, e");
        System.out.println("  Variables: You can set variables using 'set <name> <value>'");
        System.out.println();
        System.out.println("Commands:");
        System.out.println("  'demo <number>' - Run a predefined demo expression");
        System.out.println("  'set <var> <value>' - Set a variable value");
        System.out.println("  'vars' - Show all variables");
        System.out.println("  'quit' - Exit");
        System.out.println();
        
        Scanner scanner = new Scanner(System.in);
        ExpressionEvaluator evaluator = new ExpressionEvaluator();
        PrettyPrinter printer = new PrettyPrinter();
        
        // Set default variables
        evaluator.setVariable("x", 1.0);
        evaluator.setVariable("y", 2.0);
        
        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            
            if (input.equals("quit") || input.equals("exit")) {
                break;
            }
            
            if (input.equals("vars")) {
                System.out.println("Variables:");
                // Note: In a real implementation, you'd have access to the variables map
                System.out.println("  x = " + evaluator.getVariable("x"));
                System.out.println("  y = " + evaluator.getVariable("y"));
                continue;
            }
            
            if (input.startsWith("set ")) {
                String[] parts = input.split("\\s+");
                if (parts.length == 3) {
                    try {
                        String varName = parts[1];
                        double value = Double.parseDouble(parts[2]);
                        evaluator.setVariable(varName, value);
                        System.out.println("Set " + varName + " = " + value);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid number format");
                    }
                } else {
                    System.out.println("Usage: set <variable> <value>");
                }
                continue;
            }
            
            if (input.startsWith("demo ")) {
                try {
                    int demoNum = Integer.parseInt(input.substring(5).trim());
                    runDemoExpression(demoNum, evaluator, printer);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid demo number");
                }
                continue;
            }
            
            if (input.isEmpty()) {
                continue;
            }
            
            System.out.println("Note: Full parsing not implemented in this demo.");
            System.out.println("Use 'demo <1-5>' to try predefined expressions.");
        }
        
        scanner.close();
        System.out.println("Goodbye!");
    }
    
    private static void runDemoExpression(int demoNum, ExpressionEvaluator evaluator, PrettyPrinter printer) {
        ASTNode expr = null;
        
        switch (demoNum) {
            case 1:
                // x + y * 2
                expr = new BinaryOperationNode(
                    new IdentifierNode("x"),
                    "+",
                    new BinaryOperationNode(
                        new IdentifierNode("y"),
                        "*",
                        new NumberNode(2)
                    )
                );
                break;
                
            case 2:
                // cos(pi)
                expr = new FunctionCallNode("cos", Arrays.asList(
                    new ConstantNode("pi")
                ));
                break;
                
            case 3:
                // sqrt(x^2 + y^2)
                expr = new FunctionCallNode("sqrt", Arrays.asList(
                    new BinaryOperationNode(
                        new BinaryOperationNode(
                            new IdentifierNode("x"),
                            "^",
                            new NumberNode(2)
                        ),
                        "+",
                        new BinaryOperationNode(
                            new IdentifierNode("y"),
                            "^",
                            new NumberNode(2)
                        )
                    )
                ));
                break;
                
            case 4:
                // ln(e^x)
                expr = new FunctionCallNode("ln", Arrays.asList(
                    new BinaryOperationNode(
                        new ConstantNode("e"),
                        "^",
                        new IdentifierNode("x")
                    )
                ));
                break;
                
            case 5:
                // abs(-x) + y
                expr = new BinaryOperationNode(
                    new FunctionCallNode("abs", Arrays.asList(
                        new UnaryOperationNode("-", new IdentifierNode("x"))
                    )),
                    "+",
                    new IdentifierNode("y")
                );
                break;
                
            default:
                System.out.println("Demo " + demoNum + " not available. Try 1-5.");
                return;
        }
        
        try {
            String formatted = printer.format(expr);
            double result = evaluator.evaluate(expr);
            System.out.printf("Demo %d: %s = %.6f%n", demoNum, formatted, result);
        } catch (Exception e) {
            System.out.printf("Demo %d: %s = ERROR: %s%n", demoNum, printer.format(expr), e.getMessage());
        }
    }
}