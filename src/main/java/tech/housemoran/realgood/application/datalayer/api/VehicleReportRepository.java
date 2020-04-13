package tech.housemoran.realgood.application.datalayer.api;

import org.springframework.data.repository.CrudRepository;
import tech.housemoran.realgood.application.exceptions.DatabaseDeletionException;
import tech.housemoran.realgood.application.exceptions.DatabaseWriteException;
import tech.housemoran.realgood.models.VehicleReportProtobuf.VehicleReport;

import java.util.Optional;

public interface VehicleReportRepository extends CrudRepository<VehicleReport, String> {
    Optional<VehicleReport> getVehicleReport(String vin);
    Iterable<VehicleReport> getVehicleReport();
    void addVehicleReport(VehicleReport report) throws DatabaseWriteException;
    void deleteVehicleReport(VehicleReport report) throws DatabaseDeletionException;
}