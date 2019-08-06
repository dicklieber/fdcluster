name := "fdlog"

version := "1.0"

maintainer := "wa9nnn@u505.com"

lazy val `fdlog` = (project in file(".")).enablePlugins(JavaAppPackaging)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

//resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

scalaVersion := "2.12.4"

mainClass in (Compile, run) := Some("org.wa9nnn.fdlog.javafx.entry.FdLog")


unmanagedJars in (Compile, run) += Attributed.blank(file(System.getenv("JAVA_HOME") + "/lib/ext/jfxrt.jar"))

//addCompilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full)

libraryDependencies ++= Seq(jdbc, ehcache, ws, specs2 % Test, guice,
  "org.scalafx" %% "scalafx" % "8.0.192-R14",
  "com.jsuereth" %% "scala-arm" % "2.0",
  "net.codingwell" %% "scala-guice" % "4.2.6",
//  "com.typesafe.akka" %% "akka-actor" % "2.5.23",
  "com.typesafe.akka" %% "akka-actor" % "2.6.0-M5",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
)


//unmanagedResourceDirectories in Test +=  baseDirectory ( _ /"target/web/public/test" )

      