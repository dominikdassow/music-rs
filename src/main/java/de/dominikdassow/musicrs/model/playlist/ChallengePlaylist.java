package de.dominikdassow.musicrs.model.playlist;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.dominikdassow.musicrs.model.Identifiable;
import de.dominikdassow.musicrs.model.Playlist;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "challenge_set")
public class ChallengePlaylist
    extends Playlist {

    @JsonProperty("num_holdouts")
    private Integer numberOfHoldouts;

    @JsonProperty("num_samples")
    private Integer numberOfSamples;

    @Data
    @Document(collection = "dataset")
    public static class WithId
        implements Identifiable {

        private Integer id;
    }
}
