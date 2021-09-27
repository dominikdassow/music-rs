package de.dominikdassow.musicrs.task;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import de.dominikdassow.musicrs.AppConfiguration;
import de.dominikdassow.musicrs.model.SimilarTracksList;
import de.dominikdassow.musicrs.recommender.MusicPlaylistContinuationAlgorithm;
import de.dominikdassow.musicrs.recommender.MusicPlaylistContinuationProblem;
import de.dominikdassow.musicrs.recommender.algorithm.AlgorithmConfiguration;
import de.dominikdassow.musicrs.recommender.engine.SimilarTracksEngine;
import de.dominikdassow.musicrs.service.DatabaseService;
import de.dominikdassow.musicrs.study.ExecuteAlgorithms;
import de.dominikdassow.musicrs.study.GenerateReferenceParetoFront;
import de.dominikdassow.musicrs.study.GenerateTrackIdsForBestParetoSets;
import lombok.extern.slf4j.Slf4j;
import org.uma.jmetal.lab.experiment.Experiment;
import org.uma.jmetal.lab.experiment.ExperimentBuilder;
import org.uma.jmetal.lab.experiment.component.impl.ComputeQualityIndicators;
import org.uma.jmetal.lab.experiment.component.impl.GenerateFriedmanTestTables;
import org.uma.jmetal.lab.experiment.component.impl.GenerateLatexTablesWithStatistics;
import org.uma.jmetal.lab.experiment.util.ExperimentAlgorithm;
import org.uma.jmetal.lab.experiment.util.ExperimentProblem;
import org.uma.jmetal.lab.visualization.StudyVisualizer;
import org.uma.jmetal.qualityindicator.impl.hypervolume.impl.PISAHypervolume;
import org.uma.jmetal.solution.Solution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

@Slf4j
public class ConductStudyTask
    extends Task {

    private final List<List<Integer>> playlists = new ArrayList<>();

    private SimilarTracksEngine similarTracksEngine;

    private List<AlgorithmConfiguration<? extends Solution<Integer>>> algorithmConfigurations;

    private Map<Integer, List<SimilarTracksList>> similarTracksListsByPlaylist;
    private Map<Integer, Map<Integer, String>> tracksListsByPlaylist;

    public ConductStudyTask() {
        super("Conduct Study (" + AppConfiguration.get().studyName + ")");
    }

    public ConductStudyTask fromConfiguration() {
        JsonNode json = AppConfiguration.get().json;

        log.info(json.toPrettyString());

        JsonNode jsonPlaylists = json.get("studyPlaylists");

        if (jsonPlaylists.isNull()) {
            log.info("No playlists specified. Using all challenge playlists...");

            playlists.add(DatabaseService
                .readAllPlaylistChallenges()
                .sorted()
                .distinct()
                .collect(Collectors.toList()));
        } else if (jsonPlaylists.isObject()) {
            log.info("Playlists range specified (" + jsonPlaylists + "). Using these challenge playlists...");

            List<Integer> playlistsInRange = DatabaseService
                .readAllPlaylistChallenges()
                .sorted()
                .distinct()
                .skip(jsonPlaylists.get("offset").asInt())
                .limit(jsonPlaylists.get("count").asInt())
                .collect(Collectors.toList());

            if (jsonPlaylists.hasNonNull("batchSize")) {
                playlists.addAll(Lists.partition(playlistsInRange, jsonPlaylists.get("batchSize").asInt()));
            } else {
                playlists.add(playlistsInRange);
            }
        } else {
            playlists.add(StreamSupport.stream(jsonPlaylists.spliterator(), false)
                .map(JsonNode::asInt)
                .distinct()
                .collect(Collectors.toList()));
        }

        algorithmConfigurations = StreamSupport.stream(json.get("studyAlgorithms").spliterator(), false)
            .distinct()
            .map(name -> AlgorithmConfiguration.fromName(name.asText()))
            .collect(Collectors.toList());

        if (json.hasNonNull("studyPlaylistsPreload") && json.get("studyPlaylistsPreload").asBoolean()) {
            similarTracksListsByPlaylist = DatabaseService.readSimilarTracksLists();
            log.info("Preload :: similarTracksListsByPlaylist=" + similarTracksListsByPlaylist.size());

            tracksListsByPlaylist = DatabaseService.readPlaylistsTracks();
            log.info("Preload :: tracksListsByPlaylist=" + tracksListsByPlaylist.size());
        }

        return this;
    }

    public ConductStudyTask forPlaylists(Integer... playlist) {
        playlists.add(new ArrayList<>(Set.of(playlist)));

        return this;
    }

    public ConductStudyTask using(List<AlgorithmConfiguration<? extends Solution<Integer>>> configurations) {
        algorithmConfigurations = configurations;

        return this;
    }

    @Override
    protected void init() {
        similarTracksEngine = new SimilarTracksEngine();
    }

    @Override
    protected void execute() throws IOException {
        log.info("NUMBER OF PROCESSORS: " + Runtime.getRuntime().availableProcessors());
        log.info("NUMBER OF THREADS: " + ForkJoinPool.commonPool().getParallelism());

        playlists.forEach(batch -> {
            Map<String, ExperimentProblem<Solution<Integer>>> problems
                = new ConcurrentHashMap<>();

            List<ExperimentAlgorithm<Solution<Integer>, List<Solution<Integer>>>> algorithms
                = new ArrayList<>();

            batch.parallelStream().forEach(playlist -> {
                MusicPlaylistContinuationProblem.Configuration problemConfiguration
                    = getProblemConfiguration(playlist);

                String problemTag = "MPC_" + playlist;

                algorithmConfigurations.forEach(algorithmConfiguration -> {
                    @SuppressWarnings("unchecked")
                    MusicPlaylistContinuationAlgorithm<Solution<Integer>> algorithm
                        = (MusicPlaylistContinuationAlgorithm<Solution<Integer>>)
                        algorithmConfiguration.createAlgorithmFor(problemConfiguration);

                    problems.computeIfAbsent(problemTag, tag -> {
                        ExperimentProblem<Solution<Integer>> problem
                            = new ExperimentProblem<>(algorithm.getProblem(), tag);

                        problem.setReferenceFront(tag + ".csv");

                        return problem;
                    });

                    IntStream.range(0, AppConfiguration.get().studyIndependentRuns)
                        .forEach(run -> algorithms.add(new ExperimentAlgorithm<>(
                            algorithm.get(),
                            algorithmConfiguration.getName(),
                            problems.get(problemTag),
                            run
                        )));
                });
            });

            Experiment<Solution<Integer>, List<Solution<Integer>>> experiment =
                new ExperimentBuilder<Solution<Integer>, List<Solution<Integer>>>(AppConfiguration.get().studyName)
                    .setAlgorithmList(algorithms)
                    .setProblemList(new ArrayList<>(problems.values()))
                    .setExperimentBaseDirectory(AppConfiguration.get().dataDirectory + "/study")
                    .setOutputParetoFrontFileName("FUN")
                    .setOutputParetoSetFileName("VAR")
                    .setReferenceFrontDirectory(AppConfiguration.get().dataDirectory + "/study/"
                        + AppConfiguration.get().studyName + "/referenceFronts")
                    .setIndicatorList(List.of(
                        new PISAHypervolume<>()
                    ))
                    .setIndependentRuns(AppConfiguration.get().studyIndependentRuns)
                    .build();

            new ExecuteAlgorithms<>(experiment, AppConfiguration.get().studyMaxRetries).run();

            if (AppConfiguration.get().studyGenerateResults) {
                try {
                    new GenerateReferenceParetoFront(experiment).run();
                    new ComputeQualityIndicators<>(experiment).run();
                    new GenerateTrackIdsForBestParetoSets(experiment).run();
                    new GenerateFriedmanTestTables<>(experiment).run();
                    new GenerateLatexTablesWithStatistics(experiment).run();
                    new StudyVisualizer(AppConfiguration.get().dataDirectory + "/study/"
                        + AppConfiguration.get().studyName, StudyVisualizer.TYPE_OF_FRONT_TO_SHOW.MEDIAN)
                        .createHTMLPageForEachIndicator();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                    e.printStackTrace();
                }
            }
        });
    }

    private MusicPlaylistContinuationProblem.Configuration getProblemConfiguration(Integer playlist) {
        List<SimilarTracksList> similarTracksLists = getSimilarTracksLists(playlist);

        long numberOfUniqueTracks = similarTracksLists.stream()
            .flatMap(similarTracksList -> similarTracksList.getTracks().stream())
            .distinct()
            .count();

        if (numberOfUniqueTracks < AppConfiguration.get().numberOfTracks) {
            log.warn("# [" + playlist + "] SIMILAR PLAYLISTS :: " +
                "Not enough unique tracks: " + numberOfUniqueTracks);

            return null;
        }

        log.info("# [" + playlist + "] SIMILAR PLAYLISTS :: " + numberOfUniqueTracks);

//        log.info("# [" + playlist + "] SIMILAR PLAYLISTS :: "
//            + similarTracksLists.size() + " :: "
//            + similarTracksLists.stream()
//            .flatMap(similarTracksList -> similarTracksList.getTracks().stream())
//            .distinct()
//            .count());

        Map<Integer, String> tracks = getTracksLists(playlist);

        return new MusicPlaylistContinuationProblem.Configuration(similarTracksEngine, similarTracksLists, tracks);
    }

    private List<SimilarTracksList> getSimilarTracksLists(Integer playlist) {
        if (similarTracksListsByPlaylist == null || !similarTracksListsByPlaylist.containsKey(playlist))
            return DatabaseService.readSimilarTracksLists(playlist);

        return similarTracksListsByPlaylist.get(playlist);
    }

    private Map<Integer, String> getTracksLists(Integer playlist) {
        if (tracksListsByPlaylist == null || !tracksListsByPlaylist.containsKey(playlist))
            return DatabaseService.readPlaylistTracks(playlist);

        return tracksListsByPlaylist.get(playlist);
    }
}
