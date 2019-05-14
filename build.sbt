name         := "dream-theater"
organization := "com.github.makiftutuncu"
version      := "0.1"
scalaVersion := "2.12.8"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

val anorm2         = "com.typesafe.play"      %% "anorm"              % "2.5.3"
val postgresql42   = "org.postgresql"          % "postgresql"         % "42.2.5"
val scalaTestPlay4 = "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.2"  % Test

libraryDependencies ++= Seq(
  anorm2,
  evolutions,
  guice,
  jdbc,
  postgresql42,
  scalaTestPlay4
)
