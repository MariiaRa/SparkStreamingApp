package com

import com.Entity.SensorData
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.{Seconds, StreamingContext}

object Stream {

  val myConf: Config = ConfigFactory.load()
  val brokers: String = myConf.getString("kafka.brokers")
  val groupID: String = myConf.getString("kafka.groupID")
  val hiveTable: String = myConf.getString("hive.table")
  val topics = Set("spark")
  val kafkaParams = Map[String, Object](
    ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> brokers,
    ConsumerConfig.GROUP_ID_CONFIG -> groupID,
    ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
    ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
    ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "latest"
  )

  private def parseKafkaMessage(rdd: org.apache.spark.rdd.RDD[String], time: Long): org.apache.spark.rdd.RDD[SensorData] = {
    val columns = rdd.map(_.split(","))
    val sensorKafkaData = columns.map(column => SensorData(column(0).split("=")(1), column(1).split("=")(1).toDouble,
      column(2).split("=")(1).replace(".", ""), column(3).split("=")(1), time))
    sensorKafkaData
  }

  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf().setAppName("SparkAppStream")
      .set("spark.io.compression.codec", "org.apache.spark.io.SnappyCompressionCodec")
      .set("hive.metastore.uris", "thrift://delta.gemelen.net:9083")
      .set("spark.sql.warehouse.dir", "hdfs://alpha.gemelen.net:8020/apps/hive/warehouse")
      .set("hive.exec.dynamic.partition", "true")
      .set("hive.exec.dynamic.partition.mode", "nonstrict")

    val ssc = new StreamingContext(sparkConf, Seconds(10))
    ssc.sparkContext.setLogLevel("ERROR")

    val messages = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaParams)
    ).map(_.value())

    messages.foreachRDD { rdd =>
      val batchId = System.currentTimeMillis()
      val sensors = parseKafkaMessage(rdd, batchId)

      val spark = SparkSession.builder.config(rdd.sparkContext.getConf)
        .enableHiveSupport()
        .getOrCreate()

      // Convert RDD[String] to DataFrame
      val sensorDF: DataFrame = spark.createDataFrame(sensors)
      sensorDF.show()

      sensorDF.write.mode(SaveMode.Append).insertInto(hiveTable)

    }
    ssc.start()
    ssc.awaitTermination()
  }
}
