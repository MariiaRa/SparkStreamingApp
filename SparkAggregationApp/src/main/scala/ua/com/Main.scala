package ua.com

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.apache.spark.sql.functions._

object Main {

  val myConf: Config = ConfigFactory.load()

  val connection: String = myConf.getString("psql.connection")
  val host: String = myConf.getString("psql.host")
  val port: String = myConf.getString("psql.port")
  val db: String = myConf.getString("psql.database")
  val user: String = myConf.getString("psql.user")
  val password: String = myConf.getString("psql.password")
  val driver: String = myConf.getString("psql.driver")
  val url: String = connection + host + ":" + port + "/" + db + "?user=" + user + "&password=" + password
  val data: String = myConf.getString("psql.table.data")
  val location: String = myConf.getString("psql.table.location")

  val hiveTable: String = myConf.getString("hive.table")
  val metastoreUris: String = myConf.getString("hive.metastore")
  val warehouse: String = myConf.getString("hive.warehouse")

  private def getSensorData(sparkSession: SparkSession, tableName: String): DataFrame = {
    val dictionaryDF = sparkSession.read.
      format("jdbc")
      .option("url", url)
      .option("dbtable", tableName)
      .option("driver", driver)
      .load()
    dictionaryDF
  }

  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf().setAppName("SparkAggregateStreaming")
      .set("spark.io.compression.codec", "org.apache.spark.io.SnappyCompressionCodec")
      .set("hive.metastore.uris", metastoreUris)
      .set("spark.sql.warehouse.dir", warehouse)
      .set("hive.exec.dynamic.partition", "true")
      .set("hive.exec.dynamic.partition.mode", "nonstrict")

    val spark = SparkSession.builder.config(sparkConf)
      .appName("SparkAggregation")
      .enableHiveSupport()
      .getOrCreate()

    spark.sparkContext.setLogLevel("ERROR")
    import spark.sql

    val sensorDictionaryOne = getSensorData(spark, data)
    sensorDictionaryOne.createOrReplaceTempView("dictionaryOne")

    val sensorDictionaryTwo = getSensorData(spark, location)
    sensorDictionaryTwo.createOrReplaceTempView("dictionaryTwo")

    sql("SELECT max(rddId) AS rddId FROM sensors").createOrReplaceTempView("rddId")
    val rddId1 = sql("SELECT max(rddID) FROM sensors").first().getAs[Long](0)
    val rddId2 = sql("SELECT max(rddID) FROM aggregation").first().getAs[Long](0)

    if (rddId1 > rddId2) {
      sql(s"SELECT id, max(value) AS max, min(value) AS min, avg(value) AS avg FROM sensors WHERE rddId > $rddId2 GROUP BY id ORDER BY id")
        .createOrReplaceTempView("stats")

      sql("SELECT dictionaryOne.id, dictionaryOne.type, dictionaryTwo.location, stats.max, stats.min, stats.avg " +
        "FROM stats " +
        "JOIN dictionaryOne ON (dictionaryOne.id = stats.id) " +
        "JOIN dictionaryTwo ON (dictionaryTwo.id = stats.id) ORDER BY stats.id")
        .createOrReplaceTempView("aggregated")

      val agg = sql("SELECT * FROM aggregated").toDF().withColumn("rddId", lit(rddId1))
      agg.write.mode(SaveMode.Append).insertInto(hiveTable)
    }
  }
}
