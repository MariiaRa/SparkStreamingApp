package com.ua

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.flink.api.common.typeinfo.{BasicTypeInfo, TypeInformation}
import org.apache.flink.api.java.io.jdbc.JDBCInputFormat
import org.apache.flink.api.java.typeutils.RowTypeInfo
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.table.api.scala.BatchTableEnvironment
import org.apache.flink.table.api.{TableEnvironment, Types}
import org.apache.flink.table.sinks.CsvTableSink
import org.apache.flink.table.sources.CsvTableSource
import org.apache.flink.types.Row

object Aggregation {

  val myConf: Config = ConfigFactory.load()
  val connection: String = myConf.getString("psql.connection")
  val host: String = myConf.getString("psql.host")
  val port: String = myConf.getString("psql.port")
  val db: String = myConf.getString("psql.database")
  val user: String = myConf.getString("psql.user")
  val password: String = myConf.getString("psql.password")
  val driver: String = myConf.getString("psql.driver")
  val url: String = connection + host + ":" + port + "/" + db + "?user=" + user + "&password=" + password

  def main(args: Array[String]): Unit = {

    val env = ExecutionEnvironment.getExecutionEnvironment
    val bTableEnv: BatchTableEnvironment = TableEnvironment.getTableEnvironment(env)
    import org.apache.flink.streaming.api.scala._

    val fieldNames2: Array[String] = Array("id_location", "location")
    val fieldNames1: Array[String] = Array("id_sensor", "type", "description")
    val fieldTypes: Array[TypeInformation[String]] = Array(BasicTypeInfo.STRING_TYPE_INFO, BasicTypeInfo.STRING_TYPE_INFO)

    val dbDataOne =
      env.createInput(
        JDBCInputFormat.buildJDBCInputFormat()
          .setDrivername(driver)
          .setDBUrl(url)
          .setQuery("select * from sensor_data")
          .setRowTypeInfo(new RowTypeInfo(
            Array[TypeInformation[_]](BasicTypeInfo.STRING_TYPE_INFO, BasicTypeInfo.STRING_TYPE_INFO, BasicTypeInfo.STRING_TYPE_INFO),
            fieldNames1))
          .finish()
      )

    val dbDataTwo =
      env.createInput(
        JDBCInputFormat.buildJDBCInputFormat()
          .setDrivername(driver)
          .setDBUrl(url)
          .setQuery("select * from sensor_location")
          .setRowTypeInfo(new RowTypeInfo(Array[TypeInformation[_]](BasicTypeInfo.STRING_TYPE_INFO, BasicTypeInfo.STRING_TYPE_INFO), fieldNames2))
          .finish()
      )
    bTableEnv.registerDataSet("sensor_data", dbDataOne)
    val data = bTableEnv.scan("sensor_data")
    bTableEnv.registerDataSet("sensor_location", dbDataTwo)
    val location = bTableEnv.scan("sensor_location")

    val csvtable: CsvTableSource = CsvTableSource.builder()
      .path("hdfs://alpha.gemelen.net:8020/apps/flink/raw_sink.csv")
      .fieldDelimiter(",")
      .lineDelimiter("\n")
      .field("id", Types.STRING)
      .field("metrics", Types.DOUBLE)
      .field("input_time", Types.STRING)
      .field("ip", Types.STRING)
      .field("time_bucket", Types.LONG)
      .build()
    bTableEnv.registerTableSource("csvTable", csvtable)
    val rawTable = bTableEnv.scan("csvTable")

    import org.apache.flink.table.api.scala._

    val select = rawTable.groupBy('id)
      .select('id, 'metrics.avg as 'avg, 'metrics.max as 'max, 'metrics.min as 'min, 'time_bucket.max as 'max_time, 'time_bucket.proctime)

    val max_time = rawTable.select('time_bucket.max as 'max_time_id)

    val result = select
      .join(data).where('id === 'id_sensor)
      .join(location).where('id === 'id_location)
      .join(max_time).where('max_time === 'max_time_id)
      .select('id, 'type, 'location, 'avg, 'max, 'min, 'max_time)
      .toDataSet[Row] // conversion to DataSet
      .print()

    val fields: Array[String] = Array("id", "avg", "max", "min", "time_bucket")
    val types: Array[TypeInformation[_]] = Array(Types.STRING, Types.DOUBLE, Types.DOUBLE, Types.DOUBLE, Types.LONG)
    val sink = new CsvTableSink("hdfs://alpha.gemelen.net:8020/apps/flink/sink_aggregation.csv", fieldDelim = ",")
    bTableEnv.registerTableSink("CsvSinkTable", fields, types, sink)
    select.insertInto("CsvSinkTable")

    env.execute("Aggregation")
  }
}
