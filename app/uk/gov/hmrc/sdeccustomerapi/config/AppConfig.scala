/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.sdeccustomerapi.config

import play.api.Configuration

import java.net.URI
import javax.inject.{Inject, Singleton}

@Singleton
class AppConfig @Inject() (config: Configuration) {

  lazy val appName: String = config.get[String]("appName")

  private lazy val isLocal: Boolean = config.getOptional[Boolean]("govOne.useLocal").getOrElse(false)

  lazy val jwtConfig: GovOneJwtConfig = if isLocal then {
    GovOneJwtConfig(
      config.get[String]("local.issuer"),
      config.get[String]("local.audience"),
      URI.create(config.get[String]("local.jwksUrl")).toURL
    )
  } else {
    GovOneJwtConfig(
      config.get[String]("remote.issuer"),
      config.get[String]("remote.audience"),
      URI.create(config.get[String]("remote.jwksUrl")).toURL
    )
  }

}
