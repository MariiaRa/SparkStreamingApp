package com.ua.Bucketing

import org.apache.flink.streaming.connectors.fs.Clock
import org.apache.flink.streaming.connectors.fs.bucketing.Bucketer
import org.apache.hadoop.fs.Path

class MyBucketer(elem: String) extends Bucketer[String] {

 override def getBucketPath(clock: Clock, basePath: Path, element: String): Path = {
new Path(basePath + "/" + element.split(",")(4))
 }
}
