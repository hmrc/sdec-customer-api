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

import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.{JWKSet, KeyUse, RSAKey}
import com.nimbusds.jose.{JOSEObjectType, JWSAlgorithm, JWSHeader}
import com.nimbusds.jwt.{JWTClaimsSet, SignedJWT}
import com.sun.net.httpserver.HttpServer
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.*
import play.api.test.{FakeRequest, Helpers}

import java.net.InetSocketAddress
import java.security.interfaces.{RSAPrivateKey, RSAPublicKey}
import java.security.{KeyPair, KeyPairGenerator}
import java.time.Instant
import java.util.Date

class CustomerControllerSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with BeforeAndAfterAll:

  private val testIssuer   = "https://test-issuer.example.com"
  private val testAudience = "test-client"

  private val keyPair: KeyPair =
    val generator = KeyPairGenerator.getInstance("RSA")
    generator.initialize(2048)
    generator.generateKeyPair()

  private val publicJwk =
    new RSAKey.Builder(
      keyPair.getPublic.asInstanceOf[RSAPublicKey]
    )
      .keyUse(KeyUse.SIGNATURE)
      .algorithm(JWSAlgorithm.RS256)
      .keyID("test-key")
      .build()

  private val jwksServer: HttpServer =
    val server =
      HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0)

    server.createContext(
      "/.well-known/jwks.json",
      exchange =>
        val response =
          new JWKSet(publicJwk).toString.getBytes("UTF-8")

        exchange.getResponseHeaders
          .set("Content-Type", "application/json")

        exchange.sendResponseHeaders(200, response.length)

        val output = exchange.getResponseBody
        output.write(response)
        output.close()
    )

    server.start()
    server

  private val testJwksUrl = s"http://127.0.0.1:${jwksServer.getAddress.getPort}/.well-known/jwks.json"

  private def createToken(
    expiry:     Instant = Instant.now().plusSeconds(3600),
    signingKey: RSAPrivateKey = keyPair.getPrivate.asInstanceOf[RSAPrivateKey]
  ): String =
    val claims =
      new JWTClaimsSet.Builder()
        .issuer(testIssuer)
        .audience(testAudience)
        .subject("test-user")
        .expirationTime(Date.from(expiry))
        .build()

    val header =
      new JWSHeader.Builder(JWSAlgorithm.RS256)
        .keyID("test-key")
        .`type`(JOSEObjectType.JWT)
        .build()

    val jwt = new SignedJWT(header, claims)

    jwt.sign(new RSASSASigner(signingKey))

    jwt.serialize()

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure(
        "govOne.issuer"   -> testIssuer,
        "govOne.audience" -> testAudience,
        "govOne.jwksUrl"  -> testJwksUrl
      )
      .build()

  private val controller = app.injector.instanceOf[CustomerController]

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
      contentAsString(result) shouldBe "Hello customer"

    "return 401 when the Authorization header is missing" in:
      val result = controller.getCustomer(requestWithoutToken)

      status(result) shouldBe Status.UNAUTHORIZED

    "return 401 when the JWT has expired" in:
      val expiredToken = createToken(
        expiry = Instant.now().minusSeconds(3600)
      )

      val result = controller.getCustomer(requestWithToken(expiredToken))

      status(result) shouldBe Status.UNAUTHORIZED

    "return 401 when the JWT signature cannot be verified" in:
      val wrongKeyPair = KeyPairGenerator.getInstance("RSA")
      wrongKeyPair.initialize(2048)

      val wrongPrivateKey =
        wrongKeyPair
          .generateKeyPair()
          .getPrivate
          .asInstanceOf[RSAPrivateKey]

      val token = createToken(signingKey = wrongPrivateKey)

      val result = controller.getCustomer(requestWithToken(token))

      status(result) shouldBe Status.UNAUTHORIZED

  override def afterAll(): Unit =
    try
      jwksServer.stop(0)
    finally
      super.afterAll()
