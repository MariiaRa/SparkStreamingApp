package com

import com.Entity.SensorData
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.sql._
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.{Seconds, StreamingContext}

object Stream {

  val myConf: Config = ConfigFactory.load()
  val brokers: String = myConf.getString("kafka.brokers")
  val groupID: String = myConf.getString("kafka.groupID")
  val topics = Set("spark_app")

  val kafkaParams = Map[String, Object](
    ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> brokers,
    ConsumerConfig.GROUP_ID_CONFIG -> groupID,
    ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
    ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
    ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "latest"
  )

  def parseKafkaMessage(rdd: org.apache.spark.rdd.RDD[String]): org.apache.spark.rdd.RDD[SensorData] = {
    val columns = rdd.map(_.split(","))
    val sensorKafkaData = columns.map(p => SensorData(p(0).split("=")(1), p(1).split("=")(1).toDouble,
      p(2).split("=")(1).replace(".", ""), p(3).split("=")(1)))
    sensorKafkaData
  }

  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf().setAppName("SparkAppStreaming")
      .set("spark.io.compression.codec", "org.apache.spark.io.SnappyCompressionCodec")
      .set("hive.metastore.uris", "thrift://alpha.gemelen.ent:9083")
      .set("spark.sql.warehouse.dir", "hdfs://alpha.gemelen.net:8020/user/hive/warehouse")

    val ssc = new StreamingContext(sparkConf, Seconds(10))
    ssc.sparkContext.setLogLevel("ERROR")

    val messages = KafkaUtils.createDirectStream[String, String](
      ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](topics, kafkaParams)).map(_.value())

    messages.print()

    messages.foreachRDD { rdd =>

      val sensors = parseKafkaMessage(rdd)

      val spark = SparkSession.builder.config(rdd.sparkContext.getConf)
        .enableHiveSupport()
        .getOrCreate()
      // Convert RDD[String] to DataFrame
      val sensorDF: DataFrame = spark.createDataFrame(sensors)
      sensorDF.show()

      import spark.sql
      sensorDF.write.mode(SaveMode.Append).saveAsTable("sensor_metrics")
      sql("SELECT * FROM sensor_metrics").show()
    }
    ssc.start()
    ssc.awaitTermination()
  }
}
