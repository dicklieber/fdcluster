import sbtbuildinfo.BuildInfoPlugin.autoImport.buildInfoOptions
import ReleaseTransformations._
import com.typesafe.sbt.packager.SettingsHelper.makeDeploymentSettings


maintainer := "wa9nnn@u505.com"

enablePlugins(JavaAppPackaging, GitPlugin, BuildInfoPlugin, SbtTwirl, UniversalDeployPlugin)
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
wixProductId := "268963af-6f14-445a-bcc7-21775b5bdcc5"
wixProductUpgradeId := "6b10420e-df5b-4c6c-9ca0-c12daf4b239d"


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
  "com.wa9nnn" %% "util" % "0.0.7",
  "com.wa9nnn" %% "cabrillo-lib" % "1.0.2",
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
  "com.linagora" %  "metrics-elasticsearch-reporter" % "6.0.0-RC3"
)


//jlinkModules := {
//  jlinkModules.value :+ "jdk.unsupported"
//}

//jlinkIgnoreMissingDependency := JlinkIgnore.only(
//  "afu.org.checkerframework.checker.formatter" -> "afu.org.checkerframework.checker.formatter.qual",
//  "afu.org.checkerframework.checker.nullness" -> "afu.org.checkerframework.checker.nullness.qual",
//  "afu.org.checkerframework.checker.regex" -> "afu.org.checkerframework.dataflow.qual",
//  "afu.org.checkerframework.checker.regex" -> "afu.org.checkerframework.framework.qual",
//  "ch.qos.logback.classic" -> "javax.servlet.http",
//  "ch.qos.logback.classic.boolex" -> "groovy.lang",
//  "ch.qos.logback.classic.boolex" -> "org.codehaus.groovy.control",
//  "ch.qos.logback.classic.boolex" -> "org.codehaus.groovy.reflection",
//  "ch.qos.logback.classic.boolex" -> "org.codehaus.groovy.runtime",
//  "ch.qos.logback.classic.boolex" -> "org.codehaus.groovy.runtime.callsite",
//  "ch.qos.logback.classic.boolex" -> "org.codehaus.groovy.runtime.typehandling",
//  "ch.qos.logback.classic.gaffer" -> "groovy.lang",
//  "ch.qos.logback.classic.gaffer" -> "org.codehaus.groovy.control",
//  "ch.qos.logback.classic.gaffer" -> "org.codehaus.groovy.control.customizers",
//  "ch.qos.logback.classic.gaffer" -> "org.codehaus.groovy.reflection",
//  "ch.qos.logback.classic.gaffer" -> "org.codehaus.groovy.runtime",
//  "ch.qos.logback.classic.gaffer" -> "org.codehaus.groovy.runtime.callsite",
//  "ch.qos.logback.classic.gaffer" -> "org.codehaus.groovy.runtime.typehandling",
//  "ch.qos.logback.classic.gaffer" -> "org.codehaus.groovy.runtime.wrappers",
//  "ch.qos.logback.classic.gaffer" -> "org.codehaus.groovy.transform",
//  "ch.qos.logback.classic.helpers" -> "javax.servlet",
//  "ch.qos.logback.classic.helpers" -> "javax.servlet.http",
//  "ch.qos.logback.classic.selector.servlet" -> "javax.servlet",
//  "ch.qos.logback.classic.servlet" -> "javax.servlet",
//  "ch.qos.logback.core.boolex" -> "org.codehaus.janino",
//  "ch.qos.logback.core.joran.conditional" -> "org.codehaus.commons.compiler",
//  "ch.qos.logback.core.joran.conditional" -> "org.codehaus.janino",
//  "ch.qos.logback.core.net" -> "javax.mail",
//  "ch.qos.logback.core.net" -> "javax.mail.internet",
//  "ch.qos.logback.core.status" -> "javax.servlet",
//  "ch.qos.logback.core.status" -> "javax.servlet.http",
//  "com.codahale.metrics.health.jvm" -> "com.codahale.metrics.jvm",
//  "com.rabbitmq.client.impl" -> "io.micrometer.core.instrument",
//  "com.sun.media.jfxmediaimpl.platform" -> "com.sun.media.jfxmediaimpl.platform.ios",
//  "org.checkerframework.checker.formatter" -> "org.checkerframework.checker.formatter.qual",
//  "org.checkerframework.checker.nullness" -> "org.checkerframework.checker.nullness.qual",
//  "org.checkerframework.checker.regex" -> "org.checkerframework.dataflow.qual",
//  "org.checkerframework.checker.regex" -> "org.checkerframework.framework.qual",
//  "org.joda.time" -> "org.joda.convert",
//  "org.joda.time.base" -> "org.joda.convert",
//  "org.reflections.serializers" -> "com.google.gson",
//  "org.reflections.serializers" -> "org.dom4j",
//  "org.reflections.serializers" -> "org.dom4j.io",
//  "org.reflections.util" -> "javax.servlet",
//  "org.reflections.vfs" -> "org.apache.commons.vfs2",
//  "org.scalafx.extras" -> "javafx.embed.swing",
//  "scalafx" -> "javafx.embed.swing",
//  "scalafx" -> "javafx.scene.web",
//  "scalafx.embed.swing" -> "javafx.embed.swing",
//  "scalafx.scene.web" -> "javafx.scene.web",
//  "scalafxml.core" -> "javafx.fxml",
//  "com.papertrail.profiler.jaxrs" -> "javax.ws.rs"
//
//)
resolvers += ("spray repo" at "http://repo.spray.io").withAllowInsecureProtocol(true)

resolvers += "Artifactory" at "https://wa9nnn.jfrog.io/artifactory/wa9nnn"
resolvers += ("Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/").withAllowInsecureProtocol(true)

publishTo := Some("Artifactory Realm" at "https://wa9nnn.jfrog.io/artifactory/wa9nnn")
credentials += Credentials(Path.userHome / ".sbt" / "jfrog.credentials")
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