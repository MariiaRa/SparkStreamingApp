import sbt._

object Dependencies {
  val spartCore = "org.apache.spark" %% "spark-core" % "2.3.0"
  val sparkSQL = "org.apache.spark" %% "spark-sql" % "2.3.0"
  val config = "com.typesafe" % "config" % "1.3.2"
  val sparkStreaming = "org.apache.spark" %% "spark-streaming" % "2.3.0"
  val hive = "org.apache.spark" % "spark-hive_2.11" % "2.3.0"
  val hadoopClient = "org.apache.hadoop" % "hadoop-client" % "2.7.5"
  val hiveJdbc = "org.apache.hive" % "hive-jdbc" % "2.3.0"
  val sparkTest = "com.holdenkarau" %% "spark-testing-base" % "2.3.0_0.9.0" % Test
  val postgreSQL = "org.postgresql" % "postgresql" % "42.2.2"

  val appDependencies =
    Seq(spartCore, config, sparkStreaming, sparkSQL, hive, hadoopClient, hiveJdbc, sparkTest, postgreSQL)
}
