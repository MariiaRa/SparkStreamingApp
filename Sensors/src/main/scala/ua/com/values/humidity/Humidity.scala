package ua.com.values.humidity

import ua.com.values.{RoomValues, Seasons}

import scala.util.Random

object Humidity {
  def getHumidity(month: Int): Int = {

    val season = Seasons.getSeason(month)

    val (min, max) = RoomValues.avgRoomHumidity(season)
    val randNum = Random.nextInt(max - min)
    min + randNum
  }
}
