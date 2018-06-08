package com.Deserializer

import java.nio.charset.StandardCharsets
import java.util

import com.Entity.SensorData
import org.apache.kafka.common.serialization.Deserializer

class ObjectDeserializer extends Deserializer[SensorData] {
  override def configure(map: util.Map[String, _], b: Boolean): Unit = {}

  override def close(): Unit = {}

  private def parseKafkaMessage(input: String): SensorData = {
    val columns = input.split(",")
    val sensor = SensorData(columns(0), columns(1).toDouble, columns(2).replace(".", ""), columns(3))
    sensor
  }

  override def deserialize(topic: String, bytes: Array[Byte]): SensorData = {
    val str = new String(bytes, StandardCharsets.UTF_8)
    val obj = parseKafkaMessage(str)
    obj
  }
}
