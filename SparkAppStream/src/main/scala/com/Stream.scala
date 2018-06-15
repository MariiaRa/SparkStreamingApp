package com

import com.Deserializer.ObjectDeserializer
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
  val metastoreUris: String = myConf.getString("hive.metastore")
  val warehouse: String = myConf.getString("hive.warehouse")
  val checkpointFolder: String = myConf.getString("spark.checkpoint")
  val topics = Set("spark")
  val kafkaParams = Map[String, Object](
    ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> brokers,
    ConsumerConfig.GROUP_ID_CONFIG -> groupID,
    ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
    ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[ObjectDeserializer]
  )

  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf().setAppName("SparkAppStream")
      .set("spark.io.compression.codec", "org.apache.spark.io.SnappyCompressionCodec")
      .set("hive.metastore.uris", metastoreUris)
      .set("spark.sql.warehouse.dir", warehouse)
      .set("hive.exec.dynamic.partition", "true")
      .set("hive.exec.dynamic.partition.mode", "nonstrict")

    val ssc = new StreamingContext(sparkConf, Seconds(10))
    ssc.sparkContext.setLogLevel("ERROR")
    ssc.checkpoint(checkpointFolder)
    val messages = KafkaUtils.createDirectStream[String, SensorData](
      ssc,
      PreferConsistent,
      Subscribe[String, SensorData](topics, kafkaParams)
    ).map(_.value())

    messages.foreachRDD { rdd =>
      val batchId = System.currentTimeMillis()
      val spark = SparkSession.builder.config(rdd.sparkContext.getConf)
        .enableHiveSupport()
        .getOrCreate()

      import org.apache.spark.sql.functions._

      val sensorDF: DataFrame = spark.createDataFrame(rdd)
      val transformed = sensorDF.withColumn("rddId", lit(batchId))
      transformed.write.mode(SaveMode.Append).insertInto(hiveTable)
    }

    ssc.start()
    ssc.awaitTermination()
  }
}
