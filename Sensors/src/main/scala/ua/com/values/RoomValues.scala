package ua.com.values

object RoomValues {

  val avgRoomTemps = Map(
    Seasons.Winter -> (17, 24),
    Seasons.Spring -> (20, 25),
    Seasons.Summer -> (22, 27),
    Seasons.Autumn -> (18, 25)
  )

  val avgRoomHumidity = Map(
    Seasons.Winter -> (30, 45),
    Seasons.Spring -> (50, 65),
    Seasons.Summer -> (55, 70),
    Seasons.Autumn -> (45, 60)
  )
}
