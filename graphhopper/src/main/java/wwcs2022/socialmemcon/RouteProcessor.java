package wwcs2022.socialmemcon;

import com.graphhopper.*;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.shapes.GHPoint3D;
import org.codehaus.commons.compiler.util.Producer;
import org.locationtech.jts.geom.LineString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wwcs2022.socialmemcon.config.LocationEntry;

import java.util.*;

public class RouteProcessor {

    private static Logger log = LoggerFactory.getLogger(RouteProcessor.class);

    private Set<String> visitedNames = new HashSet<>();
    private Set<GHPoint3D> points = new HashSet<>();
    private List<LineString> routes = new ArrayList<>();

    public Map<Integer, Map<String, Map<String,Double>>> computeDistances(
            List<LocationEntry> locations, Map<Integer,Producer<GraphHopper>> hoppers, String vehicle, RoutingMain.Weighting w) {
        Map<Integer, Map<String, Map<String,Double>>> result = new TreeMap<>();
        for (var entry : hoppers.entrySet()) {
            result.put(entry.getKey(), computeDistances(locations, entry.getValue(), vehicle, w));
        }
        return result;
    }


    public Map<String, Map<String,Double>> computeDistances(
            List<LocationEntry> locations, Producer<GraphHopper> prod, String vehicle, RoutingMain.Weighting w) {
        GraphHopper hopper = prod.produce();
        var result = computeDistances(locations, hopper, vehicle, w);
        hopper.close();
        return result;
    }

    public Map<String, Map<String,Double>> computeDistances(
            List<LocationEntry> locations, GraphHopper hopper, String vehicle, RoutingMain.Weighting w) {
        Map<String,Map<String,Double>> result = new TreeMap<>();
        for (LocationEntry from : locations) {
            Map<String,Double> innerMap = new TreeMap<>();
            for (LocationEntry to : locations) {
                if (from != to) {
                    String profile = w.getProfileForVehicle(vehicle);
                    GHRequest req = new GHRequest(from.toPoint(), to.toPoint()).setProfile(profile);
                    GHResponse resp = hopper.route(req);
                    process(resp.getBest());
                    if (resp.hasErrors()) {
                        log.error("Errors in route computation: {}", resp.getErrors());
                    }
                    innerMap.put(to.getName(), resp.getBest().getDistance());
                }
            }
            result.put(from.getName(), innerMap);
        }
        return result;
    }

    public void printStats() {
        log.info("{} unique names visited", visitedNames.size());
        log.info("{} unique points visited", points.size());
        long legs = routes.stream().mapToInt(ls -> ls.getNumPoints()-1).sum();
        log.info("{} routes, {} line segments in total", routes.size(), legs);
    }

    private void process(ResponsePath path) {
        for (Instruction instruction : path.getInstructions()) {
            visitedNames.add(instruction.getName());
            instruction.getPoints().forEach(points::add);
        }
        routes.add(path.getPoints().toLineString(true));
    }

}
