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

package uk.gov.hmrc.sdeccustomerapi.controller

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, getRequestedFor, putRequestedFor, urlEqualTo}
import com.nimbusds.jwt.JWTClaimsSet
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.Application
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.sdeccustomerapi.infrastructure.TokenValidator
import uk.gov.hmrc.sdeccustomerapi.model.SdecErrorResponse
import uk.gov.hmrc.sdeccustomerapi.testUtil.WireMockHelper

import java.util.Date
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class ThreadControllerItSpec extends AnyWordSpec with Matchers with WireMockHelper {

  private val jwtValidator = new TokenValidator {
    override def validate(token: String): Try[JWTClaimsSet] = {

      val claimsSet = new JWTClaimsSet.Builder()
        .subject("joe")
        .expirationTime(new Date(1300819380 * 1000L))
        .claim("http://example.com/is_root", true)
        .build()

      Success(claimsSet)
    }
  }

  lazy val app: Application =
    GuiceApplicationBuilder()
      .overrides(
        play.api.inject.bind[TokenValidator].to(jwtValidator)
      )
      .configure(
        "microservice.services.ipaas.port" -> server.port,
        "microservice.services.auth.port"  -> server.port,
        "govOne.jwt.issuer"                -> s"http://localhost:${server.port}/",
        "govOne.jwt.jwksUrl"               -> s"http://localhost:${server.port}/.well-known/jwks.json"
      )
      .build()

  "POST /thread-access" must {
    "return 200 when a thread access request is received" in {
      val request =
        FakeRequest("POST", "/sdec-customer-api/thread-access")
          .withJsonBody(Json.obj("threadReference" -> "KL897865432211"))

      val result: Future[Result] = route(app, request).get

      status(result) mustBe Status.OK
    }
  }

  "GET /sdec-customer-api/api/v1/thread/:threadReference" must {
    "return 200 with the thread details when IPaaS finds the thread" in {
      val threadReference = "THREAD12345678"
      val threadDetails   = Json.obj("threadReference" -> threadReference)

      // ipaas stub
      server.stubFor(
        WireMock
          .get(urlEqualTo(s"/sdec-ipaas-stub/api/v1/thread/$threadReference"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withHeader("Content-Type", "application/json")
              .withBody(threadDetails.toString)
          )
      )

      val request =
        FakeRequest("GET", s"/sdec-customer-api/api/v1/thread/$threadReference")
          .withHeaders("Authorization" -> "Bearer token")

      val result: Future[Result] = route(app, request).get

      status(result) mustBe Status.OK
      contentAsJson(result) mustBe threadDetails

      server.verify(
        getRequestedFor(urlEqualTo(s"/sdec-ipaas-stub/api/v1/thread/$threadReference"))
      )
    }

    "return 404 when IPaaS does not find the thread" in {
      val threadReference = "THREAD654321ZZ"

      server.stubFor(
        WireMock
          .get(urlEqualTo(s"/sdec-ipaas-stub/api/v1/thread/$threadReference"))
          .willReturn(aResponse().withStatus(NOT_FOUND))
      )

      val request =
        FakeRequest("GET", s"/sdec-customer-api/api/v1/thread/$threadReference")
          .withHeaders("Authorization" -> "Bearer token")

      val result: Future[Result] = route(app, request).get

      status(result) mustBe Status.NOT_FOUND
      contentAsJson(result) mustBe Json.toJson(SdecErrorResponse("Thread not found"))
    }

    "return 400 without calling IPaaS when the thread reference is badly formatted" in {
      val threadReference = "bad-ref"

      val request =
        FakeRequest("GET", s"/sdec-customer-api/api/v1/thread/$threadReference")
          .withHeaders("Authorization" -> "Bearer token")

      val result: Future[Result] = route(app, request).get

      status(result) mustBe Status.BAD_REQUEST
      contentAsJson(result) mustBe Json.toJson(SdecErrorResponse("Invalid thread reference"))

      server.verify(
        0,
        getRequestedFor(urlEqualTo(s"/sdec-ipaas-stub/api/v1/thread/$threadReference"))
      )
    }

    "return 500 when IPaaS fails" in {
      val threadReference = "ERROR000000500"

      server.stubFor(
        WireMock
          .get(urlEqualTo(s"/sdec-ipaas-stub/api/v1/thread/$threadReference"))
          .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR))
      )

      val request =
        FakeRequest("GET", s"/sdec-customer-api/api/v1/thread/$threadReference")
          .withHeaders("Authorization" -> "Bearer token")

      val result: Future[Result] = route(app, request).get

      status(result) mustBe Status.INTERNAL_SERVER_ERROR
    }

    "return 401 if no bearer token is sent" in {
      val request = FakeRequest("GET", "/sdec-customer-api/api/v1/thread/THREAD1000AA11")

      val result: Future[Result] = route(app, request).get

      status(result) mustBe Status.UNAUTHORIZED
      contentAsJson(result) mustBe Json.toJson(SdecErrorResponse("Bearer token not supplied"))
    }
  }

  "PUT /sdec-customer-api/api/v1/thread/:threadReference" must {
    "return 200 when a thread update request is received and processed successfully" in {

      val threadReference = "KL897865432211"
      val threadUpdate    = """{
                              |  "firstName": "Alice",
                              |  "surname": "Johnson",
                              |  "email": "alice.johnson@example.com",
                              |  "mobile": "+44 7700 900123",
                              |  "nino": "AB123456C",
                              |  "caseReference": "CASE-2025-0789",
                              |  "requestedItems": ["Item A", "Item B", "Item C"],
                              |  "message": "Please provide the requested documents.",
                              |  "requestedBy": {
                              |    "day": 15,
                              |    "month": 3,
                              |    "year": 2025
                              |  }
                              |}""".stripMargin

      // ipaas stub
      server.stubFor(
        WireMock
          .put(urlEqualTo(s"/sdec-ipaas-stub/api/v1/thread/$threadReference"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withHeader("Content-Type", "application/json")
          )
      )

      val request =
        FakeRequest("PUT", s"/sdec-customer-api/api/v1/thread/$threadReference")
          .withHeaders(
            "Content-Type"  -> "application/json",
            "Authorization" -> "Bearer token"
          )
          .withJsonBody(Json.parse(threadUpdate))

      val result: Future[Result] = route(app, request).get

      status(result) mustBe Status.OK

      server.verify(
        putRequestedFor(urlEqualTo(s"/sdec-ipaas-stub/api/v1/thread/$threadReference"))
      )

    }

    "return 400 when a thread update request is received with an invalid payload" in {
      val threadReference = "KL897865432211"
      val invalidPayload  = """{"superhero":"Captain America"}""".stripMargin

      val request =
        FakeRequest("PUT", s"/sdec-customer-api/api/v1/thread/$threadReference")
          .withHeaders(
            "Content-Type"  -> "application/json",
            "Authorization" -> "Bearer token"
          )
          .withJsonBody(Json.parse(invalidPayload))

      val result: Future[Result] = route(app, request).get

      status(result) mustBe Status.BAD_REQUEST
      contentAsJson(result) mustBe Json.toJson(SdecErrorResponse("Incompatible payload sent"))

    }
    "return 401 if no bearer token is sent" in {

      val threadReference = "KL897865432211"
      val threadUpdate    =
        """{
          |  "firstName": "Alice",
          |  "surname": "Johnson",
          |  "email": "alice.johnson@example.com",
          |  "mobile": "+44 7700 900123",
          |  "nino": "AB123456C",
          |  "caseReference": "CASE-2025-0789",
          |  "requestedItems": ["Item A", "Item B", "Item C"],
          |  "message": "Please provide the requested documents.",
          |  "requestedBy": {
          |    "day": 15,
          |    "month": 3,
          |    "year": 2025
          |  }
          |}""".stripMargin

      val request =
        FakeRequest("PUT", s"/sdec-customer-api/api/v1/thread/$threadReference")
          .withHeaders(
            "Content-Type" -> "application/json"
          )
          .withJsonBody(Json.parse(threadUpdate))

      val result: Future[Result] = route(app, request).get

      status(result) mustBe Status.UNAUTHORIZED
      contentAsJson(result) mustBe Json.toJson(SdecErrorResponse("Bearer token not supplied"))

    }
    "return 401 if bearer token validation fails" in {

      val jwtValidator = new TokenValidator {
        override def validate(token: String): Try[JWTClaimsSet] = Failure(new RuntimeException())
      }

      lazy val app: Application =
        GuiceApplicationBuilder()
          .overrides(
            play.api.inject.bind[TokenValidator].to(jwtValidator)
          )
          .configure(
            "microservice.services.ipaas.port" -> server.port,
            "microservice.services.auth.port"  -> server.port,
            "govOne.jwt.issuer"                -> s"http://localhost:${server.port}/",
            "govOne.jwt.jwksUrl"               -> s"http://localhost:${server.port}/.well-known/jwks.json"
          )
          .build()

      val threadReference = "KL897865432211"
      val threadUpdate    =
        """{
          |  "firstName": "Alice",
          |  "surname": "Johnson",
          |  "email": "alice.johnson@example.com",
          |  "mobile": "+44 7700 900123",
          |  "nino": "AB123456C",
          |  "caseReference": "CASE-2025-0789",
          |  "requestedItems": ["Item A", "Item B", "Item C"],
          |  "message": "Please provide the requested documents.",
          |  "requestedBy": {
          |    "day": 15,
          |    "month": 3,
          |    "year": 2025
          |  }
          |}""".stripMargin

      val request =
        FakeRequest("PUT", s"/sdec-customer-api/api/v1/thread/$threadReference")
          .withHeaders(
            "Content-Type"  -> "application/json",
            "Authorization" -> "Bearer token"
          )
          .withJsonBody(Json.parse(threadUpdate))

      val result: Future[Result] = route(app, request).get

      status(result) mustBe Status.UNAUTHORIZED
      contentAsJson(result) mustBe Json.toJson(SdecErrorResponse("Invalid bearer token"))
    }
  }

}
