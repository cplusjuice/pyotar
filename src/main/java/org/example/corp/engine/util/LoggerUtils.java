package org.example.corp.engine.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.*;
import java.util.stream.Collectors;

import static java.util.logging.Level.WARNING;

public class LoggerUtils {

    private static final Logger nonFileLogger = Logger.getLogger(LoggerUtils.class.getName());

    public static boolean isFileLoggingEnabled = true;
    public static String logDir = "log/";
    public static String logName = "game_log";

    private static FileHandler handler;

    public static final String DEFAULT_ST_SEPARATOR = "\n";

    private static void createFileHandler(String path) throws IOException {
        handler = new FileHandler(path);
        Formatter formatter = new SimpleFormatter();
        handler.setFormatter(formatter);
    }

    @SuppressWarnings("rawtypes")
    public synchronized static Logger getLogger(Class clazz) {
        Logger logger = Logger.getLogger(clazz.getName());
        if (isFileLoggingEnabled) {
            if (handler == null) {
                File dir = new File(logDir);
                if (!dir.exists()) {
                    if (!dir.mkdir()) {
                        nonFileLogger.warning("Unable to create dir " + logDir);
                        return logger;
                    }
                    try {
                        createFileHandler(logDir + logName + ".0");
                    } catch (IOException e) {
                        nonFileLogger.log(WARNING, "Unable to create log file", e);
                        return logger;
                    }
                }
                try {
                    List<Integer> logNumbers = Files.list(dir.toPath()).filter(p -> !Files.isDirectory(p))
                            .map(p -> Integer.parseUnsignedInt(new LinkedList<>(
                                    Arrays.asList(p.toString().split("\\."))
                            ).getLast())).sorted().collect(Collectors.toList()); // Is it even worth xD?
                    if (logNumbers.isEmpty()) {
                        createFileHandler(logDir + logName + ".0");
                    } else {
                        int logNumber = logNumbers.get(logNumbers.size() - 1) + 1;
                        createFileHandler(logDir + logName + "." + logNumber);
                    }
                } catch (IOException e) {
                    nonFileLogger.log(WARNING, "Unable to create log file", e);
                    return logger;
                }
            }
            logger.addHandler(handler);
        }
        return Logger.getLogger(clazz.getName());
    }

    /**
     * Prints stack trace of current thread to the provided StringBuilder
     * @param builder builder to print stack trace
     * @param separator separating string between stack trace elements
     */
    public static void printStackTraceToString(StringBuilder builder, String separator) {
        if (builder == null)
            return;

        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : elements) {
            builder.append(element);
            if (element != elements[elements.length - 1])
                builder.append(separator);
        }
    }

    public static void printStackTraceToString(StringBuilder builder) {
        printStackTraceToString(builder, DEFAULT_ST_SEPARATOR);
    }

    public static String printStackTraceToString(String separator) {
        StringBuilder builder = new StringBuilder();
        printStackTraceToString(builder, separator);
        return builder.toString();
    }

    public static String printStackTraceToString() {
        return printStackTraceToString(DEFAULT_ST_SEPARATOR);
    }

    public static String printVertexArrayToString(float[] vertices, int vecSize) {
        StringBuilder builder = new StringBuilder();
        for (int x = 0; x < vertices.length; x += vecSize) {
            for (int y = 0; y < vecSize; y++) {
                builder.append(vertices[x + y]).append(", ");
            } builder.append("\n");
        }

        return builder.toString();
    }
}
