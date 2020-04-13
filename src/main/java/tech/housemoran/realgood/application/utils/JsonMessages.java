package tech.housemoran.realgood.application.utils;

public class JsonMessages {

    public static String JsonErrorMessage(final String message) {
        final StringBuilder builder = new StringBuilder();
        return builder.append("{\"error\": {\"message\": ")
                .append("\"")
                .append(message)
                .append("\"}}").toString();
    }
}
