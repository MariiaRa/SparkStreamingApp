import sbt._

object Dependencies {
  val sparkVersion = "2.3.0"
  val configVersion = "1.3.2"
  val flinkVersion = "1.5.0"

  val sparkCore = "org.apache.spark" %% "spark-core" % sparkVersion
  val sparkSQL = "org.apache.spark" %% "spark-sql" % sparkVersion
  val config = "com.typesafe" % "config" % configVersion
  val sparkStreaming = "org.apache.spark" %% "spark-streaming" % sparkVersion
  val sparkStreamingKafka_010 = "org.apache.spark" %% "spark-streaming-kafka-0-10-assembly" % sparkVersion
  val hive = "org.apache.spark" %% "spark-hive" % sparkVersion
  val flink = "org.apache.flink" %% "flink-scala" % flinkVersion
  val flinkSteaming = "org.apache.flink" %% "flink-streaming-scala" % flinkVersion
  val flinkClients = "org.apache.flink" %% "flink-clients" % flinkVersion
  val flinkKafkaConnector = "org.apache.flink" %% "flink-connector-kafka-0.11" % flinkVersion
  val flinkTable = "org.apache.flink" %% "flink-table" % flinkVersion
  val flinkCore = "org.apache.flink" % "flink-core" % flinkVersion
  val flinkHdfsConnector = "org.apache.flink" %% "flink-connector-filesystem" % flinkVersion
  val flinkJdbc = "org.apache.flink" % "flink-jdbc" % "1.5.0"

  val appDependencies =
    Seq(sparkCore, sparkStreaming, sparkStreamingKafka_010, sparkSQL, hive, config, flink, flinkSteaming, flinkClients, flinkKafkaConnector
      , flinkTable, flinkCore, flinkJdbc)
}