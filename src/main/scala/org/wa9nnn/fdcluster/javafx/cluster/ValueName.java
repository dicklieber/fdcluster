package org.wa9nnn.fdcluster.javafx.cluster;

/**
 * In order that willl appear in Cluster Table.
 */
public enum ValueName implements PropertyCellName {
//    Header("", ""),
    Node("Source"),
    HTTP("A link to that node."),
    QsoCount("Number of QSOs at the node. Should  be the same across all nodes."),
    FdHours("Number of hours with QSOs"),
    Band("That this station is operating on ."),
    Mode("CW, PH or DI"),
    Operator("Who is running the station. User provided in the Station Panel."),
    Rig("Free-form text. User provided in the Station Panel."),
    Antenna("Free-form text. User provided in the Station Panel."),
    Journal("Name of the file where this node stores QSOs. Should  be the same across all nodes."),
    CallSign("CallSign of your station. Should  be the same across all nodes."),
    Contest("FD or WFD"),
    OS("Operating system. Can be different for each node."),
    OpCount("Number of operators at this node. At least 1, more if web clients."),

    Stamp("When the node reported this information."),
    Version("Version of main. Not good to mix version of main!");

    private final String display;
    private final String toolTip;

    ValueName(String toolTip, String display) {
        this.display = display;
        this.toolTip = toolTip;
    }

    ValueName(String toolTip) {
        this.display = name();
        this.toolTip = toolTip;
    }

    public String getDisplay() {
        return display;
    }

    public String toolTip() {
        return toolTip;
    }
}
