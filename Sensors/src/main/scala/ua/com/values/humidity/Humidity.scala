package ua.com.values.humidity

import org.joda.time.DateTime
import ua.com.values.{RoomValues, Seasons}

import scala.util.Random

object Humidity{
  def getValue: Int = {

    val season = Seasons.getSeason(DateTime.now().getMonthOfYear)
    val (min, max) = RoomValues.avgRoomHumidity(season)
    val randNum = Random.nextInt(max - min)
    min + randNum
  }
}
