package jpen.demo.files;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Clock {
    private static final DateTimeFormatter dateFilenameFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    public static long currentUnixTime() {
        return System.currentTimeMillis();
    }

    public static String currentDateTimeAsFilename() {
        return LocalDateTime.now().format(dateFilenameFormat);
    }

    public static String calculatePeriodAsString(long t1, long t2) {
        var diff = t2 - t1;

        Duration t = Duration.ofMillis(diff);

        var millis = String.format("%03d", t.toMillisPart()).substring(0, 3);

        return String.format("%d:%d:%d.%s", t.toHours(), t.toMinutesPart(), t.toSecondsPart(), millis);
    }
}
