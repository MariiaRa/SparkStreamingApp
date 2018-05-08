package ua.com.values.temperature

import ua.com.values.{RoomValues, Seasons}

import scala.math.BigDecimal.RoundingMode
import scala.util.Random

object Temperature {

  def getTemperature(month: Int): BigDecimal = {

    val season = Seasons.getSeason(month)

    val (min, max) = RoomValues.avgRoomTemps(season)
    val randNum = Random.nextDouble * (max - min)
    BigDecimal.valueOf(min + randNum).setScale(2, RoundingMode.HALF_EVEN)
  }

}