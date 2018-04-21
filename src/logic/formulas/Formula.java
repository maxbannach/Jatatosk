package logic.formulas;

import logic.LogicalObject;
import logic.Structure;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A logical formula that is part of the whole formula we model check on the given logical structure.
 * The formula consists of an atomic CNF part that can be checked against the structure and an assignment, and it provides
 * abstract functions that are specific for the different kinds of formulas the MSO-Fragment contains.
 * These functions can modify some bit storage in an MSOState during the dynamic program and evaluate the formula with respect
 * to these information.
 */
public abstract class Formula implements LogicalObject {

    /** The logical structure on which we evaluate this formula. */
    Structure structure;

    /** Clauses of the underling atomic formula. */
    private List<String> clauses;

    public Formula(Structure structure) {
        this.structure = structure;
        this.clauses = new LinkedList<>();
    }

    /**
     * Add a clause to the formula.
     * @param clause The clause as string (i.e., one line of a .mso file).
     */
    public void addClause(String clause) {
        clause = clause.trim() + " ";
        this.clauses.add(clause);
    }

    /**
     * Checks if the formula is satisfied for the two given variables x and y under the given assignment.
     * @param x Element of the universe assigned to x.
     * @param y Element of the universe assigned to y.
     * @param assignment Representation of quantified second-order variables in the form of bit sets.
     * @return True, if the formula is satisfied.
     */
    public boolean isSatisfied(int x, int y, Map<String, BitSet> assignment) {
        // go through all clauses, if one is not satisfied the whole formula is not satisfied
        clauseloop: for (String clause : clauses) {
            String[] literals = clause.replaceAll(" x ", " "+x+" ").replaceAll(" y ", " "+y+" ").split(" ");
            // go through the literals, if one is satisfied the clause is satisfied and we can continue with the next clause
            for (int i = 0; i < literals.length; i++) {
                // get the relational symbol and check if it is negated
                String R = literals[i];
                boolean negated = false;
                if (R.charAt(0) == '-') {
                    negated = true;
                    R = R.substring(1);
                }

                // catch equality as special case
                if (R.equals("=")) {
                    int first = Integer.parseInt(literals[i+1]);
                    int second = Integer.parseInt(literals[i+2]);
                    i += 2;
                    if (!negated && first == second) continue clauseloop;
                    if (negated && first != second) continue clauseloop;
                    continue;
                }

                // get the arity, if it is negative the relation is not part of the structure and, hence, must be in the assignment
                int arity = structure.getArity(R);
                boolean inStructure = arity >= 0;
                if (!inStructure) arity = 1;

                // from the arity we know how many elements we can parse from the clause
                int[] elements = new int[arity];
                for (int j = 0; j < arity; j++) elements[j] = Integer.parseInt(literals[i+j+1]);
                i += arity;

                // if the relation is in the structure we query it
                if (inStructure && !negated && structure.inRelation(R, elements)) continue clauseloop;
                if (inStructure && negated && !structure.inRelation(R, elements)) continue clauseloop;

                // if not, we know it has arity 1 and we look it up in the assignment
                if (!inStructure && !negated && assignment.containsKey(R) && assignment.get(R).get(elements[0])) continue clauseloop;
                if (!inStructure && negated && (!assignment.containsKey(R) || !assignment.get(R).get(elements[0]))) continue clauseloop;
            }
            return false;
        }
        return true;
    }

//    /**
//     * Usually we perform a hardjoin, which means we only join states that are equal. Some formulas, however, requires weak join,
//     * which are performed between states that are equal up-to the storages, which may differ.
//     * This function indicates whether or not this is the case for this particular formula.
//     *
//     * @return
//     */
//    public abstract boolean requiresWeakJoin();
//
//    /**
//     * Given an introduced element of the universe (and its index) and a storage that can be managed by the formula during the
//     * dynamic programming phase, this function outputs a new modified storage.
//     *
//     * If the current state (in form of the given storage) in invalid for this formula (i.\,e., the formula can not be satisfied anymore),
//     * then null is returned.
//     *
//     * @param v A element of the universe.
//     * @param index The tree index of that element.
//     * @param storage Information managed by the formula.
//     * @param assignment An assignment to the quantified variables.
//     * @return Modified storage, or null if the formula can not be satisfied anymore.
//     */
//    public abstract BitSet introduce(int v, int index, BitSet storage, Map<String, BitSet> assignment);
//
//    /**
//     * Given a forget element of the universe (and its index) and a storage that can be managed by the formula during the
//     * dynamic programming phase, this function outputs a new modified storage.
//     *
//     * If the current state (in form of the given storage) in invalid for this formula (i.\,e., the formula can not be satisfied anymore),
//     * then null is returned.
//     *
//     * @param v A element of the universe.
//     * @param index The tree index of that element.
//     * @param storage Information managed by the formula.
//     * @param assignment An assignment to the quantified variables.
//     * @return Modified storage, or null if the formula can not be satisfied anymore.
//     */
//    public abstract BitSet forget(int v, int index, BitSet storage, Map<String, BitSet> assignment);
//
//    /**
//     * Given an edge consisting of two elements of the universe (and its index) and a storage that can be managed by the formula during the
//     * dynamic programming phase, this function outputs a new modified storage.
//     *
//     * If the current state (in form of the given storage) in invalid for this formula (i.\,e., the formula can not be satisfied anymore),
//     * then null is returned.
//     *
//     * @param v A element of the universe.
//     * @param w A element of the universe.
//     * @param indexV The tree index of v.
//     * @param indexW The tree index of w.
//     * @param storage Information managed by the formula.
//     * @param assignment An assignment to the free variables.
//     * @return Modified storage, or null if the formula can not be satisfied anymore.
//     */
//    public abstract BitSet edge(int v, int w, int indexV, int indexW, BitSet storage, Map<String, BitSet> assignment);
//
//    /**
//     * Given to storages of two states that meet at a join bag, this function computes the combined storage. The function
//     * is guaranteed to not return null.
//     *
//     * @param storageA
//     * @param storageB
//     * @param assignment
//     * @return
//     */
//    public abstract BitSet join(BitSet storageA, BitSet storageB, Map<String, BitSet> assignment);


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String clause : clauses) {
            sb.append(clause);
            sb.append("\n");
        }
        sb.replace(sb.length()-1,sb.length(),"");
        return sb.toString();
    }

}
