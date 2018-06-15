package com

import org.apache.flink.api.common.typeinfo.BasicTypeInfo
import org.apache.flink.api.java.io.jdbc.JDBCInputFormat
import org.apache.flink.api.java.typeutils.RowTypeInfo
import org.apache.flink.api.scala.{DataSet, ExecutionEnvironment}
import org.apache.flink.core.fs.FileSystem.WriteMode
import org.apache.flink.table.api.{Table, Types}
import org.apache.flink.table.sinks.CsvTableSink
import org.apache.flink.table.sources.CsvTableSource
import org.apache.flink.types.Row

object FlinkAggregate {

  def main(args: Array[String]): Unit = {
    import org.apache.flink.table.api.TableEnvironment
    val env = ExecutionEnvironment.getExecutionEnvironment
    val tableEnv = TableEnvironment.getTableEnvironment(env)
    import org.apache.flink.streaming.api.scala._
 val dbData: DataSet[Row] =
      env.createInput(
        JDBCInputFormat.buildJDBCInputFormat()
          .setDrivername("org.postgresql.Driver")
          .setDBUrl("jdbc:postgresql://alcyona.gemelen.net:5432/app?user=app&password=app password")
          .setQuery("select * from sensor_data")
          .setRowTypeInfo(new RowTypeInfo(BasicTypeInfo.STRING_TYPE_INFO, BasicTypeInfo.STRING_TYPE_INFO, BasicTypeInfo.STRING_TYPE_INFO))
          .finish()
      )
    dbData.print()


    val csvTableSource = CsvTableSource
      .builder
      .path("/home/maria/Downloads/csv_sink.csv")
      .field("id", Types.STRING)
      .field("metrics", Types.DOUBLE)
      .field("input_time", Types.LONG)
      .field("ip", Types.STRING)
      .field("batchId", Types.LONG)
      .ignoreParseErrors
      .build

    tableEnv.registerTableSource("CsvTable", csvTableSource)

    val rawTable = tableEnv.scan("CsvTable")

    val result: Table = tableEnv.sqlQuery(
      "SELECT id, min(metrics), max(metrics), avg(metrics) FROM CsvTable group by id order by id")
    result.printSchema()

    val sink =
      new CsvTableSink(
        "/home/maria/Downloads/agg.csv",
        fieldDelim = ",",
        numFiles = 1,
        writeMode = WriteMode.OVERWRITE)
    result.writeToSink(sink)

    env.execute("Saving aggregation")

  }
}
