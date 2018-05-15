package com

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.util.StatCounter
object Stream {

  def parse(rdd: org.apache.spark.rdd.RDD[String]): org.apache.spark.rdd.RDD[SensorDate] = {
    var keys = rdd.map(_.split(","))
    val prices = keys.map(p => SensorDate(p(0).split("=")(1), p(2).split("=")(1).toDouble, p(1).split("=")(1).replace(".", ""), p(3).split("=")(1)))
    prices
  }

  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf().setAppName("SparkAppStreaming")
    val ssc = new StreamingContext(sparkConf, Seconds(20))

    val topics = Set("spark_app")

    // Create direct kafka stream with brokers and topics
    // Enable checkpointing
    ssc.checkpoint("_checkpoint")

    val kafkaParams = Map[String, Object](
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> "10.0.3.8:9092,10.0.3.9:9092,10.0.3.10:9092",
      ConsumerConfig.GROUP_ID_CONFIG -> "test-consumer-group",
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer])
    ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "earliest"

    val messages: DStream[String] = KafkaUtils.createDirectStream[String, String](
      ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](topics, kafkaParams)).map(_.value())

    // print messages
    messages.print()

    var parsedStream = messages.mapPartitions {
      rows =>
        val parser = new Parser()
        rows.map { row => parser.parse(row) }
    }

    var reducedStream = parsedStream
      .map(record => (record.id, record.value.toDouble))
      .reduceByKeyAndWindow((acc: Double, x: Double) => acc + x, Seconds(60), Seconds(60))

    var countMetrics = parsedStream.map(record => (record.id, 1L)).reduceByKey(_ + _)

    // Return a sliding window of records per sensor device every 1 min
    var slidingSensorCounts: DStream[(String, Long)] = parsedStream.map(record => record.id)
      .countByValueAndWindow(Seconds(60), Seconds(60))

    // group by key, get statistics for column value
    var keyStatsRDD = parsedStream
      .map(record => (record.id, record.value.toDouble)).groupByKeyAndWindow(Seconds(60), Seconds(60)).
      mapValues(values => StatCounter(values))

    /** DataFrame operations inside your streaming program */
    messages.foreachRDD { rdd =>
      var keys = rdd.map(_.split(","))
      val sensors = keys.map(p => SensorDate(p(0).split("=")(1), p(1).split("=")(1).toDouble, p(2).split("=")(1).replace(".", ""), p(3).split("=")(1)))
      // Get the singleton instance of SparkSession
      val spark = SparkSession.builder.config(rdd.sparkContext.getConf).getOrCreate()
      // Convert RDD[String] to DataFrame
      val sensorDF: DataFrame = spark.createDataFrame(sensors)
      sensorDF.printSchema()
      sensorDF.show()
      // Create a temporary view
      sensorDF.createOrReplaceTempView("sensorsDF")
      // Do word count on DataFrame using SQL and print it
      spark.sql("SELECT * FROM sensorsDF WHERE sensorsDF.id = \"T1\"").show()
    }

    // reducedStream.print()
   slidingSensorCounts.print()
   keyStatsRDD.print()

    // Start the computation
    ssc.start()
    ssc.awaitTermination()
  }
}
