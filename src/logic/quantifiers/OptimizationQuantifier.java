package logic.quantifiers;

import jdrasil.graph.Bag;
import solver.MSOStateVector;

import java.util.BitSet;
import java.util.Map;

/**
 * An optimization quantifier is an existential second-order quantifier that minimizes a function over the
 * elements he uses in his quantified set.
 */
public class OptimizationQuantifier implements Quantifier {

    /** Name of the variable */
    private String variable;

    /** We compute a weighted optimization function. */
    private int[] weights;

    /**
     * Create an optimization quantifier from a .mso line.
     * @param constants
     */
    public OptimizationQuantifier(String... constants) {

        // parse variable name
        this.variable = constants[1];

        // parse constants
        this.weights = new int[constants.length-2];
        for (int i = 2; i < constants.length; i++) {
            this.weights[i-2] = Integer.parseInt(constants[i]);
        }
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
    public int weakHashCode(MSOStateVector.MSOState state) {
        return 0;
    }

    @Override
    public boolean weakEquals(MSOStateVector.MSOState stateA, MSOStateVector.MSOState stateB) {
        return false;
    }

    @Override
    public boolean requiresIntroduce() {
        return true;
    }

    @Override
    public MSOStateVector.MSOState[] introduce(MSOStateVector.MSOState state, int v, int index) {
        MSOStateVector.MSOState[] newStates = new MSOStateVector.MSOState[2];

        // don't take v
        newStates[0] = state.getCopy();
        newStates[0].stateDescription.get(this)[index] = 0;

        // take v
        newStates[1] = state.getCopy();
        newStates[1].stateDescription.get(this)[index] = 1;
        newStates[1].value += weights[v];
        if (!newStates[1].assignment.containsKey(variable)) newStates[1].assignment.put(variable, new BitSet());
        newStates[1].assignment.get(variable).set(v);

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
        return new MSOStateVector.MSOState[0];
    }

    @Override
    public boolean join(MSOStateVector.MSOState stateA, MSOStateVector.MSOState stateB, MSOStateVector.MSOState newState, Bag<Integer> bag, Map<Integer, Integer> treeIndex) {
        // update the value, which is the sum minus elements in the bag (which are counted twice)
        newState.value = stateA.value + stateB.value;
        for (Integer v : bag.vertices) {
            if (newState.stateDescription.get(this)[treeIndex.get(v)] == 0) continue;
            newState.value -= weights[v];
        }
        return true;
    }

    @Override
    public boolean finalCheck(MSOStateVector.MSOState state) { return true; }

    @Override
    public String toString() {
        return variable;
    }

}
