
lazy val root = (project in file(".")).
  settings(
    name := "SparkAppStream",
    version := "1.0",
    scalaVersion := "2.11.12",
   // mainClass in Compile := Some("com.Main")
  )

resolvers ++= Seq(
  "apache-snapshots" at "http://repository.apache.org/snapshots/",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs@_*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "2.3.0",
  "org.apache.spark" %% "spark-sql" % "2.3.0",
  "com.holdenkarau" %% "spark-testing-base" % "2.3.0_0.9.0" % Test,
  "org.apache.hadoop" % "hadoop-hdfs" % "2.5.2",
  "org.postgresql" % "postgresql" % "42.2.2",
  "com.typesafe" % "config" % "1.3.2",
"org.apache.spark" %% "spark-streaming-kafka" % "1.6.3",
  "org.apache.spark" %% "spark-streaming" % "2.3.0" % "provided",
  "org.apache.spark" %% "spark-sql-kafka-0-10" % "2.3.0",
 "org.apache.spark" %% "spark-streaming-kafka-0-10" % "2.3.0",
  "joda-time" % "joda-time" % "2.9.9"
)
//logging
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"


