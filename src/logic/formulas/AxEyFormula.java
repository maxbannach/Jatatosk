package logic.formulas;

import jdrasil.graph.Bag;
import logic.Structure;
import solver.MSOStateVector;

import java.util.Map;

/**
 * A formula of the form $\forall x\exists y E(x,y)\wedge\psi(x,y)$, where $\psi$ is a CNF.
 */
public class AxEyFormula extends Formula {

    public AxEyFormula(Structure structure) {
        super(structure);
    }

    @Override
    public int getStateSize(int tw) {
        return tw + 1;
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
        return true;
    }

    @Override
    public boolean requiresIntroduce() {
        return false;
    }

    @Override
    public MSOStateVector.MSOState[] introduce(MSOStateVector.MSOState state, int v, int index) {
        return new MSOStateVector.MSOState[0];
    }

    @Override
    public boolean requiresForget() {
        return true;
    }

    @Override
    public MSOStateVector.MSOState[] forget(MSOStateVector.MSOState state, int v, int index) {
        // check if we did see the neighbor for v
        if (state.stateDescription.get(this)[index] == 0) return new MSOStateVector.MSOState[0];
        state.stateDescription.get(this)[index] = 0;
        return new MSOStateVector.MSOState[]{state};
    }

    @Override
    public boolean requiresEdge() {
        return true;
    }

    @Override
    public MSOStateVector.MSOState[] edge(MSOStateVector.MSOState state, int v, int w, int indexV, int indexW) {
        if (isSatisfied(v, w, state.assignment)) state.stateDescription.get(this)[indexV] = 1;
        if (isSatisfied(w, v, state.assignment)) state.stateDescription.get(this)[indexW] = 1;
        return new MSOStateVector.MSOState[]{state};
    }

    @Override
    public boolean join(MSOStateVector.MSOState stateA, MSOStateVector.MSOState stateB, MSOStateVector.MSOState newState, Bag<Integer> bag, Map<Integer, Integer> treeIndex) {
        for (int i = 0; i < newState.stateDescription.get(this).length; i++) {
            // logical or of the bits in both states
            newState.stateDescription.get(this)[i] = Math.min(1, stateA.stateDescription.get(this)[i] + stateB.stateDescription.get(this)[i]);
        }
        return true;
    }

//    @Override
//    public boolean requiresWeakJoin() {
//        return true;
//    }
//
//    @Override
//    public BitSet introduce(int v, int index, BitSet storage, Map<String, BitSet> assignment) {
//        storage.clear(index);
//        return storage;
//    }
//
//    @Override
//    public BitSet forget(int v, int index, BitSet storage, Map<String, BitSet> assignment) {
//        if (!storage.get(index)) return null;
//        return storage;
//    }
//
//    @Override
//    public BitSet edge(int v, int w, int indexV, int indexW, BitSet storage, Map<String, BitSet> assignment) {
//        if (isSatisfied(v, w, assignment)) storage.set(indexV);
//        if (isSatisfied(w, v, assignment)) storage.set(indexW);
//        return storage;
//    }
//
//    @Override
//    public BitSet join(BitSet storageA, BitSet storageB, Map<String, BitSet> assignment) {
//        BitSet result = (BitSet) storageA.clone();
//        result.or(storageB);
//        return result;
//    }
//
//    @Override
//    public boolean finalCheck(MSOStateVector.MSOState state) {
//        return true;
//    }
}
