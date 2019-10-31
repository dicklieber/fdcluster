### Field Day Logging

Distributed, serverless, Field Day Logging system.

Goals:

* All nodes in sync
* Nodes can enter or leave and will resync automatically
* Each node can have native (or provide Web access to clients Maybe)
* Free open-source
* Runs on Linux, Mac or Microsoft Windows
* Raspberry Pi Support (download image, copy to SD card)

sbt:
stage
universal:packageBin

Creates target/universal/fdcluster-1.0.zip