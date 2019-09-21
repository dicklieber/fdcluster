name := "fdlog"

version := "0.2"

maintainer := "wa9nnn@u505.com"

lazy val `fdlog` = (project in file(".")).enablePlugins(JavaAppPackaging)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

//resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

scalaVersion := "2.12.8"

mainClass in(Compile, run) := Some("org.wa9nnn.fdlog.javafx.entry.FdLog")


scalacOptions in(Compile, doc) ++= Seq("-verbose")

//unmanagedJars in (Compile, run) += Attributed.blank(file(System.getenv("JAVA_HOME") + "/lib/ext/jfxrt.jar"))

import scala.util.Properties

val osType: SettingKey[String] = SettingKey[String]("osType")

osType := {
  if (Properties.isLinux)
    "linux"
  else if (Properties.isMac)
    "mac"
  else if (Properties.isWin)
    "win"
  else
    throw new Exception(s"unknown os: ${Properties.osName}")
}

val javafxLib = file(sys.env.get("JAVAFX_LIB").getOrElse("Environmental variable JAVAFX_LIB is not set"))
lazy val akkaHttpVersion = "10.1.9"
lazy val akkaVersion    = "2.6.0"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-json" % "2.8.0-M4",
  "org.specs2" %% "specs2-core" % "4.6.0" % "test",
  "com.google.inject" % "guice" % "4.2.2",
  "org.scalafx" %% "scalafx" % "8.0.192-R14",
  "com.jsuereth" %% "scala-arm" % "2.0",
  "net.codingwell" %% "scala-guice" % "4.2.6",
  "com.typesafe.akka" %% "akka-actor" % "2.6.0-M5",
  "com.typesafe.akka" %% "akka-http" % "10.1.9",
  "com.typesafe.akka" %% "akka-stream"          % akkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  // JavaFX 11 jars are distributed for each platform
  "org.openjfx" % "javafx-controls" % "11.0.1" classifier osType.value,
  "org.openjfx" % "javafx-graphics" % "11.0.1" classifier osType.value,
  "org.openjfx" % "javafx-media" % "11.0.1" classifier osType.value,
  "org.openjfx" % "javafx-base" % "11.0.1" classifier osType.value,
  "nl.grons" %% "metrics-scala" % "4.0.0",

)

