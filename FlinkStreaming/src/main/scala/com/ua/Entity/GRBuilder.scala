package com.ua.Entity

import org.apache.avro.Schema
import org.apache.avro.generic.{GenericData, GenericRecord}

object GRBuilder {
  def build(schema: Schema): GenericRecord = new GenericData.Record(schema)
}
