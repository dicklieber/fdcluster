import sbtbuildinfo.BuildInfoPlugin.autoImport.buildInfoOptions
import ReleaseTransformations._
import com.typesafe.sbt.packager.SettingsHelper.makeDeploymentSettings


maintainer := "wa9nnn@u505.com"

enablePlugins(JavaAppPackaging, GitPlugin, BuildInfoPlugin, SbtTwirl, UniversalPlugin)
buildInfoKeys ++= Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion, maintainer,
  git.gitCurrentTags, git.gitCurrentBranch, git.gitHeadCommit, git.gitHeadCommitDate, git.baseVersion)
buildInfoPackage := "org.wa9nnn.fdcluster"

buildInfoOptions ++= Seq(
  BuildInfoOption.ToJson,
  BuildInfoOption.BuildTime,
  BuildInfoOption.Traits("org.wa9nnn.fdcluster.BuildInfoBase")
)


Compile / sourceDirectories := (Compile / unmanagedSourceDirectories).value
Compile / mainClass := Some("org.wa9nnn.fdcluster.javafx.FdCluster")
Compile / discoveredMainClasses := Seq()

//scalacOptions ++= Seq(
//  "-encoding", "utf8", // Option and arguments on same line
//  "-Xfatal-warnings",  // New lines for each options
////  "-deprecation",
//  "-unchecked",
//  "-language:implicitConversions",
//  "-language:higherKinds",
//  "-language:existentials",
//  "-language:existentials",
//  "-language:postfixOps"
//)

// wix build information
//wixProductId := "268963af-6f14-445a-bcc7-21775b5bdcc5"
//wixProductUpgradeId := "6b10420e-df5b-4c6c-9ca0-c12daf4b239d"


scalaVersion := "2.13.5"

lazy val fdcluster = (project in file("."))
  .settings(
    name := "fdcluster"

  )
lazy val javaFXModules = {
  // Determine OS version of JavaFX binaries
  lazy val osName = System.getProperty("os.name") match {
    case n if n.startsWith("Linux") => "linux"
    case n if n.startsWith("Mac") => "mac"
    case n if n.startsWith("Windows") => "win"
    case _ =>
      throw new Exception("Unknown platform!")
  }
  // Create dependencies for JavaFX modules
  Seq("base", "controls", "graphics", "media")
    .map(m => "org.openjfx" % s"javafx-$m" % "15.0.1" classifier osName)
}

//libraryDependencies ++= javaFXModules

javaOptions in Test += "-Dconfig.file=conf/test.conf"

val javafxLib = file(sys.env.get("JAVAFX_LIB").getOrElse("Environmental variable JAVAFX_LIB is not set"))
lazy val akkaHttpVersion = "10.2.4"
val logbackVersion = "1.2.3"

libraryDependencies ++= Seq(
  "com.wa9nnn" %% "util" % "0.0.11-SNAPSHOT",
  "com.wa9nnn" %% "cabrillo-lib" % "1.0.3-SNAPSHOT",
  "com.typesafe.play" %% "play-json" % "2.9.2",
  "org.specs2" %% "specs2-core" % "4.6.0" % "test",
  "org.specs2" %% "specs2-mock" % "4.6.0" % "test",
  "com.google.inject" % "guice" % "4.2.2",
  "org.scalafx" %% "scalafx" % "16.0.0-R22",
  "org.scalafx" %% "scalafx-extras" % "0.3.6",
  "net.codingwell" %% "scala-guice" % "4.2.6",
  "com.github.racc" % "typesafeconfig-guice" % "0.1.0",
  "com.typesafe.akka" %% "akka-actor" % "2.6.0-M7",
  "com.github.kxbmap" %% "configs" % "0.6.0",
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream" % "2.6.4",
  "com.typesafe.akka" %% "akka-slf4j" % "2.6.4",
  "ch.qos.logback" % "logback-classic" % logbackVersion,
  "ch.qos.logback" % "logback-core" % logbackVersion,
  "net.logstash.logback" % "logstash-logback-encoder" % "6.6",
  "com.github.andyglow" %% "typesafe-config-scala" % "1.1.0" % Compile,
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "fr.davit" %% "akka-http-metrics-prometheus" % "1.6.0",
  "io.prometheus" % "simpleclient_hotspot" % "0.11.0",
  "commons-io" % "commons-io" % "2.8.0",
  "org.apache.commons" % "commons-math3" % "3.6.1",
  "javax.servlet" % "javax.servlet-api" % "3.0.1",
  "commons-codec" % "commons-codec" % "1.15",
  "org.apache.commons" % "commons-text" % "1.9",
  "com.sandinh" %% "akka-guice" % "3.3.0",
//  "org.dhatim.io.dropwizard" % "dropwizard-metrics-elasticsearch" % "1.0.9",
  "com.linagora" %  "metrics-elasticsearch-reporter" % "6.0.0-RC3",

)


resolvers += ("spray repo" at "http://repo.spray.io").withAllowInsecureProtocol(true)

resolvers += ("Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/").withAllowInsecureProtocol(true)
//resolvers += ("Sonatype Nexus Repository Manager" at  "http://192.168.0.205:8081/repository/maven-snapshots").withAllowInsecureProtocol(true)
resolvers += ("Sonatype Nexus Repository Manager" at  "http://localhost:8081/repository/maven-snapshots").withAllowInsecureProtocol(true)

//publishTo := Some("Artifactory Realm" at "https://wa9nnn.jfrog.io/artifactory/wa9nnn")
//credentials += Credentials(Path.userHome / ".sbt" / "jfrog.credentials")
makeDeploymentSettings(Universal, packageBin in Universal, "zip")

//Compile / packageDoc := Seq.empty
mappings in(Compile, packageDoc) := Seq()


releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies, // : ReleaseStep
  inquireVersions, // : ReleaseStep
  runClean, // : ReleaseStep
  runTest, // : ReleaseStep
  setReleaseVersion, // : ReleaseStep
  commitReleaseVersion, // : ReleaseStep, performs the initial git checks
  tagRelease, // : ReleaseStep
  ReleaseStep(releaseStepTask(Universal / packageBin)),

  //  publishArtifacts,                       // : ReleaseStep, checks whether `publishTo` is properly set up
  setNextVersion, // : ReleaseStep
  commitNextVersion, // : ReleaseStep
  pushChanges // : ReleaseStep, also checks that an upstream branch is properly configured
)