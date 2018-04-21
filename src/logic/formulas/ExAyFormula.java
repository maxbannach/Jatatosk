package logic.formulas;

import jdrasil.graph.Bag;
import logic.Structure;
import solver.MSOStateVector;

import java.util.Map;

/**
 * A formula of the form $\exists x\forall y E(x,y)\rightarrow\psi(x,y)$, where $\psi$ is a CNF.
 */
public class ExAyFormula extends Formula {

    /** Super-Constructor. */
    public ExAyFormula(Structure structure) {
        super(structure);
    }

    @Override
    public int getStateSize(int tw) {
        return tw + 2;
    }

    @Override
    public boolean requiresWeakJoin() {
        return true;
    }

    @Override
    public int weakHashCode(MSOStateVector.MSOState state) {
        return 0;
    }

    @Override
    public boolean weakEquals(MSOStateVector.MSOState stateA, MSOStateVector.MSOState stateB) {
        return true;
    }

    @Override
    public boolean finalCheck(MSOStateVector.MSOState state) {
        return state.stateDescription.get(this)[state.stateDescription.get(this).length-1] == 1;
    }

    @Override
    public boolean requiresIntroduce() {
        return true;
    }

    @Override
    public MSOStateVector.MSOState[] introduce(MSOStateVector.MSOState state, int v, int index) {
        state.stateDescription.get(this)[index] = 1;
        return new MSOStateVector.MSOState[]{state};
    }

    @Override
    public boolean requiresForget() {
        return true;
    }

    @Override
    public MSOStateVector.MSOState[] forget(MSOStateVector.MSOState state, int v, int index) {
        if (state.stateDescription.get(this)[index] == 1) state.stateDescription.get(this)[state.stateDescription.get(this).length-1] = 1;
        state.stateDescription.get(this)[index] = 0;
        return new MSOStateVector.MSOState[]{state};
    }

    @Override
    public boolean requiresEdge() {
        return true;
    }

    @Override
    public MSOStateVector.MSOState[] edge(MSOStateVector.MSOState state, int v, int w, int indexV, int indexW) {
        if (!isSatisfied(v, w, state.assignment)) state.stateDescription.get(this)[indexV] = 0;
        if (!isSatisfied(w, v, state.assignment)) state.stateDescription.get(this)[indexW] = 0;
        return new MSOStateVector.MSOState[]{state};
    }

    @Override
    public boolean join(MSOStateVector.MSOState stateA, MSOStateVector.MSOState stateB, MSOStateVector.MSOState newState, Bag<Integer> bag, Map<Integer, Integer> treeIndex) {
        int k = newState.stateDescription.get(this).length;
        for (int i = 0; i < k-1; i++) {
            // logical and of the bits in both states, unless the last one
            newState.stateDescription.get(this)[i] = stateA.stateDescription.get(this)[i] * stateB.stateDescription.get(this)[i];
        }
        // logical or on the decision bit
        newState.stateDescription.get(this)[k-1] = Math.min(1, stateA.stateDescription.get(this)[k-1] + stateB.stateDescription.get(this)[k-1]);
        return true;
    }

}
