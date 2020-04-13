package tech.housemoran.realgood.application.clients.api;

import tech.housemoran.realgood.application.exceptions.InvalidVinException;
import tech.housemoran.realgood.models.VehicleReportProtobuf.VehicleReport;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Optional;

public interface VinAuditor {
    Optional<VehicleReport> getReport(String vin, String comments) throws InvalidVinException, IOException;
}
