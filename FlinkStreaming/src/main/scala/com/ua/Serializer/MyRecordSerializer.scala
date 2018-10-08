package com.ua.Serializer

import com.esotericsoftware.kryo.{Kryo, Serializer}
import com.esotericsoftware.kryo.io.{Input, Output}
import org.apache.avro.Schema
import org.apache.avro.Schema.Parser
import org.apache.avro.generic.{GenericData, GenericRecord}
import collection.JavaConverters._

class MyRecordSerializer extends Serializer[GenericRecord] {
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

  override def write(kryo: Kryo, output: Output, `object`: GenericRecord): Unit = {
    val fields = `object`.getSchema.getFields.asScala
    val values = fields.map(f => `object`.get(f.name())).toList
    output.writeString(values(0).toString)
    output.writeDouble(values(1).asInstanceOf[Double])
    output.writeString(values(2).toString)
    output.writeString(values(3).toString)
    output.writeLong(values(4).asInstanceOf[Long])
  }

  override def read(kryo: Kryo, input: Input, `type`: Class[GenericRecord]): GenericRecord = {
    val avroRecord: GenericRecord = new GenericData.Record(schema)
    val fields = avroRecord.getSchema.getFields.asScala
    avroRecord.put(fields(0).name(), input.readString())
    avroRecord.put(fields(1).name(), input.readDouble())
    avroRecord.put(fields(2).name(), input.readString())
    avroRecord.put(fields(3).name(), input.readString())
    avroRecord.put(fields(4).name(), input.readLong())
    avroRecord
  }
}