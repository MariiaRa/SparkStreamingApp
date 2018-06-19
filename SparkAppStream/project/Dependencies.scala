import sbt._

object Dependencies {
  val sparkVersion = "2.0.0"
  val configVersion = "1.3.2"

  val sparkCore = "org.apache.spark" %% "spark-core" % sparkVersion
  val sparkSQL = "org.apache.spark" %% "spark-sql" % sparkVersion
  val config = "com.typesafe" % "config" % configVersion
  val sparkStreaming = "org.apache.spark" %% "spark-streaming" % sparkVersion
  val sparkStreamingKafka_010 = "org.apache.spark" %% "spark-streaming-kafka-0-10-assembly" % sparkVersion
  val hive = "org.apache.spark" %% "spark-hive" % sparkVersion

  val appDependencies =
    Seq(sparkCore, sparkStreaming, sparkStreamingKafka_010, sparkSQL, hive, config)
}