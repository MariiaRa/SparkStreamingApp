package com

case class SensorDate(id: String, value: Double, time: String, ip: String )

class Parser {
  def parse(message:String): SensorDate =
  {
    var columns = message.split(",")

    val id = columns(0).split("=")(1)
    val date = columns(2).split("=")(1).replace(".", "")
    val value = columns(1).split("=")(1).toDouble
    val ip = columns(3).split("=")(1)
    SensorDate(id, value, date, ip)
  }
}
