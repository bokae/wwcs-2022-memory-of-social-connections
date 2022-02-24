package wwcs2022.socialmemcon.config;

import com.graphhopper.util.JsonFeature;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Restriction {
    protected static final Logger log = LoggerFactory.getLogger(RestrictionEntry.class);

    private final String name;
    private final Double speedLimit;
    private final List<LatLng> polygon;
    private final List<Long> nodeIds;
    private final List<Long> wayIds;
    private final List<Long> relationIds;
    private final Map<String,Object> wayTags;

    public Restriction(String name, Double speedLimit, List<LatLng> polygon, List<Long> nodeIds,
                       List<Long> wayIds, List<Long> relationIds, Map<String,Object> wayTags) {
        this.polygon = polygon;
        this.name = name;
        this.speedLimit = speedLimit;
        this.nodeIds = nodeIds;
        this.wayIds = wayIds;
        this.relationIds = relationIds;
        this.wayTags = wayTags;
    }

    public String getName() {
        return name;
    }

    public Double getSpeedLimit() {
        return speedLimit;
    }

    public List<LatLng> getPolygon() {
        if (polygon == null) {
            return null;
        }
        return Collections.unmodifiableList(polygon);
    }

    public JsonFeature asFeature() {
        GeometryFactory gFac = new GeometryFactory();

        JsonFeature result = new JsonFeature();
        Coordinate[] coors = polygon.stream().map(LatLng::toCoordinate).toArray(Coordinate[]::new);
        result.setGeometry(gFac.createPolygon(coors));
        return result;
    }
}
