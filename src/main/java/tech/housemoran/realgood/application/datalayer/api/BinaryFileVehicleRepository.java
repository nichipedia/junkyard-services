package tech.housemoran.realgood.application.datalayer.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import tech.housemoran.realgood.application.exceptions.DatabaseDeletionException;
import tech.housemoran.realgood.application.exceptions.DatabaseWriteException;
import tech.housemoran.realgood.models.VehicleReportProtobuf;
import tech.housemoran.realgood.models.VehicleReportProtobuf.VehicleReport;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Repository
public class BinaryFileVehicleRepository implements VehicleReportRepository {

    private final String dbFilePath;
    private final boolean persist;
    private final Map<String, VehicleReport> reports = new HashMap<>();
    private final static Logger log = LoggerFactory.getLogger(BinaryFileVehicleRepository.class);

    public BinaryFileVehicleRepository(@Value("${bean.properties.dbFilePath}") final String path) {
        log.info("Path for db: " + path);
        this.dbFilePath = path;
        final Path dbPath = Paths.get(this.dbFilePath);
        boolean temp = false;
        if (!dbPath.toFile().exists()) {
            try {
                if (dbPath.toFile().createNewFile()) {
                    log.info("DB Repository File Created!");
                    temp = true;
                } else {
                    log.warn("DB Repository File could not be created. Using memory only implementation. Be advised, new reports will not be persisted!");
                }
            } catch (IOException e) {
                log.warn("Issue loading DB repository file. Using memory only implementation. Be advised, new reports will not be persisted!", e);
            }
        } else {
            if (dbPath.toFile().isDirectory()) {
                log.warn("DB Repository path is a directory. This path needs to be a file. Using memory only implementation. Be advised, new reports will not be persisted!");
            } else {
                log.info("Reading persisted reports out of repository file!");
                try {
                    this.reports.putAll(readDbFile(dbPath));
                    temp = true;
                } catch (IOException e) {
                    log.warn("Issue loading DB repository file. Using memory only implementation. Be advised, new reports will not be persisted!", e);
                }
            }
        }
        this.persist = temp;
    }

    private Map<String, VehicleReport> readDbFile(final Path dbPath) throws IOException {
        final Map<String, VehicleReport> temp = new HashMap<String, VehicleReport>();
        try (final FileInputStream fis = new FileInputStream(dbPath.toFile());
             final FileChannel fc = fis.getChannel()) {
            final ByteBuffer messageLengthBuf = ByteBuffer.allocate(4);
            while (fc.read(messageLengthBuf) > 0) {
                final int messageLength = messageLengthBuf.flip().getInt();
                final ByteBuffer messageBuf = ByteBuffer.allocate(messageLength);
                fc.read(messageBuf);
                final VehicleReport report = VehicleReport.parseFrom(messageBuf.flip());
                if (!report.getDeleted()) {
                    temp.put(report.getVin(), report);
                }
            }
        }
        return temp;
    }

    @Override
    public <S extends VehicleReportProtobuf.VehicleReport> S save(S entity) {
         final VehicleReport report = this.reports.put(entity.getVin(), entity);
         if (report != null) {
             return entity;
         } else {
             return null;
         }
    }

    @Override
    public <S extends VehicleReportProtobuf.VehicleReport> Iterable<S> saveAll(Iterable<S> entities) {
        List<S> temp = new ArrayList<S>();
        for (var entity:entities) {
           final VehicleReport report = this.reports.put(entity.getVin(), entity);
           if (report != null) {
               temp.add(entity);
           }
        }
        return temp;
    }

    @Override
    public Optional<VehicleReportProtobuf.VehicleReport> findById(String s) {
        final VehicleReport report = this.reports.get(s);
        if (report == null) {
            return Optional.empty();
        } else {
            return Optional.of(report);
        }
    }

    @Override
    public boolean existsById(String s) {
        return this.reports.containsKey(s);
    }

    @Override
    public Iterable<VehicleReportProtobuf.VehicleReport> findAll() {
        return this.reports.values();
    }

    @Override
    public Iterable<VehicleReportProtobuf.VehicleReport> findAllById(Iterable<String> strings) {
        final List<VehicleReport> temp = new ArrayList<>();
        for (final String key:strings) {
            temp.add(this.reports.get(key));
        }
        return temp;
    }

    @Override
    public long count() {
        return this.reports.size();
    }

    @Override
    public void deleteById(String s) {
        this.reports.remove(s);
    }

    @Override
    public void delete(VehicleReportProtobuf.VehicleReport entity) {
        this.reports.remove(entity.getVin());
    }

    @Override
    public void deleteAll(Iterable<? extends VehicleReportProtobuf.VehicleReport> entities) {
        entities.forEach(k -> {
            this.reports.remove(k.getVin());
        });
    }

    @Override
    public void deleteAll() {
        this.reports.clear();
    }

    @Override
    public Optional<VehicleReport> getVehicleReport(String vin) {
        return this.findById(vin);
    }

    @Override
    public Iterable<VehicleReport> getVehicleReport() {
        return this.findAll();
    }

    @Override
    public void addVehicleReport(VehicleReport report) throws DatabaseWriteException {
        if (this.save(report) == null) {
            log.info("Report did not already exist!");
        }
        if (this.persist) {
            log.info("Submitting write request for report: " + report.getVin());
        }
    }

    @Override
    public void deleteVehicleReport(VehicleReport report) throws DatabaseDeletionException {
        this.delete(report);
    }
}
