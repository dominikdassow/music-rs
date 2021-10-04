package de.dominikdassow.musicrs.study;

import de.dominikdassow.musicrs.AppConfiguration;
import de.dominikdassow.musicrs.recommender.MusicPlaylistContinuationProblem;
import de.dominikdassow.musicrs.service.DatabaseService;
import org.uma.jmetal.lab.experiment.Experiment;
import org.uma.jmetal.lab.experiment.component.ExperimentComponent;
import org.uma.jmetal.lab.experiment.util.ExperimentAlgorithm;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class GenerateRecSysChallengeSubmission
    implements ExperimentComponent {

    private final Experiment<?, ?> experiment;

    private Map<Integer, Map<Integer, String>> newPlaylists;

    public GenerateRecSysChallengeSubmission(Experiment<?, ?> experiment) {
        this.experiment = experiment;
    }

    @Override
    public void run() throws IOException {
        try {
            String newStoreFile = "{data}/store/{version}/playlist.csv"
                .replace("{data}", AppConfiguration.get().dataDirectory)
                .replace("{version}", AppConfiguration.get().storeVersion + "__NEW");

            final List<String[]> idsList = Files.lines(Paths.get(newStoreFile))
                .map(data -> data.split(DatabaseService.DELIMITER))
                .collect(Collectors.toList());

            newPlaylists = new HashMap<>() {{
                idsList.forEach(ids -> put(
                    Integer.parseInt(ids[0]),
                    // Start at index 1 to ignore the playlist_id
                    IntStream.range(1, ids.length)
                        .boxed()
                        .filter(i -> !ids[i].isEmpty())
                        .collect(Collectors.toMap(Function.identity(), i -> ids[i])))
                );
            }};
        } catch (IOException e) {
            JMetalLogger.logger.severe(e.getMessage());
            e.printStackTrace();

            return;
        }

        String submissionDirectory = experiment.getExperimentBaseDirectory() + "/submission";

        prepareDirectory(submissionDirectory);

        Map<String, Map<Integer, List<String>>> submissions = new HashMap<>();

        experiment.getProblemList().forEach(experimentProblem -> {
            MusicPlaylistContinuationProblem<?> problem;
            Integer problemPlaylist = Integer.parseInt(experimentProblem.getTag().split("_")[1]);

            try {
                problem = (MusicPlaylistContinuationProblem<?>) experimentProblem.getProblem();
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

                    submissions.putIfAbsent(algorithmTag, new HashMap<>());

                    experiment.getIndicatorList().forEach(indicator -> {
                        final String bestParetoSetsFileName = directory
                            + "/BEST_" + indicator.getName() + "_" + experiment.getOutputParetoSetFileName() + ".csv";

                        if (!(new File(bestParetoSetsFileName).isFile())) {
                            JMetalLogger.logger.severe("Best pareto sets file does not exist. " + bestParetoSetsFileName);

                            return;
                        }

                        try (Stream<String> paretoSets = Files.lines(Paths.get(bestParetoSetsFileName))) {
                            List<List<String>> trackIdsList = paretoSets
                                .map(set -> Arrays.stream(set.split(","))
                                    .mapToInt(Integer::parseInt)
                                    .boxed()
                                    .collect(Collectors.toList())
                                )
                                .map(problem::getTrackIds)
                                .collect(Collectors.toList());

                            JMetalRandom.getInstance().setSeed(AppConfiguration.get().submissionRandomSeed);

                            List<String> randomNonDominatedSolution = null;
                            List<Integer> randomIndexes = new LinkedList<>() {{
                                IntStream.range(0, trackIdsList.size()).boxed().forEach(this::add);
                            }};

                            Collections.shuffle(randomIndexes);

                            while (randomNonDominatedSolution == null && randomIndexes.size() > 0) {
                                int randomIndex = randomIndexes.remove(0);

                                List<String> selectedTrackIds = trackIdsList.get(randomIndex).stream()
                                    .filter(trackId -> !newPlaylists.get(problemPlaylist).containsValue(trackId))
                                    .map(id -> "spotify:track:" + id)
                                    .collect(Collectors.toList());

                                if (selectedTrackIds.size() >= AppConfiguration.get().numberOfTracks) {
                                    randomNonDominatedSolution = selectedTrackIds
                                        .subList(0, AppConfiguration.get().numberOfTracks);
                                }
                            }

                            if (randomNonDominatedSolution == null) {
                                JMetalLogger.logger.warning("None of the solutions has enough tracks: "
                                    + algorithmTag + "/" + experimentProblem.getTag() + "/" + indicator.getName());

                                return;
                            }

                            JMetalLogger.logger.info("Found solution: "
                                + algorithmTag + "/" + experimentProblem.getTag() + "/" + indicator.getName());

                            submissions.get(algorithmTag).put(problemPlaylist, randomNonDominatedSolution);
                        } catch (IOException e) {
                            JMetalLogger.logger.severe("Cannot read from file: " + bestParetoSetsFileName);
                            e.printStackTrace();
                        }
                    });
                });
        });

        submissions.forEach((algorithm, tracks) -> {
            final String fileName = submissionDirectory + "/" + algorithm + ".csv";

            BufferedWriter writer
                = new DefaultFileOutputContext(fileName, ",").getFileWriter();

            try {
                writer.write("team_info," + AppConfiguration.get().submissionTeamInfo);
                writer.newLine();

                for (Map.Entry<Integer, List<String>> entry : tracks.entrySet()) {
                    writer.write(entry.getKey() + "," + String.join(",", entry.getValue()));
                    writer.newLine();

                    JMetalLogger.logger.info("Successfully written playlist tracks: "
                        + entry.getKey() + " - " + algorithm);
                }

                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
