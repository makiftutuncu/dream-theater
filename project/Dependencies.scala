import sbt._

object Dependencies {
  val anorm         = "org.playframework.anorm" %% "anorm"      % "2.6.4"
  val postgresql    = "org.postgresql"           % "postgresql" % "42.2.8"
  val zio           = "dev.zio"                 %% "zio"        % "1.0.0-RC14"

  val scalaTestPlay = "org.scalatestplus.play"  %% "scalatestplus-play" % "4.0.3"  % Test
}
