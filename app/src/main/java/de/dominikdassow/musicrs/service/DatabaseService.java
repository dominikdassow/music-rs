package de.dominikdassow.musicrs.service;

import de.dominikdassow.musicrs.AppConfiguration;
import de.dominikdassow.musicrs.model.SimilarTracksList;
import de.dominikdassow.musicrs.model.Track;
import de.dominikdassow.musicrs.model.feature.PlaylistFeature;
import de.dominikdassow.musicrs.model.feature.TrackFeature;
import lombok.extern.slf4j.Slf4j;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
public class DatabaseService {

//    public static final int NUMBER_OF_PLAYLISTS = 1_000_000;
//    public static final int NUMBER_OF_TRACKS = 2_262_292;

    public static final String DELIMITER = ",";

    public enum Store {
        PLAYLIST("{data}/store/{version}/playlist.csv"),
        PLAYLIST_FEATURE("{data}/store/{version}/playlist-feature.csv"),
        TRACK("{data}/store/{version}/track.csv"),
        TRACK_FEATURE("{data}/store/{version}/track-feature.csv"),
        SIMILAR_TRACKS_LIST("{data}/store/{version}/similar-tracks-list.csv"),
        TRACK_AUDIO_FEATURE("{data}/store/tracks-audio-feature.csv");

        private final String file;

        Store(String file) {
            this.file = file;
        }

        public String getFile() {
            String file = this.file;

            return file
                .replace("{data}", AppConfiguration.get().dataDirectory)
                .replace("{version}", AppConfiguration.get().storeVersion);
        }
    }

    private DatabaseService() {}

    public static PrintWriter newStore(Store store) throws IOException {
        new PrintWriter(store.getFile()).close();

        return openStore(store);
    }

    public static PrintWriter openStore(Store store) throws IOException {
        return new PrintWriter(new FileWriter(store.getFile(), true));
    }

    public static Stream<String> readStore(Store store) {
        try {
            return Files.lines(Paths.get(store.getFile()));
        } catch (IOException e) {
            log.error(e.getMessage(), e);

            return Stream.empty();
        }
    }

    public static Stream<Integer> readAllPlaylistChallenges() {
        return readStore(DatabaseService.Store.PLAYLIST)
            .map(data -> Integer.parseInt(data.split(DELIMITER)[0]))
            .filter(data -> data >= AppConfiguration.get().firstChallengeSetPlaylistId)
            .distinct();
    }

    public static Map<Integer, String> readPlaylistTracks(Integer playlist) {
        final String[] ids = readStore(Store.PLAYLIST)
            .filter(data -> data.startsWith(playlist + DELIMITER))
            .map(data -> data.split(DELIMITER))
            .findFirst()
            .orElse(new String[]{});

        // Start at index 1 to ignore the playlist_id
        return IntStream.range(1, ids.length)
            .boxed()
            .filter(i -> !ids[i].isEmpty())
            .collect(Collectors.toMap(Function.identity(), i -> ids[i]));
    }

    public static Map<Integer, Map<Integer, String>> readPlaylistsTracks(Collection<Integer> playlists) {
        final List<String[]> idsList = readStore(Store.PLAYLIST)
            .map(data -> data.split(DELIMITER))
            .filter(data -> playlists.contains(Integer.parseInt(data[0])))
            .collect(Collectors.toList());

        return new HashMap<>() {{
            idsList.forEach(ids -> put(
                Integer.parseInt(ids[0]),
                // Start at index 1 to ignore the playlist_id
                IntStream.range(1, ids.length)
                    .boxed()
                    .filter(i -> !ids[i].isEmpty())
                    .collect(Collectors.toMap(Function.identity(), i -> ids[i])))
            );
        }};
    }

    public static long readNumberOfUniquePlaylistTracks(Collection<Integer> playlists) {
        return readPlaylistsTracks(playlists).values().stream()
            .flatMap(tracks -> tracks.values().stream())
            .distinct()
            .count();
    }

    public static Set<PlaylistFeature> readPlaylistFeatures(Integer playlist) {
        return readStore(Store.PLAYLIST_FEATURE)
            .filter(data -> data.startsWith(playlist + DELIMITER))
            .map(data -> data.split(DELIMITER))
            .map(data -> new PlaylistFeature(
                PlaylistFeature.Dimension.valueOf(data[1].split(PlaylistFeature.DELIMITER)[0]),
                data[1].split(PlaylistFeature.DELIMITER)[1],
                Double.valueOf(data[2])
            ))
            .collect(Collectors.toSet());
    }

    public static List<Track> readTracks(Collection<String> tracks) {
        return readStore(Store.TRACK)
            .map(data -> data.split(DELIMITER))
            .filter(data -> tracks.contains(data[0]))
            .map(data -> Track.builder()
                .id(data[0])
                .artistId(data[1])
                .albumId(data[2])
                .build())
            .collect(Collectors.toList());
    }

    public static List<SimilarTracksList> readSimilarTracksLists(Integer playlist) {
        return readStore(Store.SIMILAR_TRACKS_LIST)
            .filter(data -> data.startsWith(playlist + DELIMITER))
            .map(data -> data.split(DELIMITER))
            .map(data -> new SimilarTracksList(
                Arrays.asList(data).subList(2, data.length),
                Double.parseDouble(data[1]))
            )
            .collect(Collectors.toList());
    }

    public static Map<String, Map<TrackFeature.Audio, Double>> readTracksAudioFeatures(Collection<String> tracks) {
        final boolean doFilter = Objects.nonNull(tracks);
        final Map<String, Map<TrackFeature.Audio, Double>> audioFeatures = new HashMap<>();

        readStore(Store.TRACK_AUDIO_FEATURE)
            .map(data -> data.split(DELIMITER))
            .filter(data -> !doFilter || tracks.contains(data[0]))
            .forEach(data -> {
                audioFeatures.put(data[0], new HashMap<>() {{
                    put(TrackFeature.Audio.ACOUSTICNESS, Double.parseDouble(data[1]));
                    put(TrackFeature.Audio.DANCEABILITY, Double.parseDouble(data[2]));
                    put(TrackFeature.Audio.ENERGY, Double.parseDouble(data[3]));
                    put(TrackFeature.Audio.INSTRUMENTALNESS, Double.parseDouble(data[4]));
                    // put(TrackFeature.Audio.KEY, Double.parseDouble(data[5]));
                    put(TrackFeature.Audio.LIVENESS, Double.parseDouble(data[6]));
                    put(TrackFeature.Audio.LOUDNESS, Double.parseDouble(data[7]));
                    // put(TrackFeature.Audio.MODE, Double.parseDouble(data[8]));
                    put(TrackFeature.Audio.SPEECHINESS, Double.parseDouble(data[9]));
                    put(TrackFeature.Audio.TEMPO, Double.parseDouble(data[10]));
                    // put(TrackFeature.Audio.TIME_SIGNATURE, Double.parseDouble(data[11]));
                    put(TrackFeature.Audio.VALENCE, Double.parseDouble(data[12]));
                }});
            });

        return audioFeatures;
    }

    public static Map<String, Map<TrackFeature.Audio, Double>> readTracksAudioFeatures() {
        return readTracksAudioFeatures(null);
    }
}
