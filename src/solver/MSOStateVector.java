package solver;

import jdrasil.graph.Bag;
import jdrasil.workontd.StateVector;
import logic.LogicalObject;
import logic.formulas.Formula;
import logic.quantifiers.Quantifier;

import java.util.*;

/**
 * Represent a collections of states in which the tree-automaton can be for a specific bag.
 */
public class MSOStateVector implements StateVector<Integer> {

    /** turn of logging */
    private final boolean LOG = false;

    /** All fragments and quantifier. of the formula. */
    private List<LogicalObject> logicalObjects;

    /** All possible states for the current bag. */
    protected Map<MSOState, MSOState> states;

    /**
     * Just initialize data structures.
     * @param tw The tree width of the decomposition (not bag size).
     * @param quantifiers All quantifier of the formula.
     * @param formulas All subformulas of the formula.
     */
    public MSOStateVector(int tw, List<Quantifier> quantifiers, List<Formula> formulas) {
        this.logicalObjects = new LinkedList<>();
        this.logicalObjects.addAll(quantifiers); // it is important that quantifier appear in the list before formulas!
        this.logicalObjects.addAll(formulas);
        this.states = new LinkedHashMap<>();
        MSOState initialState = new MSOState(tw);
        this.states.put(initialState, initialState);
    }

    /**
     * Insert the given state into the given set of states. In case the element was already present, take the one that
     * optimization potential optimization quantifier.
     * @param newStates A set of states.
     * @param state The state to be inserted into the set.
     */
    private void secureInsert(Map<MSOState, MSOState> newStates, MSOState state) {
        if (newStates.containsKey(state)) {
            // we already have seen a the same state, minimize the value for optimization quantifier
            if (newStates.get(state).value > state.value) {
                newStates.remove(state);
                newStates.put(state, state);
            }
        } else {
            newStates.put(state, state);
        }
    }

    @Override
    public StateVector<Integer> introduce(Bag<Integer> bag, Integer v, Map<Integer, Integer> treeIndex) {
        int index = treeIndex.get(v);
        if (LOG) System.out.println("introducing " + v + " (" + index + ")");
        for (LogicalObject lo : logicalObjects) {
            if (!lo.requiresIntroduce()) continue;
            Map<MSOState, MSOState> newStates = new LinkedHashMap<>();
            for (MSOState state : states.keySet()) {
                for (MSOState newState : lo.introduce(state, v, index)) secureInsert(newStates, newState);
            }
            this.states = newStates;
        }
        if (LOG) System.out.println(this);
        return this;
    }

    @Override
    public StateVector<Integer> forget(Bag<Integer> bag, Integer v, Map<Integer, Integer> treeIndex) {
        int index = treeIndex.get(v);
        if (LOG) System.out.println("forgetting " + v + " (" + index + ")");
        for (LogicalObject lo : logicalObjects) {
            if (!lo.requiresForget()) continue;
            Map<MSOState, MSOState> newStates = new LinkedHashMap<>();
            for (MSOState state : states.keySet()) {
                for (MSOState newState : lo.forget(state, v, index)) secureInsert(newStates, newState);
            }
            this.states = newStates;
        }
        if (LOG) System.out.println(this);
        return this;
    }

    @Override
    public StateVector<Integer> join(Bag<Integer> bag, StateVector<Integer> stateVector, Map<Integer, Integer> treeIndex) {
        if (LOG) System.out.println("join");
        MSOStateVector oStateVector = (MSOStateVector) stateVector;

        // prepare the other set to be indexed easily
        Map<Integer, List<MSOState>> weakList = new HashMap<>();
        for (MSOState state : oStateVector.states.keySet()) {
            if (!weakList.containsKey(state.weakHashCode())) weakList.put(state.weakHashCode(), new LinkedList<>());
            weakList.get(state.weakHashCode()).add(state);
        }

        // identify states to join
        Map<MSOState, MSOState> newStates = new LinkedHashMap<>();
        for (MSOState state : states.keySet()) {
            if (!weakList.containsKey(state.weakHashCode())) continue; // no partner to join with
            for (MSOState oState : weakList.get(state.weakHashCode())) {
                // we can eventually join state and oState
                if (!state.weakEquals(oState)) continue;
                MSOState newState = new MSOState(state);
                boolean shallAdd = true;
                for (LogicalObject lo : logicalObjects) {
                    shallAdd &= lo.join(state, oState, newState, bag, treeIndex);
                    if (!shallAdd) break;
                }
                if (shallAdd) {
                    joinAssignments(state, oState, newState);
                    secureInsert(newStates, newState);
                }
            }
        }
        this.states = newStates;

        if (LOG) System.out.println(this);
        return this;
    }

    /**
     * Combine the assignment of to (compatible) states.
     * @param stateA The first state.
     * @param stateB The second state.
     * @param newState The resulting state.
     */
    private void joinAssignments(MSOState stateA, MSOState stateB, MSOState newState) {
        for (String R : stateA.assignment.keySet()) {
            if (!newState.assignment.containsKey(R)) newState.assignment.put(R, new BitSet());
            newState.assignment.get(R).or(stateA.assignment.get(R));
        }
        for (String R : stateB.assignment.keySet()) {
            if (!newState.assignment.containsKey(R)) newState.assignment.put(R, new BitSet());
            newState.assignment.get(R).or(stateB.assignment.get(R));
        }
    }

    @Override
    public StateVector<Integer> edge(Bag<Integer> bag, Integer v, Integer w, Map<Integer, Integer> treeIndex) {
        int indexV = treeIndex.get(v);
        int indexW = treeIndex.get(w);
        if (LOG) System.out.println("edge " + v + " " + w + " (" + indexV + ", " + indexW + ")");
        for (LogicalObject lo : logicalObjects) {
            if (!lo.requiresEdge()) continue;
            Map<MSOState, MSOState> newStates = new LinkedHashMap<>();
            for (MSOState state : states.keySet()) {
                for (MSOState newState : lo.edge(state, v, w, indexV, indexW)) secureInsert(newStates, newState);
            }
            this.states = newStates;
        }
        if (LOG) System.out.println(this);
        return this;
    }

    @Override
    public boolean shouldReduce(Bag<Integer> bag, Map<Integer, Integer> treeIndex) {
        return false;
    }

    @Override
    public void reduce(Bag<Integer> bag, Map<Integer, Integer> treeIndex) {
    }

    /**
     * Get a state that satisfies all formulas if one exists, or null otherwise.
     * @return
     */
    public MSOState getSatisfyingState() {
        List<MSOState> delete = new LinkedList<>();
        for (MSOState state : states.keySet()) {
            for (LogicalObject lo : logicalObjects) {
                if (!lo.finalCheck(state)) delete.add(state);
            }
        }
        for (MSOState state : delete) states.remove(state);
        if (states.size() == 0) return null;
        return states.keySet().stream().findFirst().get();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (MSOState state : states.keySet()) sb.append(state + "\n");
        return sb.toString();
    }

    /**
     * A state of the dynamic program, i.\,e., one possible assignment of the vertices of the current bag.
     */
    public class MSOState {

        /** Each logical object is represented in the state description, i.\,e., it has some storage. */
        public Map<LogicalObject, int[]> stateDescription;

        /** We store the assignment, but this is not part of the state description. */
        public Map<String, BitSet> assignment;

        /** The value this state has (with respect to potential optimization quantifiers). */
        public int value;

        /**
         * Initialize a fresh state.
         * @param tw The tree width of the decomposition (not bag size).
         */
        public MSOState(int tw) {
            this.stateDescription = new LinkedHashMap<>();
            this.assignment = new HashMap<>();
            for (LogicalObject lo : logicalObjects) {
                this.stateDescription.put(lo, new int[lo.getStateSize(tw)]);
            }
            this.value = 0;
        }

        /** Copy-Constructor */
        public MSOState(MSOState o) {
            this.stateDescription = new HashMap<>(o.stateDescription.size());
            for (LogicalObject lo : logicalObjects) {
                stateDescription.put(lo, Arrays.copyOf(o.stateDescription.get(lo), o.stateDescription.get(lo).length));
            }
            this.assignment = new HashMap<>();
            for (String R : o.assignment.keySet()) {
                this.assignment.put(R, (BitSet) o.assignment.get(R).clone());
            }
            this.value = o.value;
        }

        /**
         * Alternative for the copy constructor.
         * @return
         */
        public MSOState getCopy() {
            return new MSOState(this);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MSOState that = (MSOState) o;
            for (LogicalObject lo : logicalObjects) {
                if (!Arrays.equals(this.stateDescription.get(lo), that.stateDescription.get(lo))) return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 0;
            int shift = 0;
            for (LogicalObject lo : logicalObjects) {
                shift = (shift + 11) % 21;
                hash ^= (Arrays.hashCode(this.stateDescription.get(lo)) + 1024) << shift;
            }
            return hash;
        }

        /**
         * Two states must not be equal to be joined at a join-bag. However, they must be weakEqual.
         * @param o Another state.
         * @return True, if the two states are weak-equal.
         */
        public boolean weakEquals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MSOState that = (MSOState) o;
            for (LogicalObject lo : logicalObjects) {
                if (lo.requiresWeakJoin()) {
                    if (!lo.weakEquals(this, that)) return false;
                } else {
                    if (!Arrays.equals(this.stateDescription.get(lo), that.stateDescription.get(lo))) return false;
                }
            }
            return true;
        }

        /**
         * A weaker hash-code used for efficient computation at join-bags.
         * @return The weak hash-code.
         */
        public int weakHashCode() {
            int hash = 0;
            int shift = 0;
            for (LogicalObject lo : logicalObjects) {
                shift = (shift + 11) % 21;
                if (lo.requiresWeakJoin()) {
                    hash ^= (lo.weakHashCode(this) + 1024) << shift;
                } else {
                    hash ^= (Arrays.hashCode(this.stateDescription.get(lo)) + 1024) << shift;
                }
            }
            return hash;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(assignment);
            for (LogicalObject lo : logicalObjects) {
                sb.append(lo.toString());
                sb.append(": ");
                sb.append(Arrays.toString(stateDescription.get(lo)));
                sb.append("#");
            }
            sb.replace(sb.length()-1, sb.length(), "");
            return sb.toString();
        }
    }

}
