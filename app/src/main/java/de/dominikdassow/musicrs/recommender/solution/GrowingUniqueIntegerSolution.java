package de.dominikdassow.musicrs.recommender.solution;

import lombok.SneakyThrows;
import org.uma.jmetal.solution.AbstractSolution;
import org.uma.jmetal.solution.Solution;

import java.util.HashMap;

public class GrowingUniqueIntegerSolution
    extends AbstractSolution<Integer>
    implements GrowingSolution<Integer> {

    public GrowingUniqueIntegerSolution(int numberOfVariables, int numberOfObjectives, int numberOfConstraints) {
        super(numberOfVariables, numberOfObjectives, numberOfConstraints);
    }

    public GrowingUniqueIntegerSolution(int numberOfVariables, int numberOfObjectives) {
        this(numberOfVariables, numberOfObjectives, 0);
    }

    public GrowingUniqueIntegerSolution(GrowingUniqueIntegerSolution solution) {
        super(solution.getNumberOfVariables(), solution.getNumberOfObjectives(), solution.getNumberOfConstraints());

        for (int i = 0; i < solution.getNumberOfVariables(); i++) {
            setVariable(i, solution.getVariable(i));
        }

        for (int i = 0; i < solution.getNumberOfObjectives(); i++) {
            setObjective(i, solution.getObjective(i));
        }

        for (int i = 0; i < solution.getNumberOfConstraints(); i++) {
            setConstraint(i, solution.getConstraint(i));
        }

        attributes = new HashMap<>(solution.attributes);
    }

    @SneakyThrows
    @Override
    public void setVariable(int index, Integer value) {
        if (getVariables().contains(value)) {
            throw new IllegalArgumentException("Variable '" + value + "' already exists in solution.");
        }

        super.setVariable(index, value);
    }

    @Override
    public Solution<Integer> copy() {
        return new GrowingUniqueIntegerSolution(this);
    }
}
