package wwcs2022.socialmemcon.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.graphhopper.util.shapes.GHPoint;
import org.locationtech.jts.geom.Coordinate;

import java.io.IOException;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Objects;

@JsonSerialize(using = LatLng.LatLngSerializer.class)
@JsonDeserialize(using = LatLng.LatLngDeserializer.class)
public final class LatLng {

    private final double latitude, longitude;

    public LatLng(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public Coordinate toCoordinate() {
        return new Coordinate(latitude, longitude);
    }

    public GHPoint toGHPoint() {
        return new GHPoint(latitude, longitude);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LatLng latLng = (LatLng) o;
        return Double.compare(latLng.latitude, latitude) == 0 && Double.compare(latLng.longitude, longitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }

    @Override
    public String toString() {
        return "LatLng{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }

    public static final class LatLngSerializer extends JsonSerializer<LatLng> {

        @Override
        public void serialize(LatLng latLng, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            double [] array = new double[]{ latLng.getLatitude(), latLng.getLongitude() };
            jsonGenerator.writeArray(array, 0, 2);
        }
    }

    public static final class LatLngDeserializer extends JsonDeserializer<LatLng> {

        @Override
        public LatLng deserialize(JsonParser jsonParser, DeserializationContext dctx) throws IOException {
            List<Number> node = jsonParser.readValueAs(new TypeReference<List<? extends Number>>(){});
            if (node.size() == 2) {
                return new LatLng(node.get(0).doubleValue(), node.get(1).doubleValue());
            }
            throw new InputMismatchException("A LatLng object should be an array of exactly two numbers");
        }
    }

}
