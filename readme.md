# Field Day Logging

Distributed, serverless, Field Day Logging system.

Goals:

* All nodes in sync
* Nodes can enter or leave and will resync automatically
* Each node can have native (or provide Web access to clients Maybe)
* Free open-source
* Runs on Linux, Mac or Microsoft Windows
* Raspberry Pi Support (download image, copy to SD card)


#Building
  
## Zip file without the JVM. 
You can run this anywhere but you must install th JVM yourself.
`sbt universal:packageBin`

Creates target/universal/fdcluster-1.0.zip

## macOS
`sbt universal:packageOsxDmg`

## Microsoft Windows
This will bundle the JVM. Must run on Windows.

`sbt windows:packageBin`

