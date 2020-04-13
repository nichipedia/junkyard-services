package tech.housemoran.realgood.application.datalayer.api;


import tech.housemoran.realgood.application.exceptions.DatabaseDeletionException;
import tech.housemoran.realgood.application.exceptions.DatabaseWriteException;
import tech.housemoran.realgood.models.VehicleReportProtobuf.VehicleReport;

import java.util.Optional;

public interface VehicleReportService {
    Optional<VehicleReport> getVehicleReport(String vin);
    Iterable<VehicleReport> getVehicleReport();
    void addVehicleReport(VehicleReport report) throws DatabaseWriteException;
    void deleteVehicleReport(VehicleReport report) throws DatabaseDeletionException;
}
