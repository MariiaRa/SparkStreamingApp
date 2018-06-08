import sbt._

name := "Sensors"
organization := "software.sigma"
version := "0.1"
scalaVersion := "2.11.12"

val akkaVersion = "2.5.9"

//akka actors
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % "10.0.11",
  "joda-time" % "joda-time" % "2.9.9",
  "com.typesafe" % "config" % "1.3.2",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"
)
