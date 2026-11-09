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

package uk.gov.hmrc.sdeccustomerapi.controllers

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import pdi.jwt.*
import play.api.Application
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers.*
import play.api.test.{FakeRequest, Helpers}

import java.time.Instant

class CustomerControllerSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite:

  private val secret = "secretKey"

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .build()

  private val controller = app.injector.instanceOf[CustomerController]

  private val payload = Json.obj(
    "internalId"      -> "12345",
    "firstName"       -> "John",
    "lastName"        -> "Smith",
    "email"           -> "john.smith@example.com",
    "phoneNumber"     -> None,
    "nino"            -> "WM111111D",
    "confidenceLevel" -> 250
  )

  private def createToken(
    expiry:        Long = Instant.now().plusSeconds(3600).getEpochSecond,
    signingSecret: String = secret
  ): String =
    val claim = JwtClaim(
      payload.toString,
      expiration = Some(expiry)
    )

    Jwt.encode(
      claim,
      signingSecret,
      JwtAlgorithm.HS256
    )

  private def requestWithToken(token: String) =
    FakeRequest("GET", "/customer")
      .withHeaders("Authorization" -> s"Bearer $token")

  private def requestWithoutToken =
    FakeRequest("GET", "/customer")

  "GET /customer" should:
    "return 200 when a valid JWT is supplied" in:
      val token  = createToken()
      val result = controller.getCustomer(requestWithToken(token))

      status(result)          shouldBe Status.OK
      contentAsString(result) shouldBe "Hello John"

    "return 401 when the Authorization header is missing" in:
      val result = controller.getCustomer(requestWithoutToken)

      status(result) shouldBe Status.UNAUTHORIZED

    "return 401 when the JWT has expired" in:
      val expiredToken = createToken(
        expiry = Instant.now().minusSeconds(3600).getEpochSecond
      )

      val result = controller.getCustomer(requestWithToken(expiredToken))

      status(result) shouldBe Status.UNAUTHORIZED

    "return 401 when the JWT signature cannot be verified" in:
      val token = createToken(signingSecret = "wrongSecret")

      val result = controller.getCustomer(requestWithToken(token))

      status(result) shouldBe Status.UNAUTHORIZED
