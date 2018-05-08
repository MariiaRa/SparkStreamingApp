package ua.com.values.emission

import scala.util.Random

object Emission {
  def getEmission: BigDecimal = {
    val randNum = Random.nextInt(9) * 0.001
    BigDecimal.valueOf(randNum).setScale(3)
  }
}
