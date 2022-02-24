package wwcs2022.socialmemcon;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import wwcs2022.socialmemcon.config.Config;
import wwcs2022.socialmemcon.config.LocationEntry;
import wwcs2022.socialmemcon.config.RestrictionEntry;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Main {

    private static final String PBF_LOCATION = "https://download.geofabrik.de/europe/hungary-latest.osm.pbf";
    private static final String DATA_FILE = "data/hungary-latest.osm.pbf";
    private static final String GH_CACHE_DIR = "cache";
    private static final String COORDINATES_FILE = "coordinates.txt";
    private static final String RESTRICTIONS_FILE = "restrictions.txt";
    private static final String OUTPUT_PREFIX = "output_";
    private static final String VEHICLE = "car";
    private static final String PROFILE = "car";

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String [] args) throws IOException {
        File cfgFile = new File("config.yaml");
        log.info("Reading config file {}", cfgFile);
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        Config cfg = om.readValue(cfgFile, Config.class);
        log.info("File read. Starting processing pipeline.");
        executeConfig(cfg);
    }

    public static void executeConfig(Config cfg) throws IOException {

        String dataFile = cfg.getDataFile();
        String cacheDir = cfg.getCacheFolder();
        String vehicle = cfg.getVehicle();
        List<LocationEntry> locations = cfg.getLocations();
        if (!Downloader.downloadFile(cfg.getOsmUrl(), dataFile)) {
            log.info("Unable to obtain data file. Exiting");
            return;
        }
        log.info("Processing datasets");
        var datasets = FilterOSMMain.processAll(cfg.getRestrictions(), dataFile,
                cfg.getDataFolder()+File.separator+"network-", ".osm.pbf");
        log.info("Datasets: {}", datasets);
        log.info("Setting up routing engines");
        var hoppers = RoutingMain.createInstances(datasets, cacheDir, vehicle);
        log.info("Hoppers: {}", hoppers);
        var baseHopper = RoutingMain.createGraphHopperInstance(dataFile,
                cacheDir+File.separator+"now", vehicle);
        log.info("Computing distances");
        RouteProcessor proc = new RouteProcessor();
        var baseDistances = proc.computeDistances(locations, baseHopper, vehicle, RoutingMain.Weighting.SHORTEST);
        var distances = proc.computeDistances(locations, hoppers, vehicle, RoutingMain.Weighting.SHORTEST);
        log.info("Routes processed.");
        proc.printStats();
        log.info("Writing spreadsheets");
        SpreadsheetWriter.writeBigSpreadsheet(baseDistances, distances, new File(cfg.getOutputPrefix()+"-all.xlsx"));
        log.info("Finished processing");
    }

    public static void mainOld(String [] args) throws IOException {

        File coorFile = new File(COORDINATES_FILE);
        File resFile = new File(RESTRICTIONS_FILE);

        if (!coorFile.exists()) {
            log.info("No coordinates file for the routing found. Please make sure {} exists.", coorFile);
            return;
        }

        if (!resFile.exists()) {
            log.info("No restrictions file found for the deletion of parts of the network."
             + " Please make sure {} exists", resFile);
            return;
        }

        List<LocationEntry> locations = LocationEntry.readPoints(coorFile);
        if (locations == null) {
            log.info("Unable to obtain coordinates of interest. Exiting");
            return;
        }
        log.info("{} locations of interest read from input file", locations.size());
        List<RestrictionEntry> restrictions = RestrictionEntry.readEntries(resFile);
        if (restrictions == null) {
            log.info("Problem while reading restrictions file. Aborting.");
            return;
        }
        log.info("{} restriction entries read from input file", restrictions.size());

        Config cfg = new Config(PBF_LOCATION,
                                new File(DATA_FILE).getParent(),
                                GH_CACHE_DIR,
                                locations,
                                restrictions,
                                OUTPUT_PREFIX,
                                VEHICLE,
                                PROFILE
                );

        executeConfig(cfg);
    }

}
