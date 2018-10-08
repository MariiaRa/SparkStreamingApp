package com.ua.Serializer

import org.apache.avro.Schema
import org.apache.avro.generic.{GenericDatumWriter, GenericRecord}
import org.apache.avro.io.EncoderFactory
import org.apache.commons.io.output.ByteArrayOutputStream

class AvroObjectSerializer(schema: Schema) extends org.apache.flink.api.common.serialization.SerializationSchema[GenericRecord] {

  override def serialize(in: GenericRecord): Array[Byte] = {
    val writer = new GenericDatumWriter[GenericRecord](schema)
    val output = new ByteArrayOutputStream()
    val encoder = EncoderFactory.get().binaryEncoder(output, null)
    writer.write(in, encoder)
    encoder.flush()
    output.toByteArray
  }
}