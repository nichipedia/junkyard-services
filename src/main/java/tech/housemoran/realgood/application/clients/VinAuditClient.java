package tech.housemoran.realgood.application.clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.housemoran.realgood.application.clients.api.VinAuditor;
import tech.housemoran.realgood.application.exceptions.InvalidVinException;
import tech.housemoran.realgood.models.VehicleReportProtobuf.VehicleReport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class VinAuditClient implements VinAuditor {

    private static final Logger log = LoggerFactory.getLogger(VinAuditClient.class);

    @Override
    public Optional<VehicleReport> getReport(String vin, String comments) throws InvalidVinException, IOException {
        final String formattedVin = String.format("https://specifications.vinaudit.com/v3/specifications?key=VBECYHUA5NZWMJT&vin=%s&include=selections,attributes,photos", vin);
        final URL url = new URL(formattedVin);
        final HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Accept", "application/json");
        log.info("Response code from vinaudit.com: " + con.getResponseCode());
        final BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
        final StringBuilder builder = new StringBuilder();
        String buf;
        while ((buf = br.readLine()) != null) {
            builder.append(buf);
        }
        final ObjectMapper mapper = new ObjectMapper();
        final Map<String, Object> json = mapper.readValue(builder.toString(), Map.class);
        final Map<String, Object> attributes = (Map<String, Object>)json.get("attributes");
        final VehicleReport.Builder repoBuilder = VehicleReport.newBuilder();
        repoBuilder.setDoors(attributes.get("doors").toString());
        repoBuilder.setYear(attributes.get("year").toString());
        repoBuilder.setEngineSize(attributes.get("engine_size").toString());
        repoBuilder.setEngine(attributes.get("engine").toString());
        repoBuilder.setVin(vin);
        repoBuilder.setTrim(attributes.get("trim").toString());
        repoBuilder.setEngineCylinders(attributes.get("engine_cylinders").toString());
        repoBuilder.setDrivetrain(attributes.get("drivetrain").toString());
        repoBuilder.setCurbWeight(attributes.get("curb_weight").toString());
        repoBuilder.setCityMileage(attributes.get("city_mileage").toString());
        repoBuilder.setAntiBrakeSystem(attributes.get("anti_brake_system").toString());
        repoBuilder.setFuelCapacity(attributes.get("fuel_capacity").toString());
        repoBuilder.setFuelType(attributes.get("fuel_type").toString());
        repoBuilder.setGrossVehicleWeightRating(attributes.get("gross_vehicle_weight_rating").toString());
        repoBuilder.setHighwayMileage(attributes.get("highway_mileage").toString());
        repoBuilder.setMadeIn(attributes.get("made_in").toString());
        repoBuilder.setMadeInCity(attributes.get("made_in_city").toString());
        repoBuilder.setDeleted(false);
        repoBuilder.setMake(attributes.get("make").toString());
        repoBuilder.setModel(attributes.get("model").toString());
        repoBuilder.setOverallHeight(attributes.get("overall_height").toString());
        repoBuilder.setOverallLength(attributes.get("overall_length").toString());
        repoBuilder.setOverallWidth(attributes.get("overall_width").toString());
        repoBuilder.setPartsMissing(comments);
        final List<Map<String, Object>> photos = (List<Map<String, Object>>)json.get("photos");
        repoBuilder.setPhoto(photos.stream().findFirst().get().get("url").toString());
        return Optional.of(repoBuilder.build());
    }
}
