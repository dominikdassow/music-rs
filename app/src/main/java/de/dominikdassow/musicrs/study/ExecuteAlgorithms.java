package de.dominikdassow.musicrs.study;

import org.uma.jmetal.lab.experiment.Experiment;
import org.uma.jmetal.lab.experiment.component.ExperimentComponent;
import org.uma.jmetal.lab.experiment.util.ExperimentAlgorithm;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.JMetalLogger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class ExecuteAlgorithms<S extends Solution<?>, Result extends List<S>>
    implements ExperimentComponent {

    private final Experiment<S, Result> experiment;
    private final int maxRetries;

    public ExecuteAlgorithms(Experiment<S, Result> experiment, int maxRetries) {
        this.experiment = experiment;
        this.maxRetries = maxRetries;
    }

    @Override
    public void run() {
        prepareDirectory(experiment.getExperimentBaseDirectory());
        prepareDirectory(experiment.getExperimentBaseDirectory() + "/executionTime");

        boolean finished = false;
        int currentTry = 0;

        while (!finished && currentTry < maxRetries) {
            List<ExperimentAlgorithm<?, ?>> pendingAlgorithmExecutions = getPendingAlgorithmExecutions();

            if (pendingAlgorithmExecutions.isEmpty()) {
                finished = true;
                continue;
            }

            JMetalLogger.logger.info("ExecuteAlgorithms: " +
                "there are " + pendingAlgorithmExecutions.size() + " executions pending");

            pendingAlgorithmExecutions.parallelStream().forEach(execution -> {
                long start = System.currentTimeMillis();
                execution.runAlgorithm(experiment);
                writeAlgorithmExecutionTime(execution, System.currentTimeMillis() - start);
            });

            currentTry++;
        }

        if (finished) {
            JMetalLogger.logger.info("Algorithm runs finished. Number of tries: " + currentTry);
        } else {
            JMetalLogger.logger.severe("There are unfinished executions after " + maxRetries + " tries");
        }
    }

    protected List<ExperimentAlgorithm<?, ?>> getPendingAlgorithmExecutions() {
        List<ExperimentAlgorithm<?, ?>> pendingAlgorithmExecutions = new LinkedList<>();

        experiment.getAlgorithmList().forEach(algorithm -> {
            String directory = experiment.getExperimentBaseDirectory()
                + "/data/" + algorithm.getAlgorithmTag() + "/" + algorithm.getProblemTag();

            String paretoFrontFile = directory
                + "/" + experiment.getOutputParetoFrontFileName() + algorithm.getRunId() + ".csv";
            String paretoSetFile = directory
                + "/" + experiment.getOutputParetoSetFileName() + algorithm.getRunId() + ".csv";

            if (!(new File(paretoFrontFile).isFile()) || !(new File(paretoSetFile).isFile())) {
                JMetalLogger.logger.info("Pending execution: "
                    + algorithm.getAlgorithmTag() + "/" + algorithm.getProblemTag() + "/" + algorithm.getRunId());

                pendingAlgorithmExecutions.add(algorithm);
            }
        });

        return pendingAlgorithmExecutions;
    }

    protected void prepareDirectory(String directoryPath) {
        File directory = new File(directoryPath);

        if (!directory.exists() || !directory.isDirectory()) {
            if (directory.exists() && !directory.delete()) {
                JMetalLogger.logger.warning("Error while deleting the directory: " + directoryPath);
            }

            boolean wasCreated = new File(directoryPath).mkdirs();

            if (!wasCreated) throw new JMetalException("Error while creating the directory: " + directoryPath);
        }
    }

    protected void writeAlgorithmExecutionTime(ExperimentAlgorithm<?, ?> execution, long time) {
        String file = experiment.getExperimentBaseDirectory() + "/executionTime/"
            + execution.getProblemTag() + "." + execution.getAlgorithmTag() + "." + execution.getRunId() + ".txt";

        try (FileWriter writer = new FileWriter(file, false)) {
            writer.write(String.valueOf(time));
        } catch (IOException e) {
            JMetalLogger.logger.warning("Cannot write algorithm execution time (" + time + "ms) to file: "
                + file);
        }
    }
}
