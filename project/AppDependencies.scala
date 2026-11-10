import sbt.Keys.libraryDependencies
import sbt.*

object AppDependencies {

  private val bootstrapVersion    = "10.8.0"
  private val frontendPlayVersion = "13.16.0"
  private val playTestVersion     = "7.0.2"
  private val scalaTestVersion    = "3.2.20"
  private val logbackVersion      = "1.4.11"
  private val h2Version           = "2.5.250"
  private val doobieVersion       = "1.0.0-RC10"
  private val flywayVersion       = "10.17.0"
  private val jwtVersion          = "12.0.0"

  val compile = Seq(
    "uk.gov.hmrc"          %% "bootstrap-backend-play-30"  % bootstrapVersion,
    "uk.gov.hmrc"          %% "play-frontend-hmrc-play-30" % frontendPlayVersion,
    "ch.qos.logback"        % "logback-classic"            % logbackVersion,
    "org.flywaydb"          % "flyway-core"                % flywayVersion,
    "com.github.jwt-scala" %% "jwt-play-json"              % jwtVersion
  )

  val test = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-30" % bootstrapVersion % Test,
    "org.scalatestplus.play" %% "scalatestplus-play"     % playTestVersion  % Test,
    "org.scalatest"          %% "scalatest"              % scalaTestVersion % Test
  )

  val it = Seq.empty
}
