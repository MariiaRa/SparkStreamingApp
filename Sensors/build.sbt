import sbt._

name := "Sensors"
organization := "software.sigma"
version := "0.1"
scalaVersion := "2.11.12"

val akkaVersion = "2.5.9"
val akkaHttpVersion = "10.0.11"
val jodaVersion = "2.9.9"
val configVersion = "1.3.2"
val scalaLoggingVersion = "3.7.2"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "joda-time" % "joda-time" % jodaVersion,
  "com.typesafe" % "config" % configVersion,
  "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion
)
