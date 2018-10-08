package com.ua

import java.util._

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.flink.api.common.restartstrategy.RestartStrategies
import org.apache.flink.streaming.connectors.fs.bucketing.BucketingSink
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010
import com.ua.Bucketing.MyTypedBucketer
import com.ua.Deserializer.FlinkObjectDeserializer
import com.ua.Entity.{NewSensorData, SensorData}

class Main {
  val myConf: Config = ConfigFactory.load()
  val brokers: String = myConf.getString("kafka-remote.brokers")
  val groupID: String = myConf.getString("kafka-remote.groupID")
  val topic: String = myConf.getString("kafka-remote.fromTopic")
  val properties = new Properties()
  properties.setProperty("bootstrap.servers", brokers)
  properties.setProperty("group.id", groupID)

  def main(args: Array[String]): Unit = {
    import org.apache.flink.streaming.api.scala._

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //env.getConfig.disableSysoutLogging
    env.getConfig.setRestartStrategy(RestartStrategies.fixedDelayRestart(4, 10000))
    env.enableCheckpointing(5000)
    env.setParallelism(4) //we have 4 task managers with 1 task slop per each

    val myConsumerTyped = new FlinkKafkaConsumer010[SensorData](topic, new FlinkObjectDeserializer(), properties)
    println("Connecting to kafka.")

    val streamTyped: DataStream[SensorData] = env.addSource(myConsumerTyped)
    println("Streaming.")

    val transformed = streamTyped.map {
      record =>
        NewSensorData(
          record.id,
          record.value,
          record.input_time,
          record.ip,
          System.currentTimeMillis())
    }


    val sink = new BucketingSink[NewSensorData]("hdfs://alpha.gemelen.net:8020/flink/buckets")
    sink.setBucketer(new MyTypedBucketer())
    //sink.setBatchSize(1024 * 1024 * 400); // this is 400 MB,
    sink.setBatchRolloverInterval(1 * 30 * 1000); // this is 30 sec
    transformed.addSink(sink)

    env.execute("Reading from Kafka and writing to hdfs")
  }
}
