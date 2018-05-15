package ua.com.values.temperature

import org.joda.time.DateTime
import ua.com.values.{RoomValues, Seasons}

import scala.math.BigDecimal.RoundingMode
import scala.util.Random
object Temperature {

  def getValue: BigDecimal = {

    val season = Seasons.getSeason(DateTime.now().getMonthOfYear)
    val (min, max) = RoomValues.avgRoomTemps(season)
    val randNum = Random.nextDouble * (max - min)
    BigDecimal.valueOf(min + randNum).setScale(2, RoundingMode.HALF_EVEN)
  }
}


