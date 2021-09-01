package de.dominikdassow.musicrs.study;

import org.uma.jmetal.lab.experiment.Experiment;
import org.uma.jmetal.lab.experiment.component.ExperimentComponent;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.front.Front;
import org.uma.jmetal.util.front.impl.ArrayFront;
import org.uma.jmetal.util.front.util.FrontUtils;
import org.uma.jmetal.util.point.PointSolution;
import org.uma.jmetal.util.solutionattribute.impl.GenericSolutionAttribute;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class GenerateReferenceParetoFront
    implements ExperimentComponent {

    private final Experiment<?, ?> experiment;

    public GenerateReferenceParetoFront(Experiment<?, ?> experiment) {
        this.experiment = experiment;

        this.experiment.removeDuplicatedAlgorithms();
    }

    @Override
    public void run() throws IOException {
        prepareDirectory(experiment.getReferenceFrontDirectory());

        experiment.getProblemList().forEach(problem -> {
            List<PointSolution> solutions = new ArrayList<>();

            experiment.getAlgorithmList().forEach(algorithm -> {
                String directory = experiment.getExperimentBaseDirectory()
                    + "/data/" + algorithm.getAlgorithmTag() + "/" + algorithm.getProblemTag();

                IntStream.range(0, experiment.getIndependentRuns()).forEach(run -> {
                    String paretoFrontFile = directory
                        + "/" + experiment.getOutputParetoFrontFileName() + run + ".csv";

                    try {
                        Front front = new ArrayFront(paretoFrontFile, ",");

                        GenericSolutionAttribute<PointSolution, String> solutionAttribute
                            = new GenericSolutionAttribute<>();

                        FrontUtils.convertFrontToSolutionList(front).forEach(solution -> {
                            solutionAttribute.setAttribute(solution, algorithm.getAlgorithmTag());
                            solutions.add(solution);
                        });
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                });
            });

            String problemReferenceFrontFile = experiment.getReferenceFrontDirectory()
                + "/" + problem.getTag() + ".csv";

            new SolutionListOutput(solutions)
                .printObjectivesToFile(problemReferenceFrontFile, ",");

            // TODO: Need a reference front per problem/algorithm?
//            experiment.getAlgorithmList().forEach(algorithm -> {
//                String algorithmReferenceFrontFile = experiment.getReferenceFrontDirectory()
//                    + "/" + problem.getTag() + "." + algorithm.getAlgorithmTag() + ".csv";
//
//                new SolutionListOutput(solutions)
//                    .printObjectivesToFile(algorithmReferenceFrontFile, ",");
//            });
        });
    }

    protected void prepareDirectory(String directoryPath) {
        File directory = new File(directoryPath);

        if (!directory.exists() || !directory.isDirectory()) {
            boolean wasCreated = new File(directoryPath).mkdirs();

            if (!wasCreated) throw new JMetalException("Error while creating the directory: " + directoryPath);
        }
    }
}
