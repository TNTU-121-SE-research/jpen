package jpen.demo.model;

import jpen.demo.files.Clock;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Locale;

public record SavedPoint(
        long scheduledTime,
        long registeredTime,
        double x,
        double y,
        double tiltX,
        double tiltY,
        double pressure
) {
    public String asRecordedRow(
            Point center,
            Point2D.Double inputRatio,
            long recordingStartTime,
            PointTime previousTime,
            String delimiter
    ) {
        var scheduledTimeSinceStart = Clock.calculatePeriodAsString(recordingStartTime, scheduledTime);
        var registeredTimeSinceStart = Clock.calculatePeriodAsString(recordingStartTime, registeredTime);

        var scheduledTimeDelta = Clock.calculatePeriodAsString(previousTime.scheduledTime(), scheduledTime);
        var registeredTimeDelta = Clock.calculatePeriodAsString(previousTime.registeredTime(), registeredTime);

        var relative = getRelativeTo(center, inputRatio);

        return String.join(delimiter,
                registeredTimeSinceStart,
                scheduledTimeSinceStart,
                registeredTimeDelta,
                scheduledTimeDelta,
                str(relative.x),
                str(relative.y),
                str(tiltX),
                str(tiltY),
                str(pressure)
        );
    }

    private Point2D.Double getRelativeTo(Point center, Point2D.Double inputRatio) {
        // inverts Y
        return new Point2D.Double((x - center.x) / inputRatio.x, ((y - center.y) / inputRatio.y) * -1);
    }

    private String str(Double v) {
        return String.format(Locale.US, "%.8f", v);
    }

    public PointTime getTime() {
        return new PointTime(scheduledTime, registeredTime);
    }
}
