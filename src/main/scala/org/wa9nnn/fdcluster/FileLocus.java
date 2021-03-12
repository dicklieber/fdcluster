package org.wa9nnn.fdcluster;

public enum FileLocus {
    var("var"),
    journalFile("journal.json"),
    contest("CONTEST"),
    logs("logDir"),
    logFile("logFile");

    private final String pathPiece;

    FileLocus(String pathPiece) {

        this.pathPiece = pathPiece;
    }

    public String getPathPiece() {
        return pathPiece;
    }
}
