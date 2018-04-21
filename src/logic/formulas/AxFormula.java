package logic.formulas;

import jdrasil.graph.Bag;
import logic.Structure;
import solver.MSOStateVector;

import java.util.Map;

/**
 * A formula of the form $\forall x \psi(x)$, where $\psi$ is a CNF.
 */
public class AxFormula extends Formula {

    /** Super-Constructor. */
    public AxFormula(Structure structure) {
        super(structure);
    }

    @Override
    public int getStateSize(int tw) {
        return 0;
    }

    @Override
    public boolean requiresWeakJoin() {
        return false;
    }

    @Override
    public boolean weakEquals(MSOStateVector.MSOState stateA, MSOStateVector.MSOState stateB) {
        return true;
    }

    @Override
    public int weakHashCode(MSOStateVector.MSOState state) {
        return 0;
    }

    @Override
    public boolean finalCheck(MSOStateVector.MSOState state) {
        return true;
    }

    @Override
    public boolean requiresIntroduce() {
        return true;
    }

    @Override
    public MSOStateVector.MSOState[] introduce(MSOStateVector.MSOState state, int v, int index) {
        if (isSatisfied(v, v, state.assignment)) return new MSOStateVector.MSOState[]{state};
        return new MSOStateVector.MSOState[0];
    }

    @Override
    public boolean requiresForget() {
        return false;
    }

    @Override
    public MSOStateVector.MSOState[] forget(MSOStateVector.MSOState state, int v, int index) {
        return new MSOStateVector.MSOState[0];
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
        return true;
    }

}
