package logic;

import jdrasil.graph.Bag;
import solver.MSOStateVector;

import java.util.Map;

/**
 * An logical object captures all parts of an formula, i.\,e., quantifier and subformulas. This interface defined functions
 * that are required by the DP that solves the model-checking problem.
 */
public interface LogicalObject {

    /**
     * A weak-join is more cost expensive, as not only equal but "compatible" states are joined.
     * @return True if a weak join must be performed for this object.
     */
    public boolean requiresWeakJoin();

    /**
     * A weak hash-code may map non-equal object to the same hash value on purpose, as these objects may be "compatible" to join.
     * @param state The state for wish the value shall be computed.
     * @return The weak hash-code.
     */
    public int weakHashCode(MSOStateVector.MSOState state);

    /**
     * Tests weak-equality of two states, which in essence states that the states are "compatible" but not necessarily equal.
     * @param stateA The first state.
     * @param stateB The state to compare with.
     * @return True, if the states are weak-equal.
     */
    public boolean weakEquals(MSOStateVector.MSOState stateA, MSOStateVector.MSOState stateB);

    /**
     * Getter for the number of integers (array-size) that has to be reserved for this logical object.
     * @param tw The tree-width of the decomposition we work with (not bag size).
     * @return The number of integers (or "array-slots") that are required by this logical object.
     */
    public int getStateSize(int tw);

    /**
     * Indicate whether or not this logical object needs interaction on introduce-bag.
     * @return True, if @see introduce shall be called.
     */
    public boolean requiresIntroduce();

    /**
     * Introduce vertex $v$ with the given index to the state and return an (possible empty) array of new states.
     * @param state The state to which we insert.
     * @param v The vertex that is introduced.
     * @param index The tree-index of the vertex.
     * @return An array of new states.
     */
    public MSOStateVector.MSOState[] introduce(MSOStateVector.MSOState state, int v, int index);

    /**
     * Indicates whether or not this logical object needs interaction on forget-bags.
     * @return True, if @see forget shall be called.
     */
    public boolean requiresForget();

    /**
     * Forget vertex $v$ with the given index from the state, this results in an (possible empty) array of new states.
     * @param state The state we work on.
     * @param v The vertex to be forgotten.
     * @param index The tree-index of the vertex.
     * @return An array of new states.
     */
    public MSOStateVector.MSOState[] forget(MSOStateVector.MSOState state, int v, int index);

    /**
     * Indicates whether or not this logical object requires interaction on edge-bags.
     * @return True, if @see edge shall be called.
     */
    public boolean requiresEdge();

    /**
     * Introduce the edge $\{v,w\}$ with the given indices to the given state, which results in an (possible empty) array of new states.
     * @param state The state we work on.
     * @param v The first vertex of the edge.
     * @param w The second vertex of the edge.
     * @param indexV The tree-index of the first vertex.
     * @param indexW The tree-index of the second vertex.
     * @return An array of new states.
     */
    public MSOStateVector.MSOState[] edge(MSOStateVector.MSOState state, int v, int w, int indexV, int indexW);

    /**
     * Join two given states into the given new state. Returns true if this was successful, or false if the join is not valid
     * and the new state should be discarded.
     * @param stateA First state.
     * @param stateB Second state.
     * @param newState The state which should be the result of the join, initial this is a copy of stateA.
     * @param bag The bag we currently work in.
     * @param treeIndex The tree-index that is used.
     * @return True if the join was successful and false, if the result should be discarded.
     */
    public boolean join(MSOStateVector.MSOState stateA, MSOStateVector.MSOState stateB, MSOStateVector.MSOState newState, Bag<Integer> bag, Map<Integer, Integer> treeIndex);

    /**
     * After the DP was finished, this method can be used to check if the result is valid. Most objects will just return true.
     * @param state The state to be checked.
     * @return True, if the state is a valid result.
     */
    public boolean finalCheck(MSOStateVector.MSOState state);

}
