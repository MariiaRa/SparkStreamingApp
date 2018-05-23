import sbt.Keys._
import sbt._

object DefaultResolversPlugin extends AutoPlugin {

  val apache = "apache-snapshots" at "http://repository.apache.org/snapshots/"

  val typeSafeResolver = "Typesafe repository releases" at "http://repo.typesafe.com/typesafe/releases/"

  override lazy val projectSettings = Seq(
    resolvers ++= Seq(
      apache)
  )
}
