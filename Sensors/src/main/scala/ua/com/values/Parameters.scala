package ua.com.values

import org.joda.time.DateTime

import scala.math.BigDecimal.RoundingMode
import scala.util.Random

sealed trait Parameters {
  def getValue: BigDecimal
}

object Temperature extends Parameters {
  override def getValue: BigDecimal = {
    val season = Seasons.getSeason(DateTime.now().getMonthOfYear)
    val (min, max) = RoomValues.avgRoomTemps(season)
    val randNum = Random.nextDouble * (max - min)
    BigDecimal.valueOf(min + randNum).setScale(2, RoundingMode.HALF_EVEN)
  }
}

object Emission extends Parameters {
  def getValue: BigDecimal = {
    val randNum = Random.nextInt(9) * 0.001
    BigDecimal.valueOf(randNum).setScale(3)
  }
}

object Humidity extends Parameters {
  def getValue: BigDecimal = {
    val season = Seasons.getSeason(DateTime.now().getMonthOfYear)
    val (min, max) = RoomValues.avgRoomHumidity(season)
    val randNum = Random.nextInt(max - min)
    BigDecimal.valueOf(min + randNum)
  }
}