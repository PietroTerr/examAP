package it.units.project.expression;

import java.util.Objects;

public class Tuple {
    private final Object first;
    private final Object second;

    public Tuple(Object first, Object second) {
        this.first = Objects.requireNonNull(first, "First element cannot be null");
        this.second = Objects.requireNonNull(second, "Second element cannot be null");
    }

    public Object getFirst() {
        return first;
    }

    public Object getSecond() {
        return second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tuple tuple = (Tuple) o;
        return first.equals(tuple.first) && second.equals(tuple.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}
