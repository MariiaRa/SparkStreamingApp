package ua.com

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.streaming.{Seconds, StreamingContext}

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
      .set("hive.metastore.uris", "thrift://alpha.gemelen.ent:9083")
      .set("spark.sql.warehouse.dir", "hdfs://alpha.gemelen.net:8020/user/hive/warehouse")

    val ssc = new StreamingContext(sparkConf, Seconds(10))
    ssc.sparkContext.setLogLevel("ERROR")

    val sparkSession = SparkSession.builder
      .appName("SparkAppStreaming")
      .getOrCreate()
    val sensorDictionaryOne = getSensorData(sparkSession, "sensor_data")
    sensorDictionaryOne.createOrReplaceTempView("dictionaryOne")

    val dictionaryOne = sparkSession.sql("SELECT * FROM dictionaryOne")
    dictionaryOne.show()

    val sensorDictionaryTwo = getSensorData(sparkSession, "sensor_data")
    sensorDictionaryTwo.createOrReplaceTempView("dictionaryTwo")

    val dictionaryTwo = sparkSession.sql("SELECT * FROM dictionaryTwo")
    dictionaryTwo.show()
  }

}
