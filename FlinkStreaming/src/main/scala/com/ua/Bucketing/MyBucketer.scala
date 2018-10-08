package com.ua.Bucketing

import java.text.SimpleDateFormat
import java.util.Date
import org.apache.avro.generic.GenericRecord
import org.apache.flink.streaming.connectors.fs.Clock
import org.apache.flink.streaming.connectors.fs.bucketing.Bucketer
import org.apache.hadoop.fs.Path

class MyBucketer extends Bucketer[GenericRecord] {

  override def getBucketPath(clock: Clock, basePath: Path, element: GenericRecord): Path = {

    val formatDate = new SimpleDateFormat("yyyy-MM-dd-HH")
    val date = formatDate.format(new Date())
    val batchId = System.currentTimeMillis()
    new Path(basePath + s"/dateid=$date/batchId=${ (System.currentTimeMillis()/1000)/60}")
  }
}




