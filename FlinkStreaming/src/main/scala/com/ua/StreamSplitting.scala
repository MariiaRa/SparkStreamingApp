package com.ua

import java.util.Properties
import com.typesafe.config.{Config, ConfigFactory}
import com.ua.Bucketing.MyTypedBucketer
import com.ua.Deserializer.FlinkObjectDeserializer
import com.ua.Entity.{NewSensorData, SensorData}
import org.apache.flink.api.common.restartstrategy.RestartStrategies
import org.apache.flink.streaming.connectors.fs.bucketing.BucketingSink
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010

object Enricher {
  def addField(input: SensorData): NewSensorData =
    NewSensorData(
      input.id,
      input.value,
      input.input_time,
      input.ip,
      System.currentTimeMillis())
}

object StreamSplitting {
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
    env.getConfig.disableSysoutLogging
    env.getConfig.setRestartStrategy(RestartStrategies.fixedDelayRestart(4, 10000))
    env.enableCheckpointing(2000).setParallelism(4)

    //create consumer
    val myConsumerTyped = new FlinkKafkaConsumer010[SensorData](topic, new FlinkObjectDeserializer(), properties)

    //read from kafka
    val streamTyped: DataStream[SensorData] = env.addSource(myConsumerTyped)

    //transform stream
    val transformedStream = streamTyped.rebalance.map {
      record =>
        Enricher.addField(record)
    }

    //split streams
    val splittedStream = transformedStream.split { record: NewSensorData =>
      record.id.contains('T') match {
        case true => List("temperature")
        case false => List("rest")
      }
    }

    //select data stream from splitted streams
    val temperatureSensors = splittedStream select "temperature"
    val restOfSensors = splittedStream select "rest"

    val sinkOne = new BucketingSink[NewSensorData]("hdfs://alpha.gemelen.net:8020/flink/temperature")
    val sinkTwo = new BucketingSink[NewSensorData]("hdfs://alpha.gemelen.net:8020/flink/rest")

    sinkOne.setBucketer(new MyTypedBucketer())
    sinkTwo.setBucketer(new MyTypedBucketer())

    sinkOne.setBatchRolloverInterval(1 * 30 * 1000)
    sinkTwo.setBatchRolloverInterval(1 * 30 * 1000)
    temperatureSensors.addSink(sinkOne).setParallelism(4) //max parallelism is number of task managers * task slots
    restOfSensors.addSink(sinkTwo).setParallelism(2) //we can set different level of parallelism for streams

    env.execute("Reading from Kafka and splitting stream")
  }
}

