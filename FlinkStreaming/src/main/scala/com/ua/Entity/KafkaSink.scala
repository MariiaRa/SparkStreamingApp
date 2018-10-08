package com.ua.Entity

import com.typesafe.config.{Config, ConfigFactory}
import com.ua.Serializer.AvroObjectSerializer
import org.apache.avro.Schema
import org.apache.avro.Schema.Parser
import org.apache.avro.generic.GenericRecord
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer010

object KafkaSink {

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

  val myConf: Config = ConfigFactory.load()
  val topic: String = myConf.getString("kafka-remote.toTopic")
  val brokers: String = myConf.getString("kafka-remote.brokers")
  val myProducer = new FlinkKafkaProducer010[GenericRecord](
    brokers, // broker list
    topic, // target topic
    new AvroObjectSerializer(schema)) // serialization schema

  myProducer.setWriteTimestampToKafka(true)
}
