import sbt.Keys.libraryDependencies
import sbt.*

object AppDependencies {

  private val bootstrapVersion    = "10.8.0"
  private val frontendPlayVersion = "13.16.0"
  private val playTestVersion     = "7.0.2"
  private val scalaTestVersion    = "3.2.20"
  private val scalaMockVersion    = "7.5.5"
  private val logbackVersion      = "1.4.11"
  private val jwtVersion          = "12.0.0"
  private val nimbusVersion       = "10.10"
  private val hmrcMongoVersion    = "2.14.0"
  private val catsVersion         = "2.13.0"

  val compile = Seq(
    "uk.gov.hmrc"          %% "bootstrap-backend-play-30"  % bootstrapVersion,
    "uk.gov.hmrc"          %% "play-frontend-hmrc-play-30" % frontendPlayVersion,
    "ch.qos.logback"        % "logback-classic"            % logbackVersion,
    "com.github.jwt-scala" %% "jwt-play-json"              % jwtVersion,
    "uk.gov.hmrc.mongo"    %% "hmrc-mongo-play-30"         % hmrcMongoVersion,
    "com.nimbusds"          % "nimbus-jose-jwt"            % nimbusVersion,
    "org.typelevel"        %% "cats-core"                  % catsVersion
  )

  val test = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-30"  % bootstrapVersion % Test,
    "org.scalatestplus.play" %% "scalatestplus-play"      % playTestVersion  % Test,
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-30" % hmrcMongoVersion % Test,
    "org.scalatest"          %% "scalatest"               % scalaTestVersion % Test,
    "org.scalamock"          %% "scalamock"               % scalaMockVersion % Test
  )

  val it = Seq.empty
}
