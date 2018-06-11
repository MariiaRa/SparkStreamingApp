import sbt._

object Dependencies {
  val sparkVersion = "2.0.0"
  val configVersion = "1.3.2"
  val hiveJdbcVersion = "1.2.1"
  val postgreSQLVersion = "42.2.2"

  val sparkCore = "org.apache.spark" %% "spark-core" % sparkVersion
  val sparkSQL = "org.apache.spark" %% "spark-sql" % sparkVersion
  val config = "com.typesafe" % "config" % "1.3.2"
  val sparkStreaming = "org.apache.spark" %% "spark-streaming" % sparkVersion
  val hive = "org.apache.spark" %% "spark-hive" % sparkVersion
  val hiveJdbc = "org.apache.hive" % "hive-jdbc" % hiveJdbcVersion
  val postgreSQL = "org.postgresql" % "postgresql" % postgreSQLVersion

  val appDependencies =
    Seq(sparkCore, config, sparkStreaming, sparkSQL, hive, hiveJdbc, postgreSQL)
}
