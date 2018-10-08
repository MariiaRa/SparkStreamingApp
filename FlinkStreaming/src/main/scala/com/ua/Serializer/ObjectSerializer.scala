package com.ua.Serializer

import org.apache.avro.Schema
import org.apache.avro.Schema.Parser
import org.apache.avro.generic.{GenericDatumWriter, GenericRecord}
import org.apache.avro.io.EncoderFactory
import org.apache.commons.io.output.ByteArrayOutputStream

class AvroObjectSerializer extends org.apache.flink.api.common.serialization.SerializationSchema[GenericRecord] {

  override def serialize(in: GenericRecord): Array[Byte] = {
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

    val schema: Schema = new Parser().parse(str)
    val writer = new GenericDatumWriter[GenericRecord](schema)
    val output = new ByteArrayOutputStream()
    val encoder = EncoderFactory.get().binaryEncoder(output, null)
    writer.write(in, encoder)
    encoder.flush()
    output.toByteArray
  }
}