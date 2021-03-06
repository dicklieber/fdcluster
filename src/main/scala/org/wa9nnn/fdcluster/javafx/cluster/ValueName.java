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
    Operator("Who is running the station. User provided in the StationTable Panel."),
    Rig("Free-form text. User provided in the StationTable Panel."),
    Antenna("Free-form text. User provided in the StationTable Panel."),
    Journal("Name of the file where this node stores QSOs. Should  be the same across all nodes."),
    CallSign("CallSign of your station. Should  be the same across all nodes."),
    Contest("FD or WFD"),
    OS("Operating system. Can be different for each node."),
    Sessions("Operators at node. 1st is at FdCluster, other at Web cients."),

    Age("When the node reported this information."),
    Digest("Over NodeStatus."),
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
