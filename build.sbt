name := "fdlog"

version := "1.0"

lazy val `fdlog` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

scalaVersion := "2.12.2"


addCompilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full)

libraryDependencies ++= Seq(jdbc, ehcache, ws, specs2 % Test, guice,
  "org.scalafx" %% "scalafx" % "8.0.192-R14"
)


//unmanagedResourceDirectories in Test +=  baseDirectory ( _ /"target/web/public/test" )

      