package de.dominikdassow.musicrs.model.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.dominikdassow.musicrs.model.Track;
import de.dominikdassow.musicrs.service.database.TrackIdGenerator;
import lombok.Getter;

@Getter
@SuppressWarnings("unused")
public class PlaylistTrack
    extends Track {

    @JsonProperty("pos")
    private Integer position;

    public Track asTrack() {
        return withId(TrackIdGenerator.generate(getUri()));
    }
}
