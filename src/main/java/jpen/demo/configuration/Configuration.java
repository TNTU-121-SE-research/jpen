package jpen.demo.configuration;

import java.awt.geom.Point2D;

public final class Configuration {
    private static Configuration instance;

    private int tabletRate;
    private String filesPath;
    private Size<Integer> screenDimension;
    private Size<Double> tabletSize;
    private Point2D.Double inputRatio;

    private Configuration() {
        setUpSettings();
    }

    private void setUpSettings() {
        tabletRate = 250;
        filesPath = "data";

        var dimensionWidth = 1920;
        var dimensionHeight = 1080;
        var tabletWidth = 344.16;
        var tabletHeight = 193.59;

        screenDimension = new Size<>(dimensionWidth, dimensionHeight);
        tabletSize = new Size<>(tabletWidth, tabletHeight);

        var ratioX = (double) dimensionWidth / tabletWidth;
        var ratioY = (double) dimensionHeight / tabletHeight;
        inputRatio = new Point2D.Double(ratioX, ratioY);
    }

    public static Configuration get() {
        if (instance == null) {
            instance = new Configuration();
        }

        return instance;
    }

    public int getTabletRate() {
        return tabletRate;
    }

    public Size<Integer> getScreenDimension() {
        return screenDimension;
    }

    public Size<Double> getTabletSize() {
        return tabletSize;
    }

    public Point2D.Double getInputRatio() {
        return inputRatio;
    }

    public String getFilesPath() {
        return filesPath;
    }
}
