import jdrasil.algorithms.SmartDecomposer;
import jdrasil.graph.TreeDecomposition;
import jdrasil.workontd.DynamicProgrammingOnTreeDecomposition;
import logic.Structure;
import logic.formulas.*;
import logic.quantifiers.*;
import solver.MSOStateVector;
import solver.MSOStateVectorFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Main {

    /** The logical structure on which we perform model checking. */
    private Structure structure;

    /** All logic.quantifiers that appear in the formula. */
    private List<Quantifier> quantifiers;

    /** All sub-formulas of the formula. */
    private List<Formula> formulas;

    public static void main(String[] args) {
        Main app = new Main();
        app.run();
    }

    public Main() {
        this.quantifiers = new LinkedList<>();
        this.formulas = new LinkedList<>();
    }

    private void run() {
        parseStdIn();
        solve();
    }

    private void parseStdIn() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            String line;
            String[] ll;
            while ( (line = in.readLine()) != null ) {
                ll = line.split(" ");

                /* skip comment lines */
                if (ll[0].equals("c")) continue;

                /* line is >vocabulary <R_i> <arity(R_i)> < and we will fill the structure with life */
                if (ll[0].equals("vocabulary")) {
                    this.structure = new Structure();
                    for (int i = 1; i < ll.length; i += 2) {
                        String R = ll[i];
                        int arity = Integer.parseInt(ll[i+1]);
                        structure.addRelation(R, arity);
                    }
                    continue;
                }
                if (this.structure == null) throw new Exception("First declaration has to be the vocabulary!");

                /* line >structure n m< */
                if (ll[0].equals("structure")) {
                    int n = Integer.parseInt(ll[1]);
                    int m = Integer.parseInt(ll[2]);
                    this.structure.initializeUniverse(n);
                    for (int j = 0; j < m; j++) {
                        line = in.readLine();
                        String[] relation = line.split(" ");
                        if (relation[0].equals("c")) { j--; continue; }
                        int[] elements = new int[relation.length-1];
                        for (int k = 1; k < relation.length; k++) elements[k-1] = Integer.parseInt(relation[k]);
                        structure.setInRelation(relation[0], elements);
                    }
                    continue;
                }
                if (this.structure.getUniverseSize() < 0) throw new Exception("Second declaration has to be the structure!");

                /* parse and minimization quantifier */
                if (ll[0].equals("min")) {
                    Quantifier Q = new OptimizationQuantifier(ll);
                    quantifiers.add(Q);
                    continue;
                }

                /* parse a partition quantifier */
                if (ll[0].equals("exists")) {
                    Quantifier Q = new PartitionQuantifier(ll);
                    quantifiers.add(Q);
                    continue;
                }

                /* parse a connected quantifier */
                if (ll[0].equals("connected")) {
                    Quantifier Q = new ConnectedQuantifier(ll);
                    quantifiers.add(Q);
                    continue;
                }

                /* parse a forest quantifier */
                if (ll[0].equals("forest")) {
                    Quantifier Q = new ForestQuantifier(ll);
                    quantifiers.add(Q);
                    continue;
                }

                /* parse an AxAy formula */
                if (ll[0].equals("axay")) {
                    Formula psi = new AxAyFormula(structure);
                    readClauses(psi, Integer.parseInt(ll[1]), in);
                    formulas.add(psi);
                    continue;
                }

                /* parse an AxEy formula */
                if (ll[0].equals("axey")) {
                    Formula psi = new AxEyFormula(structure);
                    readClauses(psi, Integer.parseInt(ll[1]), in);
                    formulas.add(psi);
                    continue;
                }

                /* parse an ExEy formula */
                if (ll[0].equals("exey")) {
                    Formula psi = new ExEyFormula(structure);
                    readClauses(psi, Integer.parseInt(ll[1]), in);
                    formulas.add(psi);
                    continue;
                }

                /* parse an ExAy formula */
                if (ll[0].equals("exay")) {
                    Formula psi = new ExAyFormula(structure);
                    readClauses(psi, Integer.parseInt(ll[1]), in);
                    formulas.add(psi);
                    continue;
                }

                /* parse an Ax formula */
                if (ll[0].equals("ax")) {
                    Formula psi = new AxFormula(structure);
                    readClauses(psi, Integer.parseInt(ll[1]), in);
                    formulas.add(psi);
                    continue;
                }

                /* parse an Ex formula */
                if (ll[0].equals("ex")) {
                    Formula psi = new ExFormula(structure);
                    readClauses(psi, Integer.parseInt(ll[1]), in);
                    formulas.add(psi);
                }
            }
            in.close();
        } catch (IOException e) {
            System.err.println("Error while reading the input.");
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /** Read clauses from stdin. */
    private void readClauses(Formula psi, int m, BufferedReader in) throws IOException {
        String line;
        for (int i = 0; i < m; i++) {
            line = in.readLine();
            if (line.charAt(0) == 'c' && line.charAt(1) == ' ') { i--; continue; }
            psi.addClause(line);
        }
    }

    private void solve() {
        System.out.println("|V| = " + structure.getGraph().getNumVertices() + ", |E| = " + structure.getGraph().getNumberOfEdges() + ", cc(G) = " + structure.getGraph().getConnectedComponents().size());

        // compute the tree decomposition
        TreeDecomposition<Integer> td = null;
        try {
            td = new SmartDecomposer<>(structure.getGraph()).call();
        } catch (Exception e) {
            System.err.println("Failed to compute tree decomposition.");
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("tw(G) = " + td.getWidth());

        MSOStateVectorFactory factory = new MSOStateVectorFactory(quantifiers, formulas);
        DynamicProgrammingOnTreeDecomposition<Integer> solver = new DynamicProgrammingOnTreeDecomposition<>(structure.getGraph(), factory, true, td);
        MSOStateVector solution = (MSOStateVector) solver.run();
        MSOStateVector.MSOState state = solution.getSatisfyingState();
        if (state == null) {
            System.out.println("Not a model!");
        } else {
            System.out.println("Found a solution of value: " + state.value + ".");
            System.out.println(state.assignment);
        }
    }

}