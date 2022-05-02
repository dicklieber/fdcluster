# FDCluster - Field Day Logging

Distributed, serverless, shared-nothing. Field Day Logging system.

Goals:

* All nodes are equal peers.
* Nodes can enter or leave and will resync automatically
* Each node can have native (or provide Web access to clients, sometime)
* Free open-source
* Runs on Mac, Microsoft Windows and Linux

#Technologies
* Written in [Scala](https://www.scala-lang.org) with a few Java enums.
* UI is [ScalaFx](http://www.scalafx.org). a nice scala wrapper for [JavaFx](https://openjfx.io)
* Nodes in cluster discover each other via TCP/IP [Multicast](https://en.wikipedia.org/wiki/Multicast).
* [Akka](https://akka.io) actors are used within each node to manage messages.
* HTTP client and Server using [AKKA-HTTP](https://doc.akka.io/docs/akka-http/current/index.html).
* Dependency injection using [Googe Guice](https://github.com/google/guice).
* Configuration via [Typesafe config](https://github.com/lightbend/config) using [HOCON](https://en.wikipedia.org/wiki/HOCON) syntax.




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

Artifacts published to: https://wa9nnn.jfrog.io/

Source code at: https://github.com/dicklieber/fdcluster

Discuission at: https://groups.io/g/FDCluster

#Installation
## Raspberry Pi
### Install Java

https://bell-sw.com/pages/downloads/#mn Dowload the 32bit ARM *Full* verfsion deb file

