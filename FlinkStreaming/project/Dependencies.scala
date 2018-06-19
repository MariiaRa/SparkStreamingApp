import sbt._

object Dependencies {
  val configVersion = "1.3.2"
  val flinkVersion = "1.5.0"

  val config = "com.typesafe" % "config" % configVersion
  val flink = "org.apache.flink" %% "flink-scala" % flinkVersion
  val flinkSteaming = "org.apache.flink" %% "flink-streaming-scala" % flinkVersion
  val flinkClients = "org.apache.flink" %% "flink-clients" % flinkVersion
  val flinkKafkaConnector = "org.apache.flink" %% "flink-connector-kafka-0.11" % flinkVersion
  val flinkTable = "org.apache.flink" %% "flink-table" % flinkVersion
  val flinkCore = "org.apache.flink" % "flink-core" % flinkVersion
  val flinkHdfsConnector = "org.apache.flink" %% "flink-connector-filesystem" % flinkVersion
  val flinkJdbc = "org.apache.flink" % "flink-jdbc" % "1.5.0"
  val hadoop = "org.apache.hadoop" % "hadoop-common" % "2.7.1"
  val driver = "org.postgresql" % "postgresql" % "42.2.2"

  val appDependencies =
    Seq(config, flink, flinkSteaming, flinkClients, flinkKafkaConnector
      , flinkTable, flinkCore, flinkJdbc, flinkHdfsConnector, hadoop, driver)
}