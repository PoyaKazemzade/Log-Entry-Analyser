import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * The LogAnalyzer class analyzes log files and provides statistical information
 * about log entries.
 */

public class LogAnalyzer {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("File Not Found!");
            System.out.println(
                    "Provide at least one log file as the following ==> <java LogAnalyzer.java example.log>");
            System.exit(-1);
        }
        Map<String, Integer> logLevelCount = new HashMap<>();
        Map<String, Integer> messageCount = new HashMap<>();

        for (String file : args) {
            try {
                logFileReader(file, logLevelCount, messageCount);
            } catch (IOException e) {
                System.err.println("Unable to read the file " + file + ": " + e.getMessage());
            }
        }
        logFileProcessor(logLevelCount, messageCount);
    }

    /**
     * Reads the content of a log file and updates log level and message counts.
     *
     * @param file          The path to the log file.
     * @param logLevelCount A map containing log levels and their respective counts.
     * @param messageCount  A map containing log messages and their respective
     *                      counts.
     * @throws IOException If an error occurs while reading the log file.
     */

    public static void logFileReader(String file, Map<String, Integer> logLevelCount,
            Map<String, Integer> messageCount) throws IOException {
        try (BufferedReader reader = customBufferedReader(file)) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    LogEntry logEntry = logEntryMaker(line);
                    logLevelCount.put(logEntry.logLevel(), logLevelCount.getOrDefault(logEntry.logLevel(), 0) + 1);
                    messageCount.put(logEntry.message(), messageCount.getOrDefault(logEntry.message(), 0) + 1);
                } catch (ParseException e) {
                    System.err.println("Unable to read log entry in " + file + ": " + e.getMessage());
                }
            }
        }
    }

    /**
     * Creates a BufferedReader based on the file type (plain text or gzip).
     *
     * @param file The path to the file.
     * @return A BufferedReader for reading the file content.
     * @throws IOException If an error occurs while creating the BufferedReader.
     */

    public static BufferedReader customBufferedReader(String file) throws IOException {
        if (file.endsWith(".gz")) {
            return new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
        } else {
            return Files.newBufferedReader(Paths.get(file));
        }
    }

    /**
     * Creates a LogEntry object from a log entry string.
     *
     * @param line The log entry string.
     * @return A LogEntry object representing the log entry.
     * @throws ParseException If the log entry string cannot be parsed.
     */

    public static LogEntry logEntryMaker(String line) throws ParseException {
        String[] arr = line.split(" ", 3);
        if (arr.length == 3) {
            var level = arr[1];
            var msg = arr[2];
            return new LogEntry(level, msg);
        } else {
            throw new ParseException("This line cannot be a log entry ==> [" + line + "]", 0);
        }
    }

    /**
     * Finds and prints the top three most frequent log messages.
     *
     * @param messageCount A map containing log messages and their respective
     *                     counts.
     */

    public static void topThreeMsgFinder(Map<String, Integer> messageCount) {
        List<Message> logMessages = new ArrayList<>();
        messageCount.forEach((msg, count) -> logMessages.add(new Message(msg, count)));
        logMessages.sort(null);
        for (int i = 0; i < 3 && !logMessages.isEmpty(); i++) {
            Message msgCount = logMessages.get(i);
            System.out.println((i + 1) + ". " + msgCount.logMessage() + ": " + msgCount.count());
        }
    }

    /**
     * Processes the log file data and prints statistical information.
     *
     * @param logLevelCount A map containing log levels and their respective counts.
     * @param messageCount  A map containing log messages and their respective
     *                      counts.
     */

    public static void logFileProcessor(Map<String, Integer> logLevelCount, Map<String, Integer> messageCount) {
        var totalLogEntries = logLevelCount.values().stream().mapToInt(Integer::intValue).sum();
        System.out.printf("Total log entries: %d%n", totalLogEntries);
        System.out.println();
        System.out.println("Log level distribution:");
        logLevelCount.forEach((level, count) -> System.out.println(level + ": " + count));
        System.out.println();
        System.out.println("Top 3 most frequent log messages:");
        topThreeMsgFinder(messageCount);
    }
}