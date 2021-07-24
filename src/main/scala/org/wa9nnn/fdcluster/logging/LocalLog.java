package org.wa9nnn.fdcluster.logging;

public enum LocalLog {
    local(true),
    nolocal(false);

    boolean localflag;

    LocalLog(boolean b) {
        localflag = b;
    }
}
