package tech.housemoran.realgood.application.exceptions;

public class DatabaseWriteException extends Exception {
    public DatabaseWriteException(final String message) {
        super(message);
    }
}
