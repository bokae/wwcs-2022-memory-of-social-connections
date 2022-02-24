package wwcs2022.socialmemcon.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

public class Config {

    private static final Logger log = LoggerFactory.getLogger(Config.class);

    private static final String DEFAULT_OSM_FILE_NAME = "main-data.osm.pbf";

    private final String osmUrl;
    private final String dataFolder;
    private final String cacheFolder;
    private final List<LocationEntry> locations;
    private final List<RestrictionEntry> restrictions;
    private final String outputPrefix;
    private final String vehicle;
    private final String profile;

    @JsonCreator
    public Config(@JsonProperty("osmUrl") String OSMUrl,
                  @JsonProperty("dataFolder") String dataFolder,
                  @JsonProperty("cacheFolder") String cacheFolder,
                  @JsonProperty("locations") List<LocationEntry> locations,
                  @JsonProperty("restrictions") List<RestrictionEntry> restrictions,
                  @JsonProperty("outputPrefix") String outputPrefix,
                  @JsonProperty("vehicle") String vehicle,
                  @JsonProperty("profile") String profile) {
        this.osmUrl = OSMUrl;
        this.dataFolder = dataFolder;
        this.cacheFolder = cacheFolder;
        this.locations = locations;
        this.restrictions = restrictions;
        this.outputPrefix = outputPrefix;
        this.vehicle = vehicle;
        this.profile = profile;
    }

    public String getOsmUrl() {
        return osmUrl;
    }

    public String getDataFolder() {
        return dataFolder;
    }

    @JsonIgnore
    public String getDataFile() {
        try {
            URL url = new URL(osmUrl);
            String path = url.getPath();
            String [] pathSplit = path.split("/");
            String last = pathSplit[pathSplit.length-1].trim();
            if (!last.isEmpty()) {
                return dataFolder + File.separator + last;
            }
        }
        catch (MalformedURLException ex) {
            log.error("Error while parsing URL to determine logical file name", ex);
        }
        return dataFolder + File.separator + DEFAULT_OSM_FILE_NAME;
    }

    public String getCacheFolder() {
        return cacheFolder;
    }

    public List<LocationEntry> getLocations() {
        return Collections.unmodifiableList(locations);
    }

    public List<RestrictionEntry> getRestrictions() {
        return Collections.unmodifiableList(restrictions);
    }

    public String getOutputPrefix() {
        return outputPrefix;
    }

    public String getVehicle() {
        return vehicle;
    }

    public String getProfile() {
        return profile;
    }

    @Override
    public String toString() {
        return "Config{" +
                "OSMUrl='" + osmUrl + '\'' +
                ", dataFolder='" + dataFolder + '\'' +
                ", cacheFolder='" + cacheFolder + '\'' +
                ", locations=" + locations +
                ", restrictions=" + restrictions +
                ", outputPrefix='" + outputPrefix + '\'' +
                ", vehicle='" + vehicle + '\'' +
                ", profile='" + profile + '\'' +
                '}';
    }
}
