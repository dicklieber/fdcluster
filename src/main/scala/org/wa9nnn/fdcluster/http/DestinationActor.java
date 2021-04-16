package org.wa9nnn.fdcluster.http;

/**
 * Where an http message should be sent.
 */
public enum DestinationActor {
    qsoStore, // generally messages with Qsos
    cluster // mesages with Uuids.
}
