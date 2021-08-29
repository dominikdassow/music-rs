package de.dominikdassow.musicrs.recommender.algorithm.aco.maco.util;

import de.dominikdassow.musicrs.recommender.algorithm.aco.maco.MACO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
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

    protected EnumeratedDistribution<T> candidateDistribution;

    public Colony(MACO<S, T> algorithm) {
        this.algorithm = algorithm;

        IntStream.range(0, algorithm.getNumberOfAnts())
            .forEach(i -> ants.add(new Ant<>(algorithm, this)));
    }

    public void createSolutions() {
        log.info("Colony::createSolutions(), ants=" + ants.size());
        ants.forEach(ant -> solutions.add(ant.createSolution()));
        log.info("--- Colony::createSolutions()");
    }

    public void reset() {
        solutions.clear();
    }

    public Collection<S> getBestSolutions() {
        return bestSolutions.values();
    }

    public abstract void initPheromoneTrails();

    public abstract void updatePheromoneTrails();

    protected abstract double getPheromoneFactor(List<T> partialSolution, T candidate);

    protected abstract double getHeuristicFactor(S solution);

    protected EnumeratedDistribution<T> getCandidateDistribution(List<T> partialSolution, List<T> candidates) {
        log.info("Colony::getCandidateDistribution(), candidates=" + candidates.size() + ", partialSolution=" + partialSolution.size());

        Map<T, Double> pheromoneFactors = candidates.stream()
            .collect(Collectors.toMap(Function.identity(),
                candidate -> algorithm.applyPheromoneFactorsWeight(getPheromoneFactor(partialSolution, candidate))));

        Map<T, Double> heuristicFactors = candidates.stream()
            .collect(Collectors.toMap(Function.identity(),
                candidate -> algorithm.applyHeuristicFactorsWeight(getHeuristicFactor(partialSolution, candidate))));

        double summedCandidatesFactor = candidates.stream()
            .mapToDouble(candidate -> pheromoneFactors.get(candidate) * heuristicFactors.get(candidate))
            .sum();

        Map<T, Double> probabilities = candidates.stream()
            .collect(Collectors.toMap(Function.identity(),
                candidate -> (pheromoneFactors.get(candidate) * heuristicFactors.get(candidate))
                    / summedCandidatesFactor));

        List<Pair<T, Double>> candidatesWithProbability = candidates.stream()
            .map(candidate -> new Pair<>(candidate, probabilities.get(candidate)))
            .collect(Collectors.toList());

        return new EnumeratedDistribution<>(candidatesWithProbability);
    }

    protected double getHeuristicFactor(List<T> partialSolution, T candidate) {
        S solution = algorithm.getProblem().createSolution();

        for (int i = 0; i < solution.getNumberOfVariables(); i++) {
            solution.setVariable(i, null);
        }

        for (int i = 0; i < partialSolution.size(); i++) {
            solution.setVariable(i, partialSolution.get(i));
        }

        solution.setVariable(partialSolution.size(), candidate);

        algorithm.getProblem().evaluate(solution);

        return getHeuristicFactor(solution);
    }

    protected void updatePossibleBestSolution(int objective, S solution) {
        if (solution.getObjective(objective) > bestSolutions.get(objective).getObjective(objective)) {
            bestSolutions.put(objective, solution);
        }
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
        protected double getPheromoneFactor(List<T> partialSolution, T candidate) {
            return pheromoneTrail.get(candidate);
        }

        @Override
        protected double getHeuristicFactor(S solution) {
            return solution.getObjective(objective);
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
            protected double getPheromoneFactor(List<T> partialSolution, T candidate) {
                return pheromoneTrail.get(candidate);
            }

            @Override
            protected double getHeuristicFactor(S solution) {
                return IntStream.range(0, solution.getNumberOfObjectives())
                    .mapToDouble(solution::getObjective)
                    .sum();
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
            protected double getPheromoneFactor(List<T> partialSolution, T candidate) {
                switch (aggregation) {
                    case RANDOM:
                        return pheromoneTrails
                            .get(JMetalRandom.getInstance().nextInt(0, pheromoneTrails.size()))
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
            protected double getHeuristicFactor(S solution) {
                return IntStream.range(0, solution.getNumberOfObjectives())
                    .mapToDouble(solution::getObjective)
                    .sum();
            }
        }
    }
}
