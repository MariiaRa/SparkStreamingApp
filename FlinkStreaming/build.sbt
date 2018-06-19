import Dependencies._
import sbt.Keys.libraryDependencies

lazy val root = (project in file(".")).
  enablePlugins(RootProjectPlugin).
  settings(
    assemblyMergeStrategy in assembly := {
      case PathList("META-INF", xs@_*) => MergeStrategy.discard
      case x => MergeStrategy.first
    }
  ).settings(
  libraryDependencies ++= appDependencies
)