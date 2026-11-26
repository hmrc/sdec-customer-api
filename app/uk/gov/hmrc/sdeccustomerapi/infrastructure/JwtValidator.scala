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

package uk.gov.hmrc.sdeccustomerapi.infrastructure

import com.nimbusds.jose.proc.*
import com.nimbusds.jwt.*
import com.nimbusds.jwt.proc.*
import uk.gov.hmrc.sdeccustomerapi.config.AppConfigAlgebra

import java.util.Set
import javax.inject.Inject
import scala.util.Try

final class JwtValidator @Inject() (
  appConfig: AppConfigAlgebra
) extends TokenValidator {

  private lazy val keySelector =
    JWSAlgorithmFamilyJWSKeySelector
      .fromJWKSetURL[SecurityContext](appConfig.jwtConfig.jwksUrl)

  private lazy val processor =
    new DefaultJWTProcessor[SecurityContext]()

  private lazy val claimsVerifier =
    new DefaultJWTClaimsVerifier[SecurityContext](
      appConfig.jwtConfig.audience,
      new JWTClaimsSet.Builder()
        .issuer(appConfig.jwtConfig.issuer)
        .build(),
      Set.of("exp")
    )

  def validate(token: String): Try[JWTClaimsSet] = {
    processor.setJWSKeySelector(keySelector)
    processor.setJWTClaimsSetVerifier(claimsVerifier)
    Try(processor.process(token, new SecurityContext {}))
  }
}
