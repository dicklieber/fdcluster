name := "fdcluster"

version := "0.2"

maintainer := "wa9nnn@u505.com"


lazy val `fdcluster` = (project in file("."))
  .enablePlugins(JavaAppPackaging, GitPlugin, BuildInfoPlugin).settings(
    buildInfoKeys ++= Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion,
      git.gitCurrentTags, git.gitCurrentBranch, git.gitHeadCommit, git.gitHeadCommitDate, git.baseVersion,
      BuildInfoKey.action("buildTime") {
        System.currentTimeMillis
      } // re-computed each time at compile)
    ),
//    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion, git.gitCurrentTags, git.gitCurrentBranch),
    buildInfoPackage := "org.wa9nnn.fdcluster"
  )

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

//resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

scalaVersion := "2.13.0"

mainClass in(Compile, run) := Some("org.wa9nnn.fdcluster.javafx.entry.FdCluster")


scalacOptions in(Compile, doc) ++= Seq("-verbose")

//unmanagedJars in (Compile, run) += Attributed.blank(file(System.getenv("JAVA_HOME") + "/lib/ext/jfxrt.jar"))

import scala.util.Properties


//lazy val root = (project in file(".")).
//  enablePlugins(BuildInfoPlugin).
//  settings(
//    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
//    buildInfoPackage := "hello"
//  )
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

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-json" % "2.8.0-M4",
  "org.specs2" %% "specs2-core" % "4.6.0" % "test",
  "com.google.inject" % "guice" % "4.2.2",
  "org.scalafx" %% "scalafx" % "12.0.2-R18",
  "net.codingwell" %% "scala-guice" % "4.2.6",
  "com.typesafe.akka" %% "akka-actor" % "2.6.0-M7",
  "com.typesafe.akka" %% "akka-http" % "10.1.10",
  "com.typesafe.akka" %% "akka-stream" % "2.6.0-M7",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  // JavaFX 11 jars are distributed for each platform
  "org.openjfx" % "javafx-controls" % "11.0.1" classifier osType.value,
  "org.openjfx" % "javafx-graphics" % "11.0.1" classifier osType.value,
  "org.openjfx" % "javafx-media" % "11.0.1" classifier osType.value,
  "org.openjfx" % "javafx-base" % "11.0.1" classifier osType.value,
  "nl.grons" %% "metrics4-scala" % "4.1.1",
  "de.heikoseeberger" %% "akka-http-play-json" % "1.29.1"
)

