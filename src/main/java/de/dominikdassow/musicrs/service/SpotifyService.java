package de.dominikdassow.musicrs.service;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.exceptions.detailed.TooManyRequestsException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.AudioFeatures;
import com.wrapper.spotify.requests.data.tracks.GetAudioFeaturesForSeveralTracksRequest;
import de.dominikdassow.musicrs.model.Track;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
@PropertySource("classpath:spotify.properties")
@SuppressWarnings("unused")
public class SpotifyService {

    @Value("${spotify.api.client-id}")
    private String clientId;

    @Value("${spotify.api.client-secret}")
    private String clientSecret;

    private SpotifyApi spotifyApi;

    public void init() {
        log.info("SpotifyService::init()");

        spotifyApi = new SpotifyApi.Builder()
            .setClientId(clientId)
            .setClientSecret(clientSecret)
            .build();

        try {
            final ClientCredentials clientCredentials = spotifyApi.clientCredentials()
                .build()
                .execute();

            spotifyApi.setAccessToken(clientCredentials.getAccessToken());

            log.info("Access Token: " + clientCredentials.getAccessToken());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            log.error(e.getMessage(), e);
        }
    }

    public AudioFeatures[] getAudioFeatures(List<Track.WithIdAndUri> tracks) throws Exception {
        final GetAudioFeaturesForSeveralTracksRequest request = spotifyApi
            .getAudioFeaturesForSeveralTracks(extractTrackIds(tracks))
            .build();

        try {
            return request.execute();
        } catch (TooManyRequestsException e) {
            int retryAfter = e.getRetryAfter();

            if (retryAfter < 1) retryAfter = 3;

            log.warn(e.getMessage(), "Retry After: " + retryAfter + " seconds");

            Thread.sleep(retryAfter * 1000L);

            return getAudioFeatures(tracks);
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new Exception(e.getMessage());
        }
    }

    public String[] extractTrackIds(List<Track.WithIdAndUri> tracks) {
        return tracks.stream()
            .map(track -> track.getUri().split(":")[2])
            .toArray(String[]::new);
    }
}
