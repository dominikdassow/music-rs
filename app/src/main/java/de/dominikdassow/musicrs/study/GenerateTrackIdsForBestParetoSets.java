package de.dominikdassow.musicrs.study;

import de.dominikdassow.musicrs.recommender.MusicPlaylistContinuationProblem;
import org.uma.jmetal.lab.experiment.Experiment;
import org.uma.jmetal.lab.experiment.component.ExperimentComponent;
import org.uma.jmetal.lab.experiment.util.ExperimentAlgorithm;
import org.uma.jmetal.qualityindicator.impl.GenericIndicator;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GenerateTrackIdsForBestParetoSets
    implements ExperimentComponent {

    private final Experiment<?, ?> experiment;

    public GenerateTrackIdsForBestParetoSets(Experiment<?, ?> experiment) {
        this.experiment = experiment;
    }

    @Override
    public void run() throws IOException {
        experiment.getProblemList().forEach(experimentProblem -> {
            final MusicPlaylistContinuationProblem problem;

            try {
                problem = (MusicPlaylistContinuationProblem) experimentProblem.getProblem();
            } catch (ClassCastException e) {
                JMetalLogger.logger.severe("Experiment problem " +
                    "with tag '" + experimentProblem.getTag() + "' " +
                    "must be of type '" + MusicPlaylistContinuationProblem.class.getName() + "'.");

                return;
            }

            experiment.getAlgorithmList().stream()
                .map(ExperimentAlgorithm::getAlgorithmTag)
                .distinct()
                .forEach(algorithmTag -> {
                    final String directory = experiment.getExperimentBaseDirectory()
                        + "/data/" + algorithmTag + "/" + experimentProblem.getTag();

                    experiment.getIndicatorList()
                        .forEach(indicator -> write(problem, indicator, directory));
                });
        });
    }

    private void write(MusicPlaylistContinuationProblem problem, GenericIndicator<?> indicator, String directory) {
        final String bestParetoSetsFileName = directory
            + "/BEST_" + indicator.getName() + "_" + experiment.getOutputParetoSetFileName() + ".csv";

        final String bestTrackIdsFileName = directory
            + "/BEST_" + indicator.getName() + "_TRACKS.csv";

        if (!(new File(bestParetoSetsFileName).isFile())) {
            JMetalLogger.logger.severe("Best pareto sets file does not exist. " + bestParetoSetsFileName);

            return;
        } else if (new File(bestTrackIdsFileName).isFile()) {
            JMetalLogger.logger.info("Best track ids file does already exist. " + bestTrackIdsFileName);

            return;
        }

        BufferedWriter writer
            = new DefaultFileOutputContext(bestTrackIdsFileName, ",").getFileWriter();

        try (Stream<String> paretoSets = Files.lines(Paths.get(bestParetoSetsFileName))) {
            List<String> trackIdsList = paretoSets
                .map(set -> Arrays.stream(set.split(","))
                    .mapToInt(Integer::parseInt)
                    .boxed()
                    .collect(Collectors.toList())
                )
                .map(problem::getTrackIds)
                .map(ids -> String.join(",", ids))
                .collect(Collectors.toList());

            for (String trackIds : trackIdsList) {
                writer.write(trackIds);
                writer.newLine();
            }

            writer.close();

            JMetalLogger.logger.info("Successfully written best track ids to file: " + bestTrackIdsFileName);
        } catch (IOException e) {
            JMetalLogger.logger.severe("Cannot read from file: " + bestParetoSetsFileName);
            e.printStackTrace();
        }
    }
}
