name := "fdcluster"

maintainer := "wa9nnn@u505.com"


lazy val `fdcluster` = (project in file("."))
  .enablePlugins(JlinkPlugin, GitPlugin, BuildInfoPlugin, SbtTwirl).settings(
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

scalaVersion := "2.13.5"

mainClass in(Compile, run) := Some("org.wa9nnn.fdcluster.javafx.entry.FdCluster")


scalacOptions in(Compile, doc) ++= Seq("-verbose", "-Ymacro-annotations")

//unmanagedJars in (Compile, run) += Attributed.blank(file(System.getenv("JAVA_HOME") + "/lib/ext/jfxrt.jar"))

import scala.util.Properties


sourceDirectories in (Compile, TwirlKeys.compileTemplates) := (unmanagedSourceDirectories in Compile).value

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
  "com.wa9nnn" %% "util" % "0.0.1-SNAPSHOT",
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

jlinkModules := {
  jlinkModules.value :+ "jdk.unsupported"
}

jlinkOptions := {
  jpackage {
    imageOptions = listOf("--icon", "src/main/resources/java.icns")
  }
}

jlinkIgnoreMissingDependency := JlinkIgnore.only(
  "afu.org.checkerframework.checker.formatter" -> "afu.org.checkerframework.checker.formatter.qual",
  "afu.org.checkerframework.checker.nullness" -> "afu.org.checkerframework.checker.nullness.qual",
  "afu.org.checkerframework.checker.regex" -> "afu.org.checkerframework.dataflow.qual",
  "afu.org.checkerframework.checker.regex" -> "afu.org.checkerframework.framework.qual",
  "ch.qos.logback.classic" -> "javax.servlet.http",
  "ch.qos.logback.classic.boolex" -> "groovy.lang",
  "ch.qos.logback.classic.boolex" -> "org.codehaus.groovy.control",
  "ch.qos.logback.classic.boolex" -> "org.codehaus.groovy.reflection",
  "ch.qos.logback.classic.boolex" -> "org.codehaus.groovy.runtime",
  "ch.qos.logback.classic.boolex" -> "org.codehaus.groovy.runtime.callsite",
  "ch.qos.logback.classic.boolex" -> "org.codehaus.groovy.runtime.typehandling",
  "ch.qos.logback.classic.gaffer" -> "groovy.lang",
  "ch.qos.logback.classic.gaffer" -> "org.codehaus.groovy.control",
  "ch.qos.logback.classic.gaffer" -> "org.codehaus.groovy.control.customizers",
  "ch.qos.logback.classic.gaffer" -> "org.codehaus.groovy.reflection",
  "ch.qos.logback.classic.gaffer" -> "org.codehaus.groovy.runtime",
  "ch.qos.logback.classic.gaffer" -> "org.codehaus.groovy.runtime.callsite",
  "ch.qos.logback.classic.gaffer" -> "org.codehaus.groovy.runtime.typehandling",
  "ch.qos.logback.classic.gaffer" -> "org.codehaus.groovy.runtime.wrappers",
  "ch.qos.logback.classic.gaffer" -> "org.codehaus.groovy.transform",
  "ch.qos.logback.classic.helpers" -> "javax.servlet",
  "ch.qos.logback.classic.helpers" -> "javax.servlet.http",
  "ch.qos.logback.classic.selector.servlet" -> "javax.servlet",
  "ch.qos.logback.classic.servlet" -> "javax.servlet",
  "ch.qos.logback.core.boolex" -> "org.codehaus.janino",
  "ch.qos.logback.core.joran.conditional" -> "org.codehaus.commons.compiler",
  "ch.qos.logback.core.joran.conditional" -> "org.codehaus.janino",
  "ch.qos.logback.core.net" -> "javax.mail",
  "ch.qos.logback.core.net" -> "javax.mail.internet",
  "ch.qos.logback.core.status" -> "javax.servlet",
  "ch.qos.logback.core.status" -> "javax.servlet.http",
  "com.codahale.metrics.health.jvm" -> "com.codahale.metrics.jvm",
  "com.rabbitmq.client.impl" -> "io.micrometer.core.instrument",
  "com.sun.media.jfxmediaimpl.platform" -> "com.sun.media.jfxmediaimpl.platform.ios",
  "org.checkerframework.checker.formatter" -> "org.checkerframework.checker.formatter.qual",
  "org.checkerframework.checker.nullness" -> "org.checkerframework.checker.nullness.qual",
  "org.checkerframework.checker.regex" -> "org.checkerframework.dataflow.qual",
  "org.checkerframework.checker.regex" -> "org.checkerframework.framework.qual",
  "org.joda.time" -> "org.joda.convert",
  "org.joda.time.base" -> "org.joda.convert",
  "org.reflections.serializers" -> "com.google.gson",
  "org.reflections.serializers" -> "org.dom4j",
  "org.reflections.serializers" -> "org.dom4j.io",
  "org.reflections.util" -> "javax.servlet",
  "org.reflections.vfs" -> "org.apache.commons.vfs2",
  "org.scalafx.extras" -> "javafx.embed.swing",
  "scalafx" -> "javafx.embed.swing",
  "scalafx" -> "javafx.scene.web",
  "scalafx.embed.swing" -> "javafx.embed.swing",
  "scalafx.scene.web" -> "javafx.scene.web",
  "scalafxml.core" -> "javafx.fxml",

)