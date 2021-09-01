package de.dominikdassow.musicrs.recommender.problem;

import de.dominikdassow.musicrs.recommender.solution.GrowingSolution;
import org.uma.jmetal.problem.Problem;

import java.util.List;

public interface GrowingProblem<S extends GrowingSolution<T>, T>
    extends Problem<S> {

    List<T> getCandidates();

    double evaluate(T candidate, int objective);

    double applyObjectiveValueNormalization(int objective, double value);

    boolean isCandidateRewardedInSolution(T candidate, S solution);
}
