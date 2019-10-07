name         := "dream-theater"
organization := "com.github.makiftutuncu"
version      := "0.2"
scalaVersion := "2.13.1"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

libraryDependencies ++= Seq(
  evolutions,
  guice,
  jdbc,
  Dependencies.anorm,
  Dependencies.postgresql,
  Dependencies.zio,
  Dependencies.scalaTestPlay
)
