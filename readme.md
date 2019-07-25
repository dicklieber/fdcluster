### Field Day Logging

Disgtributed, serverless Field Day Logging system.

Goals:

* All nodes in sync
* As long as one node exists, no data loss.
* Each node can have native or provide Web access to clients
* Free open-source


sbt:
stage
universal:packageBin

Creates target/universal/fdlog-1.0.zip