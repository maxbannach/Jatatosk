package logic;

import jdrasil.graph.Graph;
import jdrasil.graph.GraphFactory;

import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

/**
 * A logical structure that is guaranteed to contain a binary symmetric edge relation and which eventually contains other
 * relations as well.
 */
public class Structure {

    /** Universe is 0,1,...,n-1, where n is this value. */
    private int universeSize;

    /** Every structure we consider has to have an edge relation E. */
    private Graph<Integer> E;

    /** Other relations then E are just stored as bitset. */
    private Map<String, BitSet> relations;

    /** Arity of each relation present in the structure. */
    private Map<String, Integer> arity;

    /**
     * Create an empty structure of the given universe size, where the universe will be 0,1,2,... .
     * By initialization, there is an empty binary, symmetric relation E that encodes a graph.
     * @param universeSize
     */
    public Structure(int universeSize) {
        this.universeSize = universeSize;
        this.E = GraphFactory.emptyGraph();
        for (int v = 0; v < universeSize; v++) this.E.addVertex(v);
        this.relations = new HashMap<>();
        this.arity = new HashMap<>();
    }

    /**
     * Returns the size of the universe of the structure.
     * @return
     */
    public int getUniverseSize() {
        return this.universeSize;
    }

    /**
     * Add a relation of given arity to the structure (empty by initialization).
     * @param R
     * @param arity
     */
    public void addRelation(String R, int arity) {
        if (R.equals("E")) return;
        this.relations.put(R, new BitSet());
        this.arity.put(R, arity);
    }

    /**
     * Returns the arity of the requested relation. -1 If the relation is not present in the structure.
     * @param R The relation to query.
     * @return The arity of R, or -1 if R is not in the vocabulary.
     */
    public int getArity(String R) {
        if (R.equals("E")) return 2;
        if (arity.containsKey(R)) return arity.get(R);
        return -1;
    }

    /**
     * Test if the given elements of the universe are in the given relation. It is assumed that R is in fact a relation of
     * this structure and that the number of elements matches the arity of R.
     * @param R
     * @param elements
     * @return
     */
    public boolean inRelation(String R, int... elements) {
        if (R.equals("E")) return E.isAdjacent(elements[0], elements[1]);
        return this.relations.get(R).get(getIndex(elements));
    }

    /**
     * Sets elements in the given relation. It is assumed that R is in fact a relation present in the structure, and that
     * the number of elements matches the arity of R.
     *
     * All relations are assumed to be orderd, unless E which is assumed to be symmetric and will be stored in a symmetric way.
     *
     * @param R
     * @param elements
     */
    public void setInRelation(String R, int... elements) {
        if (R.equals("E")) { E.addEdge(elements[0], elements[1]); return; }
        this.relations.get(R).set(getIndex(elements));
    }

    /**
     * Given a set of elements, this computes the index in a bit set that represents this relation. In fact, the index
     * is e_0 * n^0 + e_1 * n + e_2 * n^2 + ...
     * @param elements
     * @return
     */
    private int getIndex(int... elements) {
        int index = 0;
        for (int i = 0; i < elements.length; i++) index += elements[i] * Math.pow(universeSize, i);
        return index;
    }

    /**
     * Obtain a graph object that represents the relation E.
     * @return
     */
    public Graph<Integer> getGraph() {
        return E;
    }

    /** Next assignment of elements (i.\,e, n-nary counting). */
    private boolean next(int[] elements) {
        for (int i = 0; i < elements.length; i++) {
            elements[i]++;
            if (elements[i] == universeSize) {
                elements[i] = 0;
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(E.toString());
        sb.delete(0, sb.indexOf("\n")+1);
        sb.insert(0, "E of arity 2:\n");
        for (String R : relations.keySet()) {
            int arity = this.arity.get(R);
            sb.append(R + " of arity " + arity + ":\n");
            int[] elements = new int[arity];
            do {
                if (inRelation(R, elements)) sb.append(Arrays.toString(elements) + "\n");
            } while (next(elements));
        }
        sb.delete(sb.length()-1, sb.length());
        return sb.toString();
    }
}
