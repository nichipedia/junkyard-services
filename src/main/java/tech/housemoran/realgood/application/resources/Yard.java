package tech.housemoran.realgood.application.resources;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import tech.housemoran.realgood.application.clients.VinAuditClient;
import tech.housemoran.realgood.application.clients.api.VinAuditor;
import tech.housemoran.realgood.application.datalayer.api.VehicleReportService;
import tech.housemoran.realgood.application.exceptions.DatabaseDeletionException;
import tech.housemoran.realgood.application.exceptions.DatabaseWriteException;
import tech.housemoran.realgood.application.exceptions.InvalidVinException;
import tech.housemoran.realgood.application.utils.JsonMessages;
import tech.housemoran.realgood.models.VehicleReportProtobuf.VehicleReport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/yard")
public class Yard {

    private static final Logger log = LoggerFactory.getLogger(Yard.class);
    private static final JsonFormat.Printer formater = JsonFormat.printer();

    @Autowired
    private VehicleReportService reportDB;

    @CrossOrigin
    @GetMapping("/vehicle")
    public ResponseEntity<List<String>> getVehicles() {
        log.trace("Getting all vehicle reports!");
        final Iterable<VehicleReport> reports = reportDB.getVehicleReport();
        final List<String> jsonArray = new ArrayList<>();
        reports.forEach(report -> {
            try {
                jsonArray.add(formater.print(report));
            } catch (InvalidProtocolBufferException e) {
               log.warn("Could not format report with ID" + report.getVin() + " into JSON");
            }
        });
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(jsonArray);
    }

    @GetMapping("/vehicle/{vin}")
    public ResponseEntity<String> getVehicle(@PathVariable("vin") String vin) {
        log.trace("Getting Report for vin: " + vin);
        final Optional<VehicleReport> report = reportDB.getVehicleReport(vin);
        if (report.isPresent()) {
            try {
                final String json = formater.print(report.get());
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(json);
            } catch (InvalidProtocolBufferException e) {
                log.warn("Could not format report with ID" + report.get().getVin() + " into JSON");
                return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(JsonMessages.JsonErrorMessage("Issue Formating Report!"));
            }
        } else {
            final VinAuditor vinAuditor = new VinAuditClient();
            try {
                final Optional<VehicleReport> report2 = vinAuditor.getReport(vin, "");
                final String json = formater.print(report2.get());
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(json);
            } catch (InvalidVinException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            log.debug("Report not found for vin: " +  vin);
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(JsonMessages.JsonErrorMessage("Report Not found at that vin!"));
        }
    }


    @PostMapping("/vehicle")
    public ResponseEntity<String> addVhehicle(@RequestParam(value = "vin") String vin, @RequestParam(value = "comments") String comments) {
        log.trace("Adding vehicle report for vin: " + vin);
        final VinAuditor vinAuditor = new VinAuditClient();
        try {
            final Optional<VehicleReport> report = vinAuditor.getReport(vin, comments);
            reportDB.addVehicleReport(report.get());
            final String json = formater.print(report.get());
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(json);
        } catch (InvalidVinException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DatabaseWriteException e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/vehicle/{vin}")
    public ResponseEntity<String> deleteVehicle(@PathVariable("vin") String vin) {
        log.trace("Deleteing vehicle report for vin: " + vin);
        final Optional<VehicleReport> report = reportDB.getVehicleReport(vin);
        if (report.isPresent()) {
            try {
                reportDB.deleteVehicleReport(report.get());
                return ResponseEntity.ok().build();
            } catch (DatabaseDeletionException e) {
                e.printStackTrace();
                return ResponseEntity.notFound().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}