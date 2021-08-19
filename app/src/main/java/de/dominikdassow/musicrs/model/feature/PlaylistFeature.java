package de.dominikdassow.musicrs.model.feature;

import de.dominikdassow.musicrs.util.Formatter;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlaylistFeature {

    public static final String DELIMITER = "#";

    private Dimension dimension;

    private String identifier;

    private Double value;

    public String getId() {
        return dimension.name() + DELIMITER + identifier;
    }

    public String formattedValue() {
        return Formatter.format(value);
    }

    public enum Dimension {
        TRACK,
        ARTIST,
        ALBUM,
        AUDIO,
    }
}
