package logic.formulas;

import jdrasil.graph.Bag;
import logic.Structure;
import solver.MSOStateVector;

import java.util.Map;

/**
 * A formula of the form $\forall x\forall y E(x,y)\rightarrow\psi(x,y)$, where $\psi$ is a CNF.
 */
public class AxAyFormula extends Formula {

    /**
     * Super constructor.
     * @param structure
     */
    public AxAyFormula(Structure structure) {
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
    public int weakHashCode(MSOStateVector.MSOState state) {
        return 0;
    }

    @Override
    public boolean weakEquals(MSOStateVector.MSOState stateA, MSOStateVector.MSOState stateB) {
        return false;
    }

    @Override
    public boolean finalCheck(MSOStateVector.MSOState state) {
        return true;
    }

    @Override
    public boolean requiresIntroduce() {
        return false;
    }

    @Override
    public MSOStateVector.MSOState[] introduce(MSOStateVector.MSOState state, int v, int index) {
        return null;
    }

    @Override
    public boolean requiresForget() {
        return false;
    }

    @Override
    public MSOStateVector.MSOState[] forget(MSOStateVector.MSOState state, int v, int index) {
        return null;
    }

    @Override
    public boolean requiresEdge() {
        return true;
    }

    @Override
    public MSOStateVector.MSOState[] edge(MSOStateVector.MSOState state, int v, int w, int indexV, int indexW) {
        if (isSatisfied(v, w, state.assignment)) return new MSOStateVector.MSOState[]{state};
        return new MSOStateVector.MSOState[0];
    }

    @Override
    public boolean join(MSOStateVector.MSOState stateA, MSOStateVector.MSOState stateB, MSOStateVector.MSOState newState, Bag<Integer> bag, Map<Integer, Integer> treeIndex) {
        // we do not have to do anything, as we do not store anything
        return true;
    }

}
