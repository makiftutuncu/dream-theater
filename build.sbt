name         := "dream-theater"
organization := "dev.akif"
version      := "0.2"
scalaVersion := "2.13.1"

lazy val e             = "dev.akif"                %% "e-play-json"        % "0.2.3"
lazy val playAnorm     = "org.playframework.anorm" %% "anorm"              % "2.6.4"
lazy val postgresql    = "org.postgresql"           % "postgresql"         % "42.2.8"
lazy val zio           = "dev.zio"                 %% "zio"                % "1.0.0-RC17"
lazy val scalaTestPlay = "org.scalatestplus.play"  %% "scalatestplus-play" % "4.0.3"      % Test

libraryDependencies ++= Seq(
  evolutions,
  guice,
  jdbc,
  e,
  playAnorm,
  postgresql,
  zio,
  scalaTestPlay
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)
