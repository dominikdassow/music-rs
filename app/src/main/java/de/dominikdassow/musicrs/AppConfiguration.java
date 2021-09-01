package de.dominikdassow.musicrs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;

public final class AppConfiguration {

    private static AppConfiguration instance;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public int numberOfTracks;
    public int minNumberOfCandidateTracks;
    public int minNumberOfTracksPerDatasetPlaylist;
    public int firstChallengeSetPlaylistId;
    public int studyIndependentRuns;
    public int importSlices;
    public int importSlicesOffset;
    public int importSlicesPerBatch;
    public int studyMaxRetries;
    public String storeVersion;

    private AppConfiguration() {}

    public static AppConfiguration get() {
        if (instance == null) instance = new AppConfiguration();

        return instance;
    }

    public void load(String file) throws IOException {
        JsonNode json = objectMapper.readTree(new File(file));

        numberOfTracks = json.get("numberOfTracks").asInt();
        minNumberOfCandidateTracks = json.get("minNumberOfCandidateTracks").asInt();
        minNumberOfTracksPerDatasetPlaylist = json.get("minNumberOfTracksPerDatasetPlaylist").asInt();
        firstChallengeSetPlaylistId = json.get("firstChallengeSetPlaylistId").asInt();
        importSlices = json.get("importSlices").asInt();
        importSlicesOffset = json.get("importSlicesOffset").asInt();
        importSlicesPerBatch = json.get("importSlicesPerBatch").asInt();
        studyIndependentRuns = json.get("studyIndependentRuns").asInt();
        studyMaxRetries = json.get("studyMaxRetries").asInt();
        storeVersion = json.get("storeVersion").asText();
    }

    @SneakyThrows
    @Override
    public String toString() {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
    }
}
