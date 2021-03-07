package org.wa9nnn.util;

public enum Disposition {
    happy("happy"),
    sad("sad"),
    neutral("");

    private final  String style;
    private Disposition(String happy) {
        style = happy;
    }

    public String getStyle() {
        return style;
    }
}
