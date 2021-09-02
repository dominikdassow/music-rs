package de.dominikdassow.musicrs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;

public final class AppConfiguration {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static AppConfiguration instance;

    public JsonNode json;

    public int numberOfTracks;
    public int minNumberOfCandidateTracks;
    public int maxNumberOfTracksPerDatasetPlaylist;
    public int firstChallengeSetPlaylistId;
    public int studyIndependentRuns;
    public int importSlices;
    public int importSlicesOffset;
    public int importSlicesPerBatch;
    public int studyMaxRetries;
    public boolean studyGenerateResults;
    public String studyName;
    public String dataDirectory;
    public String storeVersion;

    private AppConfiguration() {}

    public static AppConfiguration get() {
        if (instance == null) instance = new AppConfiguration();

        return instance;
    }

    public void load(String file) throws IOException {
        json = objectMapper.readTree(new File(file));

        numberOfTracks = json.get("numberOfTracks").asInt();
        minNumberOfCandidateTracks = json.get("minNumberOfCandidateTracks").asInt();
        maxNumberOfTracksPerDatasetPlaylist = json.get("minNumberOfTracksPerDatasetPlaylist").asInt();
        firstChallengeSetPlaylistId = json.get("firstChallengeSetPlaylistId").asInt();
        importSlices = json.get("importSlices").asInt();
        importSlicesOffset = json.get("importSlicesOffset").asInt();
        importSlicesPerBatch = json.get("importSlicesPerBatch").asInt();
        studyIndependentRuns = json.get("studyIndependentRuns").asInt();
        studyMaxRetries = json.get("studyMaxRetries").asInt();
        studyGenerateResults = json.get("studyGenerateResults").asBoolean(false);
        studyName = json.get("studyName").asText();
        dataDirectory = json.get("dataDirectory").asText();
        storeVersion = json.get("storeVersion").asText();
    }

    @SneakyThrows
    @Override
    public String toString() {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this.json);
    }
}
