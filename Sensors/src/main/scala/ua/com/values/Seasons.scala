package ua.com.values

object Seasons extends Enumeration {
  val Spring, Summer, Autumn, Winter = Value

  def getSeason(month: Int): Seasons.Value = month match {
    case x if x >= 3 && x <= 5  => Spring
    case x if x >= 6 && x <= 8  => Summer
    case x if x >= 9 && x <= 11  => Autumn
    case _ => Winter
  }
}