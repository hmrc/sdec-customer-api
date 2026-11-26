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

import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.net.URI
import javax.inject.{Inject, Singleton}

trait AppConfigAlgebra {
  def appName:                         String
  def iPaasUpdateThreadDetailsBaseUrl: String
  def jwtConfig:                       GovOneJwtConfig
}

@Singleton
class AppConfig @Inject() (config: ServicesConfig) extends AppConfigAlgebra {

  lazy val appName: String = config.getString("appName")

  lazy val iPaasUpdateThreadDetailsBaseUrl: String = config.baseUrl("ipaas.updateThreadDetails")

  lazy val jwtConfig: GovOneJwtConfig =
    GovOneJwtConfig(
      config.getString("govOne.jwt.issuer"),
      config.getString("govOne.jwt.audience"),
      URI.create(config.getString("govOne.jwt.jwksUrl")).toURL
    )

}
