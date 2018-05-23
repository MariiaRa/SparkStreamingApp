import sbt._

object Dependencies {
  val spartCore = "org.apache.spark" %% "spark-core" % "2.3.0"
  val sparkSQL = "org.apache.spark" %% "spark-sql" % "2.3.0"
  val config = "com.typesafe" % "config" % "1.3.2"
  val sparkStreaming = "org.apache.spark" %% "spark-streaming" % "2.3.0"
  val sparkStreamingKafka_010 = "org.apache.spark" % "spark-streaming-kafka-0-10-assembly_2.11" % "2.3.0"
  val hive = "org.apache.spark" % "spark-hive_2.11" % "2.3.0"
  val hadoopClient = "org.apache.hadoop" % "hadoop-client" % "2.7.5"
  val hiveJdbc = "org.apache.hive" % "hive-jdbc" % "2.3.0"

  val appDependencies =
    Seq(spartCore, config, sparkStreaming, sparkStreamingKafka_010, sparkSQL, hive, hadoopClient, hiveJdbc)
}
