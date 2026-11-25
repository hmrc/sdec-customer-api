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

import com.nimbusds.jwt.JWTClaimsSet
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, UpstreamErrorResponse}
import uk.gov.hmrc.sdeccustomerapi.infrastructure.{AuthenticatedActionBuilder, TokenValidator}
import uk.gov.hmrc.sdeccustomerapi.model.{ThreadReference, ThreadUpdate}
import uk.gov.hmrc.sdeccustomerapi.service.IpaasServiceAlgebra

import java.util.Date
import scala.concurrent.Future
import scala.util.{Success, Try}

class ThreadControllerSpec extends AnyWordSpec with Matchers {

  private lazy val cc = stubControllerComponents()

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

  private val ipaasService = new IpaasServiceAlgebra {
    override def updateThreadDetails(threadReference: ThreadReference, threadUpdate: ThreadUpdate)(implicit
      hc: HeaderCarrier
    ): Future[Either[UpstreamErrorResponse, HttpResponse]] = Future.successful(Right(HttpResponse(200)))
  }

  private lazy val authAction =
    new AuthenticatedActionBuilder(
      cc,
      jwtValidator
    )
  private val controller = new ThreadController(stubControllerComponents(), authAction, ipaasService)

  "POST /thread-access" must {
    "return 200 when a thread access request is received" in {
      val request =
        FakeRequest("POST", "/thread-access")
          .withJsonBody(Json.obj("threadReference" -> "KL897865432211"))

      val result = controller.requestAccess(request)

      status(result) mustBe Status.OK
    }
  }

}
