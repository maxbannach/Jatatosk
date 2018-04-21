package solver;

import jdrasil.workontd.StateVector;
import jdrasil.workontd.StateVectorFactory;
import logic.formulas.Formula;
import logic.quantifiers.Quantifier;

import java.util.List;

public class MSOStateVectorFactory implements StateVectorFactory<Integer> {

    /** A list of all logic.quantifiers that appear in the formula. */
    private List<Quantifier> quantifiers;

    /** A list of all sub formulas that appear in the formula. */
    private List<Formula> formulas;

    public MSOStateVectorFactory(List<Quantifier> quantifiers, List<Formula> formulas) {
        this.quantifiers = quantifiers;
        this.formulas = formulas;
    }

    @Override
    public StateVector<Integer> createStateVectorForLeaf(int tw) {
        return new MSOStateVector(tw, quantifiers, formulas);
    }

}
