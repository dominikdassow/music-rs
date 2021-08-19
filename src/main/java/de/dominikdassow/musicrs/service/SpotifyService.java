package de.dominikdassow.musicrs.service;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.exceptions.detailed.TooManyRequestsException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.AudioFeatures;
import com.wrapper.spotify.requests.data.tracks.GetAudioFeaturesForSeveralTracksRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.util.List;

@Slf4j
public class SpotifyService {

    private static final String CLIENT_ID = "f4e295abae84403485807f85ca438a5d";
    private static final String CLIENT_SECRET = "1680a267781141c580983e51c6084a07";

    private final SpotifyApi spotifyApi;

    public SpotifyService() {
        log.info("SpotifyService()");

        spotifyApi = new SpotifyApi.Builder()
            .setClientId(CLIENT_ID)
            .setClientSecret(CLIENT_SECRET)
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

    public AudioFeatures[] getAudioFeatures(List<String> tracks) throws Exception {
        try {
            final GetAudioFeaturesForSeveralTracksRequest request = spotifyApi
                .getAudioFeaturesForSeveralTracks(tracks.toArray(String[]::new))
                .build();

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
}
