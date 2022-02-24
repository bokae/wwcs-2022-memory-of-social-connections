package wwcs2022.socialmemcon.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.graphhopper.util.JsonFeature;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RestrictionEntry extends Restriction {

    private final int year;

    //    private final List<Long> nodeIds;
    //    private final List<Long> wayIds;
    //    private final List<Long> relationIds;
    //    private final Map<String,Object> wayTags;

    @JsonCreator
    public RestrictionEntry(@JsonProperty("name") String name,
                            @JsonProperty("speedLimit") double speedLimit,
                            @JsonProperty("year") int year,
                            @JsonProperty("polygon") List<LatLng> polygon,
                            @JsonProperty("nodeIds") List<Long> nodeIds,
                            @JsonProperty("wayIds") List<Long> wayIds,
                            @JsonProperty("relationIds") List<Long> relationIds,
                            @JsonProperty("wayTags") Map<String,Object> wayTags) {
        super(name,speedLimit,polygon, nodeIds, wayIds, relationIds, wayTags);
        this.year = year;
    }

    public static RestrictionEntry parse(String line) {
        String [] split1 = line.split(";");
        String name = split1[0].strip();
        double speedLimit = Double.parseDouble(split1[2].strip());
        int year = Integer.parseInt(split1[1].strip());
        String polygonStr = split1[3];
        String [] split2 = polygonStr.split("::");
        List<LatLng> polygon = new ArrayList<>(split2.length);
        for (String latLngStr : split2) {
            String [] split3 = latLngStr.split(",");
            double lat = Double.parseDouble(split3[0].strip());
            double lng = Double.parseDouble(split3[1].strip());
            polygon.add(new LatLng(lat,lng));
        }
        return new RestrictionEntry(name, speedLimit, year, polygon, Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyMap());
    }

    public int getYear() {
        return year;
    }

    public static List<RestrictionEntry> readEntries(File f) {
        try {
            return Files.lines(f.toPath())
                    .filter(line -> !line.isBlank())
                    .map(RestrictionEntry::parse)
                    .collect(Collectors.toList());
        }
        catch (IOException ex) {
            log.error("Error reading restriction entries", ex);
            return null;
        }
    }

}
