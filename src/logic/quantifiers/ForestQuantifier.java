package logic.quantifiers;

import jdrasil.graph.Bag;
import solver.MSOStateVector;

import java.util.*;

/**
 * This quantifier guesses a acyclic subgraph (with respect to E).
 */
public class ForestQuantifier implements Quantifier {

    /** Name of the variable */
    private String variable;

    /**
     * Create a connected quantifier from a .mso line.
     * @param variable
     */
    public ForestQuantifier(String... variable) {
        this.variable = variable[1];
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
        BitSet mask = new BitSet();
        int[] stateDescription = state.stateDescription.get(this);
        for (int i = 0; i < stateDescription.length; i++) {
            if (stateDescription[i] > 0) mask.set(i);
        }
        return mask.hashCode();
    }

    @Override
    public boolean weakEquals(MSOStateVector.MSOState stateA, MSOStateVector.MSOState stateB) {
        int[] stateDescriptionA = stateA.stateDescription.get(this);
        int[] stateDescriptionB = stateB.stateDescription.get(this);
        for (int i = 0; i < stateDescriptionA.length; i++) {
            if ( (stateDescriptionA[i] > 0) != (stateDescriptionB[i] > 0) ) return false;
        }
        return true;
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
        int value = Arrays.stream(newStates[1].stateDescription.get(this)).max().getAsInt()+1;
        newStates[1].stateDescription.get(this)[index] = value;
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
        return true;
    }

    @Override
    public MSOStateVector.MSOState[] edge(MSOStateVector.MSOState state, int v, int w, int indexV, int indexW) {
        int[] values = state.stateDescription.get(this);
        if (values[indexV] == 0 || values[indexW] == 0) return new MSOStateVector.MSOState[]{state}; // don't do anything
        if (values[indexV] == values[indexW]) return new MSOStateVector.MSOState[0]; // not valid, return nothing

        int replace = values[indexV] < values[indexW] ? values[indexW] : values[indexV];
        int with = values[indexV] >= values[indexW] ? values[indexW] : values[indexV];
        for (int i = 0; i < values.length; i++) {
            if (values[i] == replace) values[i] = with;
        }
        state.stateDescription.put(this, values);
        return new MSOStateVector.MSOState[]{state};
    }

    /**
     * Finds the root of v in the given forest ("union-find"-find with path compression).
     * @param forest
     * @param v
     * @return
     */
    private int find(int[] forest, int v) {
        if (forest[v] == v) return v;
        forest[v] = find(forest, forest[v]);
        return forest[v];
    }

    /**
     * Unites the components of v and w ("union-find"-union), returns true if a join actually happened, and false if v
     * and w where already in the same component.
     * @param forest
     * @param v
     * @param w
     * @return
     */
    private boolean join(int[] forest, int v, int w) {
        int rootV = find(forest, v);
        int rootW = find(forest, w);
        if (rootV == rootW) return false;
        forest[rootW] = rootV;
        return true;
    }

    /**
     * Given an initial union-find forest, and a map from vertices to components. This function computes a spanning forest
     * in the union-find structure and adds used edges to the given list.
     * @param forest An initial union-find structure (each vertex has itself as root)
     * @param components A map from vertices to components.
     * @param forestEdges A list to which used edges will be added.
     */
    private void computeSpanningForest(int[] forest, int[] components, List<Integer> forestEdges) {
        for (int v = 0; v < forest.length-1; v++) {
            if (components[v] == 0) continue; // v not selected to the forest
            for (int w = v+1; w < forest.length; w++) {
                if (components[w] == 0) continue; // w not selected to the forest
                if (components[v] != components[w]) continue; // no path between v and w
                if (join(forest, v, w)) {
                    forestEdges.add(v);
                    forestEdges.add(w);
                }
            }
        }
    }

    @Override
    public boolean join(MSOStateVector.MSOState stateA, MSOStateVector.MSOState stateB, MSOStateVector.MSOState newState, Bag<Integer> bag, Map<Integer, Integer> treeIndex) {

        // initialize three union-find data structures
        int[] firstForest = new int[stateA.stateDescription.get(this).length];
        int[] secondForest = new int[stateA.stateDescription.get(this).length];
        int[] joinedForest = new int[stateA.stateDescription.get(this).length];
        for (int i = 0; i < firstForest.length; i++) {
            firstForest[i] = i;
            secondForest[i] = i;
            joinedForest[i] = i;
        }

        // compute for both bags a spanning forest in the form of an edge list
        List<Integer> forestEdges = new ArrayList<>();
        computeSpanningForest(firstForest, stateA.stateDescription.get(this), forestEdges);
        computeSpanningForest(secondForest, stateB.stateDescription.get(this), forestEdges);

        // combine the the spanning forests, return false if a cycle is detected
        for (int i = 0; i < forestEdges.size(); i+=2) {
            if (!join(joinedForest, forestEdges.get(i), forestEdges.get(i+1))) return false;
        }

        // compress paths such that the union-find structure represents components
        for (int i = 0; i < joinedForest.length; i++) find(joinedForest, i);

        // map union-find forest to new components
        int[] components = stateA.stateDescription.get(this);
        int numberOfComponents = 1;
        int[] map = new int[joinedForest.length];
        for (int i = 0; i < map.length; i++) {
            if (components[i] == 0) continue;
            if (map[joinedForest[i]] == 0) map[joinedForest[i]] = numberOfComponents++;
        }
        for (int i = 0; i < map.length; i++) {
            if (components[i] == 0) joinedForest[i] = 0;
            else joinedForest[i] = map[joinedForest[i]];
        }

        // update components in new state
        newState.stateDescription.put(this, joinedForest);
        return true;
    }

    @Override
    public boolean finalCheck(MSOStateVector.MSOState state) { return true; }

    @Override
    public String toString() {
        return variable;
    }

}
