package de.dominikdassow.musicrs.model.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import de.dominikdassow.musicrs.model.Track;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class PlaylistTracksDeserializer extends JsonDeserializer<Map<Integer, Track>> {
    @Override
    public Map<Integer, Track> deserialize(JsonParser parser, DeserializationContext context)
        throws IOException {

        return Arrays.stream(parser.getCodec().readValue(parser, PlaylistTrack[].class))
            .collect(Collectors.toMap(PlaylistTrack::getPosition, PlaylistTrack::asTrack));
    }
}
