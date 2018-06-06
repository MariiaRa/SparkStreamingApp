import sbt._

object Dependencies {
  val spartCore = "org.apache.spark" %% "spark-core" % "2.0.0"
  val sparkSQL = "org.apache.spark" %% "spark-sql" % "2.0.0"
  val config = "com.typesafe" % "config" % "1.3.2"
  val sparkStreaming = "org.apache.spark" %% "spark-streaming" % "2.0.0"
  val sparkStreamingKafka_010 = "org.apache.spark" %% "spark-streaming-kafka-0-10-assembly" % "2.0.0"
  val hive = "org.apache.spark" %% "spark-hive" % "2.0.0"

  val appDependencies =
    Seq(spartCore, sparkStreaming, sparkStreamingKafka_010, sparkSQL, hive, config)
}