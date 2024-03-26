package jpen.demo.files;

import jpen.demo.configuration.Configuration;
import jpen.demo.model.PointTime;
import jpen.demo.model.SavedPoint;
import jpen.owner.multiAwt.AwtPenToolkit;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

public final class FileManager {
    private final String filePath = Configuration.get().getFilesPath();
    private final String delimiter = "\t";
    private final Point2D.Double inputRatio;

    private File file;

    public FileManager() {
        inputRatio = Configuration.get().getInputRatio();
    }

    public void writeToFile(ArrayList<SavedPoint> data, Map<Long, Long> availableMsData, long startTime, Point center) {
        createFile();
        addHeader();

        try (var writer = new FileWriter(file, true)) {
            var previousTime = data.getFirst().getTime();

            long registerPeriodSum = 0;
            long schedulePeriodSum = 0;

            for (var p : data) {
                var availableMsAtTime = Optional.ofNullable(availableMsData.get(p.systemTime()));

                registerPeriodSum += (p.registeredTime() - previousTime.registeredTime());
                schedulePeriodSum += (p.scheduledTime() - previousTime.scheduledTime());

                write(writer, p, startTime, center, previousTime, availableMsAtTime);

                previousTime = p.getTime();
            }

            logAveragePeriod(registerPeriodSum, schedulePeriodSum, data.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createFile() {
        var filename = Clock.currentDateTimeAsFilename();

        try {
            var folder = Files.createDirectories(Paths.get(filePath));

            var pathSeparator = File.separator;
            file = new File(String.format("%s%s%s.txt", folder, pathSeparator, filename));

            var isCreated = file.createNewFile();

            if (isCreated) {
                System.out.printf("The file %s is created\n", file.getPath());
            } else {
                System.out.printf("The file %s cannot be created\n", file.getPath());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addHeader() {
        var config = Configuration.get();

        int period = 1000 / AwtPenToolkit.getPenManager().pen.getFrequency();

        var tabletRate = formatProperty("Tablet rate", config.getTabletRate());
        var screenDimension = formatProperty("Screen dimension", config.getScreenDimension());
        var tabletSize = formatProperty("Tablet size", config.getTabletSize());
        var periodAsString = formatProperty("Period", period);

        var propertiesInfo = joinWithNewLine(tabletRate, screenDimension, tabletSize, periodAsString);

        var header = String.join(
                delimiter,
                "registeredTime",
                "scheduledTime",
                "registeredDelta",
                "scheduledDelta",
                "availableMs",
                "x",
                "y",
                "tiltX",
                "tiltY",
                "pressure"
        );

        safelyWriteLn(String.format("%s\n\n%s", propertiesInfo, header));
    }

    private String formatProperty(String name, Object value) {
        return String.format("# %s %s", name, value);
    }

    private String joinWithNewLine(String... values) {
        return String.join("\n", values);
    }

    private void safelyWriteLn(String data) {
        try (var writer = new FileWriter(file, true)) {
            writer.write(data + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void write(FileWriter writer, SavedPoint point, long startTime, Point center, PointTime previousTimestamp, Optional<Long> availableMs) {
        try {
            var formattedPoint = point.asRecordedRow(center, inputRatio, startTime, previousTimestamp, availableMs, delimiter);

            writer.write(formattedPoint + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logAveragePeriod(long overallRegisterPeriod, long overallSchedulePeriod, int pointsNumber) {
        var averageRegister = overallRegisterPeriod / pointsNumber;
        var averageSchedule = overallSchedulePeriod / pointsNumber;

        System.out.printf("average register period=%s\naverage schedule period=%s", averageRegister, averageSchedule);
    }
}
