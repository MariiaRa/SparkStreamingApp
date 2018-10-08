package com.ua.Entity

import com.typesafe.config.{Config, ConfigFactory}
import com.ua.Serializer.AvroObjectSerializer
import org.apache.avro.generic.GenericRecord
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer010

object KafkaSink {

  val myConf: Config = ConfigFactory.load()
  val topic: String = myConf.getString("kafka-remote.toTopic")
  val brokers: String = myConf.getString("kafka-remote.brokers")
  val myProducer = new FlinkKafkaProducer010[GenericRecord](
    brokers, // broker list
    topic, // target topic
    new AvroObjectSerializer()) // serialization schema

  myProducer.setWriteTimestampToKafka(true)
}
