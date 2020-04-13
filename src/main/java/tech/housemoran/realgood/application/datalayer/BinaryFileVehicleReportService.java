package tech.housemoran.realgood.application.datalayer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.housemoran.realgood.application.datalayer.api.VehicleReportRepository;
import tech.housemoran.realgood.application.datalayer.api.VehicleReportService;
import tech.housemoran.realgood.application.exceptions.DatabaseDeletionException;
import tech.housemoran.realgood.application.exceptions.DatabaseWriteException;
import tech.housemoran.realgood.models.VehicleReportProtobuf.VehicleReport;

import java.util.Optional;

@Service
public class BinaryFileVehicleReportService implements VehicleReportService {

    @Autowired
    private VehicleReportRepository repo;

    @Override
    public Optional<VehicleReport> getVehicleReport(String vin) {
        return this.repo.getVehicleReport(vin);
    }

    @Override
    public Iterable<VehicleReport> getVehicleReport() {
        return this.repo.getVehicleReport();
    }

    @Override
    public void addVehicleReport(VehicleReport report) throws DatabaseWriteException {
        this.repo.addVehicleReport(report);
    }

    @Override
    public void deleteVehicleReport(VehicleReport report) throws DatabaseDeletionException {
        this.repo.deleteVehicleReport(report);
    }
}
