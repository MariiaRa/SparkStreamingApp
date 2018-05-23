package com.DB

import com.Main._
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.spark.sql.{DataFrame, SparkSession}

class PostgreSQL {

  val myConf: Config = ConfigFactory.load()
  val connection: String = myConf.getString("psql.connection")
  val host: String = myConf.getString("psql.host")
  val port: String = myConf.getString("psql.port")
  val db: String = myConf.getString("psql.database")
  val user: String = myConf.getString("psql.user")
  val password: String = myConf.getString("psql.password")

  val url = connection + host + ":" + port + "/" + db

  val sparkSession = SparkSession.builder
    .appName("SparkAppStreaming")
    .getOrCreate()

  def getSensorData(sensorID: String): DataFrame = {

    val sensorDataDF = sparkSession.read.
      format("jdbc")
      .option("url", url)
      .option("dbtable", "sensor_data")
      .option("user", user)
      .option("password", password)
      .option("driver", "org.postgresql.Driver")
      .load()

    val sensorLocationDF = sparkSession.read.
      format("jdbc")
      .option("url", url)
      .option("dbtable", "sensor_location")
      .option("user", user)
      .option("password", password)
      .option("driver", "org.postgresql.Driver")
      .load()

    //join Two DataFrames without a duplicated Column
    val jounedTable: DataFrame = sensorDataDF.join(sensorLocationDF, "id").orderBy("id")
    jounedTable.show()

    // Register the DataFrame as a SQL temporary view
    jounedTable.createOrReplaceTempView("sensors")

    //sparkSession.sql("SELECT sensors.id, sensors.type, sensors.location FROM sensors WHERE sensors.id = \"" + sensorId + "\"").show()

    val selectedSensor: DataFrame = sparkSession.sql("SELECT sensors.id, sensors.type, sensors.location FROM sensors WHERE sensors.id = \"" + sensorId + "\"")
    selectedSensor

  }
}
