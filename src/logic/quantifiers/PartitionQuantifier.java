package logic.quantifiers;

import jdrasil.graph.Bag;
import solver.MSOStateVector;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

/**
 * A partition quantifier divides the universe into a given amount of disjoint sets. For instance, the classical 3-coloring
 * formula can be implement using one partition quantifier over three sets R, G, B.
 */
public class PartitionQuantifier implements Quantifier {

    /** Variables are represented by integers that can be assigned to atoms. */
    private Map<String, Integer> variableToValue;
    private Map<Integer, String> valueToVariable;

    /** Maximum value used to represent a set (i.\,e., number of sets). */
    private int maxValue;

    /** Constructor that parses a .mso line to initialize data structures. */
    public PartitionQuantifier(String... variables) {
        this.variableToValue = new HashMap<>();
        this.valueToVariable = new HashMap<>();
        int value = 0;
        for (int i = 1; i < variables.length; i++) {
            this.variableToValue.put(variables[i], value);
            this.valueToVariable.put(value, variables[i]);
            value++;
        }
        this.maxValue = value;
    }

    @Override
    public int getStateSize(int tw) {
        return tw + 1;
    }

    @Override
    public boolean requiresWeakJoin() {
        return false;
    }

    @Override
    public boolean weakEquals(MSOStateVector.MSOState stateA, MSOStateVector.MSOState stateB) {
        return false;
    }

    @Override
    public int weakHashCode(MSOStateVector.MSOState state) {
        return 0;
    }

    @Override
    public boolean requiresIntroduce() {
        return true;
    }

    @Override
    public MSOStateVector.MSOState[] introduce(MSOStateVector.MSOState state, int v, int index) {
        MSOStateVector.MSOState[] newStates = new MSOStateVector.MSOState[maxValue];
        for (int i = 0; i < maxValue; i++) {
            newStates[i] = state.getCopy();
            newStates[i].stateDescription.get(this)[index] = i;
            if (!newStates[i].assignment.containsKey(valueToVariable.get(i)))
                newStates[i].assignment.put(valueToVariable.get(i), new BitSet());
            newStates[i].assignment.get(valueToVariable.get(i)).set(v);
        }
        return newStates;
    }

    @Override
    public boolean requiresForget() {
        return true;
    }

    @Override
    public MSOStateVector.MSOState[] forget(MSOStateVector.MSOState state, int v, int index) {
        state.stateDescription.get(this)[index] = 0;
        return new MSOStateVector.MSOState[]{state};
    }

    @Override
    public boolean requiresEdge() {
        return false;
    }

    @Override
    public MSOStateVector.MSOState[] edge(MSOStateVector.MSOState state, int v, int w, int indexV, int indexW) {
        return null;
    }

    @Override
    public boolean join(MSOStateVector.MSOState stateA, MSOStateVector.MSOState stateB, MSOStateVector.MSOState newState, Bag<Integer> bag, Map<Integer, Integer> treeIndex) {
        // we do not have to do anything, as newState already is a copy of stateA
        return true;
    }

    @Override
    public boolean finalCheck(MSOStateVector.MSOState state) { return true; }

}
