package com.ua.Bucketing

import org.apache.flink.streaming.connectors.fs.Clock
import org.apache.flink.streaming.connectors.fs.bucketing.Bucketer
import org.apache.hadoop.fs.Path

class MyBucketer extends Bucketer[String] {

  override def getBucketPath(clock: Clock, basePath: Path, element: String): Path = {

    val formatDate = new java.text.SimpleDateFormat("yyyy-MM-dd-HH-mm")
    val date = formatDate.format(new java.util.Date())

    new Path(basePath + "/" + date + "/" + element.split(",")(4))
  }
}




