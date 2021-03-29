name := "fdcluster"

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

//resolvers += ("example-releases" at "http://repo.example.com/releases/")

//resolvers += ("scalaz-bintray" at "http://dl.bintray.com/scalaz/releases").withAllowInsecureProtocol(true)

//resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

scalaVersion := "2.13.0"

mainClass in(Compile, run) := Some("org.wa9nnn.fdcluster.javafx.entry.FdCluster")


scalacOptions in(Compile, doc) ++= Seq("-verbose", "-Ymacro-annotations")

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
val logbackVersion = "1.2.3"

libraryDependencies ++= Seq(
  "com.wa9nnn" %% "cabrillo-lib" % "1.0.2-SNAPSHOT",
  "com.typesafe.play" %% "play-json" % "2.8.0-M4",
  "org.specs2" %% "specs2-core" % "4.6.0" % "test",
  "org.specs2" %% "specs2-mock" % "4.6.0" % "test",
  "com.google.inject" % "guice" % "4.2.2",
  "org.scalafx" %% "scalafx" % "15.0.1-R21",
  "org.scalafx" %% "scalafx-extras" % "0.3.6",
  "net.codingwell" %% "scala-guice" % "4.2.6",
  "com.github.racc" % "typesafeconfig-guice" % "0.1.0",
  "com.typesafe.akka" %% "akka-actor" % "2.6.0-M7",
  "com.github.kxbmap" %% "configs" % "0.6.0",
  "com.typesafe.akka" %% "akka-http" % "10.1.10",
  "com.typesafe.akka" %% "akka-stream" % "2.6.0-M7",
  "ch.qos.logback" % "logback-classic" % logbackVersion,
  "ch.qos.logback" % "logback-core" % logbackVersion,
  "com.github.andyglow" %% "typesafe-config-scala" % "1.1.0" % Compile,
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  // JavaFX 11 jars are distributed for each platform
  "org.openjfx" % "javafx-controls" % "11.0.1" classifier osType.value,
  "org.openjfx" % "javafx-graphics" % "11.0.1" classifier osType.value,
  "org.openjfx" % "javafx-media" % "11.0.1" classifier osType.value,
  "org.openjfx" % "javafx-base" % "11.0.1" classifier osType.value,
  "nl.grons" %% "metrics4-scala" % "4.1.5",
  "de.heikoseeberger" %% "akka-http-play-json" % "1.29.1",
  "io.dropwizard.metrics" % "metrics-core" % "4.1.2",
  "io.dropwizard.metrics" % "metrics-graphite" % "4.1.2",
  "com.fazecast" % "jSerialComm" % "2.6.2",
  "commons-io" % "commons-io" % "2.8.0",
  "org.apache.commons" % "commons-math3" % "3.6.1",
  //  "com.github.jvican" %% "xmlrpc" % "1.2.1"
)

