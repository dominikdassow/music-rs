package de.dominikdassow.musicrs.model.feature;

import de.dominikdassow.musicrs.model.Playlist;
import de.dominikdassow.musicrs.model.Track;
import de.dominikdassow.musicrs.model.track.AudioFeatures;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FeatureGenerator {

    private FeatureGenerator() {}

    public static Set<PlaylistFeature> generateFor(Playlist playlist) {
        final Collection<Track> tracks = playlist.getTracks().values();

        final Map<String, Double> trackDimension = new HashMap<>();
        final Map<String, Double> artistDimension = new HashMap<>();
        final Map<String, Double> albumDimension = new HashMap<>();
        final Map<TrackFeature.Audio, Double> audioDimension = new HashMap<>();

        tracks.forEach(track -> {
            addTrackFeatureValue(trackDimension, track.getId(), 1.0);
            addTrackFeatureValue(artistDimension, track.getArtistId(), 1.0);
            addTrackFeatureValue(albumDimension, track.getAlbumId(), 1.0);
        });

        addAverageAudioFeatureValue(audioDimension, tracks,
            TrackFeature.Audio.ACOUSTICNESS, AudioFeatures::getAcousticness);

        addAverageAudioFeatureValue(audioDimension, tracks,
            TrackFeature.Audio.DANCEABILITY, AudioFeatures::getDanceability);

        addAverageAudioFeatureValue(audioDimension, tracks,
            TrackFeature.Audio.ENERGY, AudioFeatures::getEnergy);

        addAverageAudioFeatureValue(audioDimension, tracks,
            TrackFeature.Audio.INSTRUMENTALNESS, AudioFeatures::getInstrumentalness);

//        addAverageAudioFeatureValue(audioDimension, tracks,
//            TrackFeature.Audio.KEY, Track.AudioFeatures::getKey);

        addAverageAudioFeatureValue(audioDimension, tracks,
            TrackFeature.Audio.LIVENESS, AudioFeatures::getLiveness);

        addAverageAudioFeatureValue(audioDimension, tracks,
            TrackFeature.Audio.LOUDNESS, AudioFeatures::getLoudness);

//        addAverageAudioFeatureValue(audioDimension, tracks,
//            TrackFeature.Audio.MODE, Track.AudioFeatures::getMode);

        addAverageAudioFeatureValue(audioDimension, tracks,
            TrackFeature.Audio.SPEECHINESS, AudioFeatures::getSpeechiness);

        addAverageAudioFeatureValue(audioDimension, tracks,
            TrackFeature.Audio.TEMPO, AudioFeatures::getTempo);

//        addAverageAudioFeatureValue(audioDimension, tracks,
//            TrackFeature.Audio.TIME_SIGNATURE, Track.AudioFeatures::getTimeSignature);

        addAverageAudioFeatureValue(audioDimension, tracks,
            TrackFeature.Audio.VALENCE, AudioFeatures::getValence);

        return new HashSet<>() {{
            trackDimension
                .forEach((id, value) -> add(new PlaylistFeature(PlaylistFeature.Dimension.TRACK, id, value)));
            artistDimension
                .forEach((id, value) -> add(new PlaylistFeature(PlaylistFeature.Dimension.ARTIST, id, value)));
            albumDimension
                .forEach((id, value) -> add(new PlaylistFeature(PlaylistFeature.Dimension.ALBUM, id, value)));
            audioDimension
                .forEach((id, value) -> add(new PlaylistFeature(PlaylistFeature.Dimension.AUDIO, id.name(), value)));
        }};
    }

    public static Set<TrackFeature> generateFor(Track track) {
        final Set<TrackFeature> features = new HashSet<>();

        if (Objects.nonNull(track.getArtistId()))
            features.add(TrackFeature.fromArtist(track.getArtistId(), 1.0));

        if (Objects.nonNull(track.getAlbumId()))
            features.add(TrackFeature.fromAlbum(track.getAlbumId(), 1.0));

        final AudioFeatures audioFeatures = track.getAudioFeatures();

        if (Objects.nonNull(audioFeatures)) {
            addAudioFeatureValue(features,
                TrackFeature.Audio.ACOUSTICNESS, audioFeatures.getAcousticness());

            addAudioFeatureValue(features,
                TrackFeature.Audio.DANCEABILITY, audioFeatures.getDanceability());

            addAudioFeatureValue(features,
                TrackFeature.Audio.ENERGY, audioFeatures.getEnergy());

            addAudioFeatureValue(features,
                TrackFeature.Audio.INSTRUMENTALNESS, audioFeatures.getInstrumentalness());

//            addAudioFeatureValue(features,
//                TrackFeature.Audio.KEY, audioFeatures.getKey());

            addAudioFeatureValue(features,
                TrackFeature.Audio.LIVENESS, audioFeatures.getLiveness());

            addAudioFeatureValue(features,
                TrackFeature.Audio.LOUDNESS, audioFeatures.getLoudness());

//            addAudioFeatureValue(features,
//                TrackFeature.Audio.MODE, audioFeatures.getMode());

            addAudioFeatureValue(features,
                TrackFeature.Audio.SPEECHINESS, audioFeatures.getSpeechiness());

            addAudioFeatureValue(features,
                TrackFeature.Audio.TEMPO, audioFeatures.getTempo());

//            addAudioFeatureValue(features,
//                TrackFeature.Audio.TIME_SIGNATURE, audioFeatures.getTimeSignature());

            addAudioFeatureValue(features,
                TrackFeature.Audio.VALENCE, audioFeatures.getValence());
        }

        return features;
    }

    private static void addTrackFeatureValue(Map<String, Double> dimension, String identifier, Double value) {
        if (Objects.nonNull(identifier) && Objects.nonNull(value))
            dimension.put(identifier, dimension.getOrDefault(identifier, 0.0) + value);
    }

    private static void addAverageAudioFeatureValue(
        Map<TrackFeature.Audio, Double> dimension,
        Collection<Track> tracks,
        TrackFeature.Audio audio,
        Function<AudioFeatures, Double> getFeature
    ) {
        List<Double> values = tracks.stream()
            .map(Track::getAudioFeatures)
            .filter(Objects::nonNull)
            .map(getFeature)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        if (values.isEmpty()) return;

        dimension.put(audio, values.stream().collect(Collectors.averagingDouble(value -> value)));
    }

    private static void addAudioFeatureValue(Set<TrackFeature> features, TrackFeature.Audio audio, Double value) {
        if (Objects.nonNull(value)) features.add(TrackFeature.fromAudio(audio, value));
    }
}
