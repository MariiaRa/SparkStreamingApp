package ua.com

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}

object Main {

  val myConf: Config = ConfigFactory.load()
  val connection: String = myConf.getString("psql.connection")
  val host: String = myConf.getString("psql.host")
  val port: String = myConf.getString("psql.port")
  val db: String = myConf.getString("psql.database")
  val user: String = myConf.getString("psql.user")
  val password: String = myConf.getString("psql.password")
  val driver: String = myConf.getString("psql.driver")

  val url: String = connection + host + ":" + port + "/" + db

  def getSensorData(sparkSession: SparkSession, tableName: String): DataFrame = {
    val dictionaryDataDF = sparkSession.read.
      format("jdbc")
      .option("url", url)
      .option("dbtable", tableName)
      .option("user", user)
      .option("password", password)
      .option("driver", driver)
      .load()
    dictionaryDataDF
  }

  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf().setAppName("SparkAggregateStreaming")
      .set("spark.io.compression.codec", "org.apache.spark.io.SnappyCompressionCodec")
      .set("hive.metastore.uris", "thrift://delta.gemelen.net:9083")
      .set("spark.sql.warehouse.dir", "hdfs://alpha.gemelen.net:8020/apps/hive/warehouse")

    val spark = SparkSession.builder.config(sparkConf)
      .appName("SparkAggregation")
      .enableHiveSupport()
      .getOrCreate()

    spark.sparkContext.setLogLevel("ERROR")
    import spark.sql

    val sensorDictionaryOne = getSensorData(spark, "sensor_data")
    sensorDictionaryOne.createOrReplaceTempView("dictionaryOne")
    val dictionaryOne = sql("SELECT * FROM dictionaryOne")
    dictionaryOne.show()

    val sensorDictionaryTwo = getSensorData(spark, "sensor_location")
    sensorDictionaryTwo.createOrReplaceTempView("dictionaryTwo")
    val dictionaryTwo = sql("SELECT * FROM dictionaryTwo")
    dictionaryTwo.show()

    val dfHive = sql("SELECT * FROM sensors")
    sql("SELECT max(rddID) AS rddId FROM metrics").createOrReplaceTempView("rddId")
    val rddId1 = sql("SELECT max(rddID) FROM metrics").first().getAs[Long](0)

    sql("SELECT id, max(value) AS max, min(value) AS min, avg(value) AS avg FROM metrics GROUP BY id ORDER BY id").createOrReplaceTempView("stats")
    sql("SELECT * FROM stats").show()
    //sql("CREATE TABLE IF NOT EXISTS aggregation (id STRING, type String, location String, max DECIMAL(10,4), min DECIMAL(10,4), avg DECIMAL(10,4)) PARTITIONED BY (rddId BIGINT)")

    val rddId2 = sql("SELECT max(rddID) FROM aggregation").first().getAs[Long](0)

    if (rddId1 > rddId2) {
      sql("SELECT dictionaryOne.id, dictionaryOne.type, dictionaryTwo.location, stats.max, stats.min, stats.avg" +
        "FROM dictionaryOne " +
        "JOIN dictionaryTwo ON (dictionaryOne.id = dictionaryTwo.id) " +
        "JOIN stats ON (dictionaryOne.id = stats.id) "
      ).createOrReplaceTempView("aggregated")
    }
    sql("SELECT * FROM aggregated").show()
  }
}
