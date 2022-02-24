package wwcs2022.socialmemcon.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.graphhopper.util.shapes.GHPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

public final class LocationEntry {

    private static Logger log = LoggerFactory.getLogger(LocationEntry.class);

    private final String name;
    private final LatLng location;

    @JsonCreator
    public LocationEntry(@JsonProperty("name") String name,
                         @JsonProperty("location") LatLng location) {
        this.name = name;
        this.location = location;
    }


    public static LocationEntry parse(String line) {
        String [] split = line.split(",");
        String name = split[0].strip();
        double lat = Double.parseDouble(split[1].strip());
        double lng = Double.parseDouble(split[2].strip());
        return new LocationEntry(name, new LatLng(lat,lng));
    }

    public String getName() {
        return name;
    }

    public LatLng getLocation() {
        return location;
    }

    public GHPoint toPoint() {
        return location.toGHPoint();
    }

    @Override
    public String toString() {
        return "LocationEntry{" +
                "name='" + name + '\'' +
                ", location=" + location +
                '}';
    }

    public static List<LocationEntry> readPoints(File in) {
        try {
            return Files.lines(in.toPath())
                    .filter(line -> !line.isBlank())
                    .map(LocationEntry::parse)
                    .collect(Collectors.toList());
        }
        catch (IOException ex) {
            log.error("Error reading coordinates file", ex);
            return null;
        }
    }
}
