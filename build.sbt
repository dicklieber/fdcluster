name := "fdlog"

version := "1.0"

maintainer := "wa9nnn@u505.com"

lazy val `fdlog` = (project in file(".")).enablePlugins(JavaAppPackaging)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

//resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

scalaVersion := "2.12.2"

mainClass in (Compile, run) := Some("org.wa9nnn.fdlog.javafx.entry.FdLog")


unmanagedJars in (Compile, run) += Attributed.blank(file(System.getenv("JAVA_HOME") + "/lib/ext/jfxrt.jar"))

//addCompilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full)

libraryDependencies ++= Seq(jdbc, ehcache, ws, specs2 % Test, guice,
  "org.scalafx" %% "scalafx" % "8.0.192-R14",
  "com.jsuereth" %% "scala-arm" % "2.0"
)


//unmanagedResourceDirectories in Test +=  baseDirectory ( _ /"target/web/public/test" )

      