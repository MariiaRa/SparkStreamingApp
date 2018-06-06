import sbt._

object Dependencies {
  val spartCore = "org.apache.spark" %% "spark-core" % "2.0.0"
  val sparkSQL = "org.apache.spark" %% "spark-sql" % "2.0.0"
  val config = "com.typesafe" % "config" % "1.3.2"
  val sparkStreaming = "org.apache.spark" %% "spark-streaming" % "2.0.0"
  val hive = "org.apache.spark" %% "spark-hive" % "2.0.0"
  val hadoopClient = "org.apache.hadoop" % "hadoop-client" % "2.7.1"
  val hiveJdbc = "org.apache.hive" % "hive-jdbc" % "1.2.1"
  val postgreSQL = "org.postgresql" % "postgresql" % "42.2.2"

  val appDependencies =
    Seq(spartCore, config, sparkStreaming, sparkSQL, hive, hadoopClient, hiveJdbc, postgreSQL)
}
