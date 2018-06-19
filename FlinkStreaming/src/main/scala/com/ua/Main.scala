package com.ua

import java.util.Properties

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.flink.api.common.restartstrategy.RestartStrategies
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.core.fs.FileSystem.WriteMode
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011
import org.apache.flink.table.api.TableEnvironment
import org.apache.flink.table.sinks.CsvTableSink

object Main {
  val myConf: Config = ConfigFactory.load()
  val brokers: String = myConf.getString("kafka.brokers")
  val groupID: String = myConf.getString("kafka.groupID")
  val topic: String = myConf.getString("kafka.topic")

  val properties = new Properties()
  properties.setProperty("bootstrap.servers", brokers)
  properties.setProperty("group.id", groupID)

  def main(args: Array[String]): Unit = {
    import org.apache.flink.streaming.api.scala._
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val tableEnv = TableEnvironment.getTableEnvironment(env)
    env.getConfig.disableSysoutLogging
    env.getConfig.setRestartStrategy(RestartStrategies.fixedDelayRestart(4, 10000))
    env.enableCheckpointing(5000)

    // val myConsumer = new FlinkKafkaConsumer011[SensorData](topic, new FlinkObjectDeserializer(), properties)
    val myConsumer = new FlinkKafkaConsumer011[String](topic, new SimpleStringSchema, properties)

    myConsumer.setStartFromEarliest() // start from the earliest record possible
    val stream: DataStream[String] = env.addSource(myConsumer)
    val transformed = stream.map(elem => elem + "," + System.currentTimeMillis())

    val table = tableEnv.fromDataStream(transformed)
    table.writeToSink(
      new CsvTableSink(
        "hdfs://alpha.gemelen.net:8020/apps/flink/sink1.csv",
        fieldDelim = ",",
        numFiles = 1,
        writeMode = WriteMode.OVERWRITE))

    env.execute("Reading from Kafka")
  }
}
