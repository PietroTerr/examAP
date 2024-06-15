package it.units.project.expression;

import java.math.BigDecimal;
import java.util.*;

public  class ValueTuplesHandler {


    private enum valuesKind {
        GRID,
        LIST
    }

    Set<Map<String, Double>> valueTuples = new HashSet<>();
    Iterator<Map<String, Double>> iterator;

    public ValueTuplesHandler() {
        valueTuples = new HashSet<>();
    }

    public static ValueTuplesHandler getTuplesHandler() {
        return new ValueTuplesHandler();
    }

    public Set<Map<String, Double>> getValueTuples() {
        return valueTuples;
    }

    public void setValueTuples(String variableValues, String valuesKindString) {
        valuesKind vk;
        if (valuesKindString.equals("GRID")) {
            vk = valuesKind.GRID;
        } else if (valuesKindString.equals("LIST")) {
            vk = valuesKind.LIST;
        } else {
            throw new IllegalArgumentException("ERR; Invalid value kind: " + valuesKindString);
        }

        String[] infos = variableValues.split(",");
        for (String info : infos) {
            String[] values = info.split(":");
            String variableName = values[0];
            BigDecimal start = new BigDecimal(values[1]);
            BigDecimal step = new BigDecimal(values[2]);
            BigDecimal end = new BigDecimal(values[3]);
            List<Double> valueList = new ArrayList<>();
            for (BigDecimal j = start; j.compareTo(end) <= 0; j = j.add(step)) {
                valueList.add(Double.parseDouble(j.toString()));
            }

            if (valueTuples.isEmpty()) {
                for (Double value : valueList) {
                    Map<String, Double> map = new HashMap<>();
                    map.put(variableName, value);
                    valueTuples.add(map);
                }
            } else {
                if (vk == valuesKind.GRID) {
                    Set<Map<String, Double>> grid = new HashSet<>();
                    for (Map<String, Double> value : valueTuples) {
                        for (Double val : valueList) {
                            Map<String, Double> temp = new HashMap<>(value);
                            temp.put(variableName, val);
                            grid.add(temp);
                        }
                    }
                    valueTuples = grid;
                } else if (vk == valuesKind.LIST) {
                    Set<Map<String, Double>> list = new HashSet<>();
                    int i = 0;
                    for (Map<String, Double> value : valueTuples) {
                        if (i >= valueList.size()) break; // Evita l'eccezione IndexOutOfBoundsException
                        Map<String, Double> temp = new HashMap<>(value);
                        temp.put(variableName, valueList.get(i));
                        list.add(temp);
                        i++;
                    }
                    valueTuples = list;
                } else {
                    throw new IllegalArgumentException("Invalid value kind: " + valuesKindString);
                }
            }
        }


    }
}
