package com.ua

import java.util.Properties
import com.typesafe.config.{Config, ConfigFactory}
import com.ua.Bucketing.MyBucketer
import com.ua.Deserializer.FlinkObjectDeserializer
import com.ua.Entity._
import com.ua.Serializer.MyRecordSerializer
import org.apache.avro.Schema
import org.apache.avro.Schema.Parser
import org.apache.avro.generic.{GenericData, GenericRecord}
import org.apache.flink.api.common.restartstrategy.RestartStrategies
import org.apache.flink.streaming.connectors.fs.bucketing.BucketingSink
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010


object Transformation {
  def addField(input: SensorData): NewSensorData =
    NewSensorData(input.id, input.value, input.ip, input.input_time, System.currentTimeMillis())
}

object Serialization {

  def main(args: Array[String]): Unit = {
    import org.apache.flink.streaming.api.scala._
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //env.getConfig.disableSysoutLogging
    env.getConfig.setRestartStrategy(RestartStrategies.fixedDelayRestart(4, 10000))
    env.enableCheckpointing(5000)
    env.setParallelism(4)

    val serializer: MyRecordSerializer = new MyRecordSerializer()
    env.getConfig.addDefaultKryoSerializer(classOf[GenericData], classOf[MyRecordSerializer])
    env.getConfig.registerTypeWithKryoSerializer(classOf[GenericData], serializer.getClass)

    val myConf: Config = ConfigFactory.load()
    val brokers: String = myConf.getString("kafka-remote.brokers")
    val topic: String = myConf.getString("kafka-remote.fromTopic")
    val groupID: String = myConf.getString("kafka-remote.groupID")

    val properties = new Properties()
    properties.setProperty("bootstrap.servers", brokers)
    properties.setProperty("group.id", groupID)

    val typedConsumer = new FlinkKafkaConsumer010[SensorData](topic, new FlinkObjectDeserializer, properties)
    val typesStream: DataStream[SensorData] = env.addSource(typedConsumer)

    val transformed = typesStream.rebalance.map {
      elem =>
        Transformation.addField(elem)
    }

    val sink = new BucketingSink[GenericRecord]("hdfs://alpha.gemelen.net:8020/flink/generic_records")
    sink.setBucketer(new MyBucketer())
    sink.setBatchRolloverInterval(1 * 20 * 1000); // this is 20 sec

    val avroRecords = transformed.map {
      message =>
        val str =
          """{
          "namespace": "kakfa-avro.test",
          "type": "record",
          "name": "sensor",
          "fields":[
            {
              "name": "id", "type": "string"
            },
            {
              "name": "value",  "type": "double"
            },
            {
              "name": "input_time", "type": "string"
            },
            {
              "name": "ip", "type": "string"
            },
            {
              "name": "time_bucket", "type": "long"
            }
          ]
        }"""
        lazy val schema: Schema = new Parser().parse(str)
        lazy val avroRecord = GRBuilder.build(schema)
        avroRecord.put("id", message.id)
        avroRecord.put("value", message.metrics)
        avroRecord.put("input_time", message.input_time)
        avroRecord.put("ip", message.ip)
        avroRecord.put("time_bucket", message.time_bucket)
        avroRecord
    }


    //avroRecords.addSink(KafkaSink.myProducer).setParallelism(2)

    avroRecords.addSink(sink).setParallelism(4)

    env.execute("Reading from Kafka and writing generic record to Kafka")
  }
}
