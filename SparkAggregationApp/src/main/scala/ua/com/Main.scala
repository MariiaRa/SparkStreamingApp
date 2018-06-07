package ua.com

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}

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

  val hiveTable: String = myConf.getString("hive.table")

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
      .set("hive.metastore.uris", "thrift://delta.gemelen.net:9083")
      .set("spark.sql.warehouse.dir", "hdfs://alpha.gemelen.net:8020/apps/hive/warehouse")
      .set("hive.exec.dynamic.partition", "true")
      .set("hive.exec.dynamic.partition.mode", "nonstrict")

    val spark = SparkSession.builder.config(sparkConf)
      .appName("SparkAggregation")
      .enableHiveSupport()
      .getOrCreate()

    spark.sparkContext.setLogLevel("ERROR")
    import spark.sql

    val sensorDictionaryOne = getSensorData(spark, "sensor_data")
    sensorDictionaryOne.createOrReplaceTempView("dictionaryOne")

    val sensorDictionaryTwo = getSensorData(spark, "sensor_location")
    sensorDictionaryTwo.createOrReplaceTempView("dictionaryTwo")

    sql("SELECT max(rddId) AS rddId FROM sensors").createOrReplaceTempView("rddId")

    val rddId1 = sql("SELECT max(rddID) FROM sensors").first().getAs[Long](0)
    val rddId2 = sql("SELECT max(rddID) FROM aggregation").first().getAs[Long](0)

    //sql("CREATE TABLE IF NOT EXISTS aggregation (id STRING, type String, location String, max DECIMAL(10,4), min DECIMAL(10,4), avg DECIMAL(10,4)) PARTITIONED BY (rddId BIGINT)")

    if (rddId1 > rddId2) {
      sql(s"SELECT id, max(value) AS max, min(value) AS min, avg(value) AS avg FROM sensors WHERE rddId > $rddId2 GROUP BY id ORDER BY id")
        .createOrReplaceTempView("stats")

      sql("SELECT * FROM  ( SELECT dictionaryOne.id, dictionaryOne.type, dictionaryTwo.location, stats.max, stats.min, stats.avg " +
        "FROM dictionaryOne " +
        "JOIN dictionaryTwo ON (dictionaryOne.id = dictionaryTwo.id) " +
        "JOIN stats ON (dictionaryOne.id = stats.id) ORDER BY dictionaryOne.id ) CROSS JOIN rddId ")
        .createOrReplaceTempView("aggregated")

      val agg = sql("SELECT * FROM aggregated")
      agg.write.mode(SaveMode.Append).insertInto(hiveTable)
    }
  }
}
