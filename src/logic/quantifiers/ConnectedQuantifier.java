package logic.quantifiers;

import jdrasil.graph.Bag;
import solver.MSOStateVector;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Map;

/**
 * This quantifier guesses a connected subgraph (with respect to E).
 */
public class ConnectedQuantifier implements Quantifier {

    /** Name of the variable */
    private String variable;

    /**
     * Create a connected quantifier from a .mso line.
     * @param variable
     */
    public ConnectedQuantifier(String... variable) {
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

        // once we left the connected component, we can not add vertices to it anymore
        if (state.stateDescription.get(this)[0] == -1) return new MSOStateVector.MSOState[]{state};

        MSOStateVector.MSOState[] newStates = new MSOStateVector.MSOState[2];

        // don't take the vertex
        newStates[0] = state.getCopy();
        newStates[0].stateDescription.get(this)[index] = 0;

        // take the vertex -> give it the smallest free non negative value
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

        // once we left the connected component, we can not add vertices to it anymore, same holds if the vertex was not chosen
        if (state.stateDescription.get(this)[0] == -1 || state.stateDescription.get(this)[index] == 0) return new MSOStateVector.MSOState[]{state};

        // get the components and the component of the current variable
        int[] components = state.stateDescription.get(this);
        int component = components[index];

        // count how many elements are in the same or in another component
        int sameComponent = 0;
        int otherComponent = 0;
        for (int x : components) {
            if (x == component) sameComponent++;
            else if (x > 0) otherComponent++;
        }

        // going to delete last element -> connected component is closed now
        if (sameComponent == 1 && otherComponent == 0) {
            for (int i = 0; i < components.length; i++) components[i] = -1;
            state.stateDescription.put(this, components);
            return new MSOStateVector.MSOState[]{state};
        }

        // delete the last element of the component, but the whole component is not closed yet -> not valid
        if (sameComponent == 1 && otherComponent > 0) {
            return new MSOStateVector.MSOState[]{};
        }

        // just remove the element
        state.stateDescription.get(this)[index] = 0;
        return new MSOStateVector.MSOState[]{state};
    }

    @Override
    public boolean requiresEdge() {
        return true;
    }

    @Override
    public MSOStateVector.MSOState[] edge(MSOStateVector.MSOState state, int v, int w, int indexV, int indexW) {

        // once we left the connected component, we can not add vertices to it anymore
        if (state.stateDescription.get(this)[0] == -1) return new MSOStateVector.MSOState[]{state};

        // update components
        int[] components = state.stateDescription.get(this);

        // if we do not connect components, do nothing
        if (components[indexV] == 0 || components[indexW] == 0) return new MSOStateVector.MSOState[]{state};

        int toReplace = 0;
        int replaceWith = 0;
        if (components[indexV] < components[indexW]) {
            toReplace = components[indexW];
            replaceWith = components[indexV];
        } else {
            toReplace = components[indexV];
            replaceWith = components[indexW];
        }
        for (int i = 0; i < components.length; i++) {
            if (components[i] == toReplace) components[i] = replaceWith;
        }
        state.stateDescription.put(this, components);
        return new MSOStateVector.MSOState[]{state};
    }

    @Override
    public boolean join(MSOStateVector.MSOState stateA, MSOStateVector.MSOState stateB, MSOStateVector.MSOState newState, Bag<Integer> bag, Map<Integer, Integer> treeIndex) {
        // if exactly one side is a solution return it, if both are done this is not a valid join
        if (stateA.stateDescription.get(this)[0] == -1 && stateB.stateDescription.get(this)[0] == -1) return false;
        if (stateA.stateDescription.get(this)[0] == -1) return true; // newState is copy of A anyway
        if (stateB.stateDescription.get(this)[0] == -1) {
            newState.stateDescription.put(this, Arrays.copyOf(stateB.stateDescription.get(this), stateB.stateDescription.get(this).length));
            return true;
        }

        // join components
        int[] result = Arrays.copyOf(stateA.stateDescription.get(this), stateA.stateDescription.get(this).length);
        int[] other = stateB.stateDescription.get(this);
        for (int i = 0; i < result.length-1; i++) {
            for (int j = i+1; j < result.length; j++) {
                if (result[i] <= 0 || result[j] <= 0 || result[i] == result[j]) continue; // no need to join
                if (other[i] != other[j]) continue; // can't join
                int replace = result[i] < result[j] ? result[j] : result[i];
                int with = result[i] >= result[j] ? result[j] : result[i];
                for (int k = 0; k < result.length; k++) {
                    if (result[k] == replace) result[k] = with;
                }
            }
        }
        newState.stateDescription.put(this, result);
        return true;
    }

    @Override
    public boolean finalCheck(MSOStateVector.MSOState state) { return true; }


}
