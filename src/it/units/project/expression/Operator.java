package it.units.project.expression;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Operator extends Node {

  public enum Type {
    SUM('+', a -> a[0] + a[1]),
    SUBTRACTION('-', a -> a[0] - a[1]),
    MULTIPLICATION('*', a -> a[0] * a[1]),
    DIVISION('/', a -> a[0] / a[1]),
    POWER('^', a -> Math.pow(a[0], a[1]));
    private final char symbol;
    private final Function<double[], Double> function;

    Type(char symbol, Function<double[], Double> function) {
      this.symbol = symbol;
      this.function = function;
    }

    public char getSymbol() {
      return symbol;
    }

    public Function<double[], Double> getFunction() {
      return function;
    }
  }

  private final Type type;

  public Operator(Type type, List<Node> children) {
    super(children);
    this.type = type;
  }

  public Type getType() {
    return type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Operator operator = (Operator) o;
    return type == operator.type;
  }

  @Override
  public int hashCode() {
    return Objects.hash(type);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("(");
    sb.append(getChildren().stream()
        .map(Node::toString)
        .collect(Collectors.joining(" " + Character.toString(type.symbol) + " "))
    );
    sb.append(")");
    return sb.toString();
  }



  @Override
  public double evaluate(Map<String, Double> variables) {
    double left = getChildren().get(0).evaluate(variables);
    double right = getChildren().get(1).evaluate(variables);

    switch (type) {
      case SUM:
        return left + right;
      case SUBTRACTION:
        return left - right;
      case MULTIPLICATION:
        return left * right;
      case DIVISION:
        if (right == 0) {
          throw new ArithmeticException("Division by zero");
        }
        return left / right;
      case POWER:
        return Math.pow(left, right);
      default:
        throw new IllegalArgumentException("Unknown operator: " + type);
    }
  }
}
