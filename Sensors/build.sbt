name := "Sensors"
organization := "software.sigma"
version := "0.1"
scalaVersion := "2.11.12"
//akka actors
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.9",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.9" % Test,
  "com.typesafe.akka" %% "akka-stream" % "2.5.9",
  "com.typesafe.akka" %% "akka-http" % "10.0.11",
  "joda-time" % "joda-time" % "2.9.9",
  "com.typesafe" % "config" % "1.3.2"
)
//testing
libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.4"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % "test"
libraryDependencies += "org.specs2" %% "specs2-core" % "4.0.2" % "test"
//logging
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"