package de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util;

import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.MACO;
import lombok.extern.slf4j.Slf4j;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.comparator.ObjectiveComparator;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public abstract class Colony<S extends Solution<T>, T> {

    protected final MACO<S, T> algorithm;

    protected final List<Ant<S, T>> ants = new ArrayList<>();
    protected final List<S> solutions = new ArrayList<>();
    protected final Map<Integer, S> bestSolutions = new HashMap<>();

    public Colony(MACO<S, T> algorithm) {
        this.algorithm = algorithm;

        IntStream.range(0, algorithm.getNumberOfAnts())
            .forEach(i -> ants.add(new Ant<>(algorithm, this)));
    }

    public void createSolutions() {
        ants.parallelStream().forEach(ant -> solutions.add(ant.createSolution()));
    }

    public void reset() {
        solutions.clear();
    }

    public Collection<S> getBestSolutions() {
        return bestSolutions.values();
    }

    public abstract void initPheromoneTrails();

    public abstract void updatePheromoneTrails();

    protected abstract double getPheromoneFactor(S solution, T candidate);

    protected abstract double getHeuristicFactor(S solution, T candidate);

    protected Map<T, Double> getPheromoneFactors(S solution) {
        return solution.getVariables().stream().collect(Collectors.toMap(
            Function.identity(),
            candidate -> algorithm.applyPheromoneFactorsWeight(getPheromoneFactor(solution, candidate)))
        );
    }

    protected Map<T, Double> getHeuristicFactors(S solution) {
        return solution.getVariables().stream().collect(Collectors.toMap(
            Function.identity(),
            candidate -> algorithm.applyHeuristicFactorsWeight(getHeuristicFactor(solution, candidate))
        ));
    }

    protected void updatePossibleBestSolution(int objective, S solution) {
        boolean update = !bestSolutions.containsKey(objective) ||
            solution.getObjective(objective) > bestSolutions.get(objective).getObjective(objective);

        if (update) bestSolutions.put(objective, solution);
    }

    public static class SingleObjective<S extends Solution<T>, T>
        extends Colony<S, T> {

        private final int objective;
        private final PheromoneTrail<S, T> pheromoneTrail;
        private final Comparator<S> solutionComparator;

        public SingleObjective(MACO<S, T> algorithm, int objective, PheromoneTrail<S, T> pheromoneTrail) {
            super(algorithm);

            this.objective = objective;
            this.pheromoneTrail = pheromoneTrail;

            solutionComparator = new ObjectiveComparator<>(objective);
        }

        @Override
        public void initPheromoneTrails() {
            pheromoneTrail.init();
        }

        @Override
        public void updatePheromoneTrails() {
            S bestSolution
                = SolutionListUtils.findBestSolution(solutions, solutionComparator);

            updatePossibleBestSolution(objective, bestSolution);

            pheromoneTrail.update((component, value) -> {
                value = algorithm.applyEvaporationFactor(value);

                if (bestSolution.getVariables().contains(component)) {
                    value += 1 / (1 + bestSolution.getObjective(objective)
                        - bestSolutions.get(objective).getObjective(objective));
                }

                return value;
            });
        }

        @Override
        protected double getPheromoneFactor(S solution, T candidate) {
            return pheromoneTrail.get(candidate);
        }

        @Override
        protected double getHeuristicFactor(S solution, T candidate) {
            return solution.getObjective(objective) / solution.getNumberOfVariables();
        }
    }

    public static abstract class MultiObjective<S extends Solution<T>, T>
        extends Colony<S, T> {

        public MultiObjective(MACO<S, T> algorithm) {
            super(algorithm);
        }

        public static class SinglePheromoneTrail<S extends Solution<T>, T>
            extends Colony.MultiObjective<S, T> {

            private final PheromoneTrail<S, T> pheromoneTrail;

            public SinglePheromoneTrail(MACO<S, T> algorithm, PheromoneTrail<S, T> pheromoneTrail) {
                super(algorithm);

                this.pheromoneTrail = pheromoneTrail;
            }

            @Override
            public void initPheromoneTrails() {
                pheromoneTrail.init();
            }

            @Override
            public void updatePheromoneTrails() {
                IntStream.range(0, algorithm.getProblem().getNumberOfObjectives()).forEach(objective -> {
                    List<S> nonDominatedSolutions
                        = SolutionListUtils.getNonDominatedSolutions(solutions);

                    nonDominatedSolutions
                        .forEach(solution -> updatePossibleBestSolution(objective, solution));

                    List<T> candidatesToReward = nonDominatedSolutions
                        .stream().flatMap(nonDominatedSolution -> nonDominatedSolution.getVariables().stream())
                        .collect(Collectors.toList());

                    pheromoneTrail.update((candidate, value) -> {
                        value = algorithm.applyEvaporationFactor(value);

                        if (candidatesToReward.contains(candidate)) {
                            value += 1;
                        }

                        return value;
                    });
                });
            }

            @Override
            protected double getPheromoneFactor(S solution, T candidate) {
                return pheromoneTrail.get(candidate);
            }

            @Override
            protected double getHeuristicFactor(S solution, T candidate) {
                double factor = IntStream.range(0, solution.getNumberOfObjectives())
                    .mapToDouble(solution::getObjective)
                    .sum();

                return factor / solution.getNumberOfVariables();
            }
        }

        public static class MultiplePheromoneTrails<S extends Solution<T>, T>
            extends Colony.MultiObjective<S, T> {

            private final List<PheromoneTrail<S, T>> pheromoneTrails;
            private final MACO.PheromoneFactorAggregation aggregation;

            public MultiplePheromoneTrails(
                MACO<S, T> algorithm,
                List<PheromoneTrail<S, T>> pheromoneTrails,
                MACO.PheromoneFactorAggregation aggregation
            ) {
                super(algorithm);

                this.pheromoneTrails = pheromoneTrails;
                this.aggregation = aggregation;
            }

            @Override
            public void initPheromoneTrails() {
                pheromoneTrails.forEach(PheromoneTrail::init);
            }

            @Override
            public void updatePheromoneTrails() {
                IntStream.range(0, algorithm.getProblem().getNumberOfObjectives()).forEach(objective -> {
                    S bestSolution
                        = SolutionListUtils.findBestSolution(solutions, new ObjectiveComparator<>(objective));

                    updatePossibleBestSolution(objective, bestSolution);

                    pheromoneTrails.get(objective).update((candidate, value) -> {
                        value = algorithm.applyEvaporationFactor(value);

                        if (bestSolution.getVariables().contains(candidate)) {
                            value += 1 / (1 + bestSolution.getObjective(objective)
                                - bestSolutions.get(objective).getObjective(objective));
                        }

                        return value;
                    });
                });
            }

            @Override
            protected double getPheromoneFactor(S solution, T candidate) {
                switch (aggregation) {
                    case RANDOM:
                        return pheromoneTrails
                            .get(JMetalRandom.getInstance().nextInt(0, pheromoneTrails.size() - 1))
                            .get(candidate);
                    case SUMMED:
                        return pheromoneTrails.stream()
                            .mapToDouble(pheromoneTrail -> pheromoneTrail.get(candidate))
                            .sum();
                    default:
                        throw new IllegalStateException("Unexpected value: " + aggregation);
                }
            }

            @Override
            protected double getHeuristicFactor(S solution, T candidate) {
                double factor = IntStream.range(0, solution.getNumberOfObjectives())
                    .mapToDouble(solution::getObjective)
                    .sum();

                return factor / solution.getNumberOfVariables();
            }
        }
    }
}
