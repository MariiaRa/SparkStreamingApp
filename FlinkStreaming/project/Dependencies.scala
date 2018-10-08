import sbt._

object Dependencies {
  val configVersion = "1.3.2"
  val flinkVersion = "1.6.1"

  val config = "com.typesafe" % "config" % configVersion
  val flinkSteaming = "org.apache.flink" %% "flink-streaming-scala" % "1.6.1"
  val flinkClients = "org.apache.flink" %% "flink-clients" % "1.6.1"
  val flinkKafkaConnector = "org.apache.flink" %% "flink-connector-kafka-0.10" % "1.6.1"
  val flinkTable = "org.apache.flink" %% "flink-table" % "1.6.1"
  val flinkCore = "org.apache.flink" % "flink-core" % "1.6.1"
  val flinkHdfsConnector = "org.apache.flink" %% "flink-connector-filesystem" % "1.6.1"
  val flinkJdbc = "org.apache.flink" % "flink-jdbc" % "1.6.1"
  val hadoop = "org.apache.hadoop" % "hadoop-common" % "2.7.3"
  val driver = "org.postgresql" % "postgresql" % "42.2.2"
  val avro = "org.apache.avro" % "avro" % "1.8.2"
  val hdfs = "org.apache.hadoop" % "hadoop-hdfs" % "2.7.3"
  val linkAvro = "org.apache.flink" % "flink-avro" % "1.6.1"

  val appDependencies =
    Seq(config,
      flinkSteaming,
      flinkClients,
      flinkKafkaConnector,
      flinkTable,
      flinkCore,
      flinkJdbc,
      flinkHdfsConnector,
      hadoop,
      hdfs,
      avro,
      linkAvro
    )
}