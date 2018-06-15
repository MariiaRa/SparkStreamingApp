package com

import java.util.Properties

import com.Deserializer.FlinkObjectDeserializer
import com.Entity.{NewSensorData, SensorData}
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.flink.api.common.restartstrategy.RestartStrategies
import org.apache.flink.core.fs.FileSystem.WriteMode
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011
import org.apache.flink.table.api.TableEnvironment

object FlinkStream {

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
    // create a checkpoint every 5 seconds
    env.enableCheckpointing(5000)

    val myConsumer = new FlinkKafkaConsumer011[SensorData](topic, new FlinkObjectDeserializer(), properties)
   myConsumer.setStartFromEarliest() // start from the earliest record possible
    val stream = env.addSource(myConsumer)

    val transformed: DataStream[NewSensorData] = stream.map {
      val batchId = System.currentTimeMillis()
      sensor: SensorData => NewSensorData(sensor.id, sensor.value, sensor.input_time, sensor.ip, batchId)
    }
    transformed.print()

  /*  tableEnv.registerDataStream("rawData", transformed)

    val result = tableEnv.sqlQuery(
      "SELECT id, metrics FROM rawData GROUP BY id")
*/

  val table = tableEnv.fromDataStream(transformed)

      import org.apache.flink.table.sinks.CsvTableSink

    table.writeToSink(
        new CsvTableSink(
          "/home/maria/Downloads/csv_sink.csv",
          fieldDelim = ",",
          numFiles = 1,
          writeMode = WriteMode.OVERWRITE))

    env.execute("Reading from Kafka 0.10")
  }

}
