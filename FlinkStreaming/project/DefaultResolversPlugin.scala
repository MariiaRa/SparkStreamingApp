import sbt.Keys._
import sbt._

object DefaultResolversPlugin extends AutoPlugin {
  val apache = "Apache Development Snapshot Repository" at "https://repository.apache.org/content/repositories/snapshots/"
  val apacheSnapshots = "apache-snapshots" at "http://repository.apache.org/snapshots/"
  val apacheRepository = "Apache Repository" at "https://repository.apache.org/content/repositories/releases/"
  val typeSafeResolver = "Typesafe repository releases" at "http://repo.typesafe.com/typesafe/releases/"
  val confluent = "Confluent" at "http://packages.confluent.io/maven"
  val sonatype = "Sonatype" at "https://oss.sonatype.org/content/repositories/releases/"
  override lazy val projectSettings = Seq(
    resolvers ++= Seq(
      apache,
      apacheSnapshots,
      apacheRepository,
      typeSafeResolver,
      sonatype)
  )
}