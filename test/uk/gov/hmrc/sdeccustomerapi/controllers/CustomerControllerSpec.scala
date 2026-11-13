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

import com.nimbusds.jwt.JWTClaimsSet
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.*
import play.api.test.{FakeRequest, Helpers}
import play.api.{Application, inject}
import uk.gov.hmrc.sdeccustomerapi.infrastructure.TokenValidator

import scala.util.{Failure, Success, Try}

class CustomerControllerSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with BeforeAndAfterAll {

  private val testClaims =
    new JWTClaimsSet.Builder()
      .subject("test-user")
      .build()

  private val stubTokenValidator = new TokenValidator {
    override def validate(token: String): Try[JWTClaimsSet] =
      if token == "valid-token" then Success(testClaims)
      else Failure(new RuntimeException("Invalid token"))
  }

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .overrides(
        inject.bind[TokenValidator].toInstance(stubTokenValidator)
      )
      .build()

  private val controller = app.injector.instanceOf[CustomerController]

  private def requestWithToken(token: String) =
    FakeRequest("GET", "/customer")
      .withHeaders("Authorization" -> s"Bearer $token")

  private def requestWithoutToken =
    FakeRequest("GET", "/customer")

  "GET /customer" should {
    "return 200 when a valid JWT token is supplied" in {
      val result = controller.getCustomer(requestWithToken("valid-token"))

      status(result)          shouldBe Status.OK
      contentAsString(result) shouldBe "Hello customer"
    }

    "return 401 when the Authorization header is missing" in {
      val result = controller.getCustomer(requestWithoutToken)

      status(result) shouldBe Status.UNAUTHORIZED
    }

    "return 401 when the token is invalid" in {
      val result = controller.getCustomer(requestWithToken("invalid-token"))

      status(result) shouldBe Status.UNAUTHORIZED
    }
  }

}
