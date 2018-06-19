package com.ua.Deserializer

import java.nio.charset.StandardCharsets

import com.ua.Entity.SensorData
import org.apache.flink.api.common.serialization.DeserializationSchema
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.java.typeutils.TypeExtractor

class FlinkObjectDeserializer  extends DeserializationSchema[SensorData] {

  private def parseKafkaMessage(input: String): SensorData = {
    val columns = input.split(",")
    val sensor = SensorData(columns(0), columns(1).toDouble, columns(2).replace(".", ""), columns(3))
    sensor
  }

  override def isEndOfStream(nextElement: SensorData): Boolean = false

  override def deserialize(bytes: Array[Byte]): SensorData = {
    val str = new String(bytes, StandardCharsets.UTF_8)
    val obj = parseKafkaMessage(str)
    obj
  }

  override def getProducedType: TypeInformation[SensorData] = TypeExtractor.getForClass(classOf[SensorData])
}
