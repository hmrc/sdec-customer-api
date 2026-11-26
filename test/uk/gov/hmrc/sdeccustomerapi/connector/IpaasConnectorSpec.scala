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

package uk.gov.hmrc.sdeccustomerapi.connector

import izumi.reflect.Tag
import org.scalatest.Inside.inside
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Writes
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, UpstreamErrorResponse}
import uk.gov.hmrc.sdeccustomerapi.config.{AppConfigAlgebra, GovOneJwtConfig}
import uk.gov.hmrc.sdeccustomerapi.model.{RequestedBy, ThreadReference, ThreadUpdate}

import java.net.URI
import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService, Future}

class IpaasConnectorSpec extends AnyWordSpec with Matchers {

  private val mockAppConfig = new AppConfigAlgebra {
    override def appName: String = "app-name"

    override def iPaasUpdateThreadDetailsBaseUrl: String = "http:localhost:3000"

    override def jwtConfig: GovOneJwtConfig =
      GovOneJwtConfig("abc", "def", URI.create("http:localhost:3000/jwks").toURL)
  }

  implicit val ec: ExecutionContextExecutorService =
    ExecutionContext.fromExecutorService(Executors.newSingleThreadExecutor())

  implicit val hc: HeaderCarrier = HeaderCarrier()

  private val succesResponse = HttpResponse(200)
  HttpResponse(500)

  private val ipaaSClientAlgebra = new IpaaSClientAlgebra {
    override def post[A: Tag](path: String, body: A)(implicit
      hc:     HeaderCarrier,
      writes: Writes[A]
    ): Future[Either[UpstreamErrorResponse, HttpResponse]] = fail("unexpected call to post method")

    override def put[A: Tag](path: String, body: A)(implicit
      hc:     HeaderCarrier,
      writes: Writes[A]
    ): Future[Either[UpstreamErrorResponse, HttpResponse]] = Future.successful(Right(succesResponse))

    override def get(path: String)(implicit hc: HeaderCarrier): Future[Either[UpstreamErrorResponse, HttpResponse]] =
      fail("unexpected call to get method")
  }

  val underTest = new IpaasConnector(ipaaSClientAlgebra, appConfig = mockAppConfig)

  "IpaasService" must {
    "facilitate a call to Ipaas to update thread details" in {

      val threadReference = ThreadReference("KL897865432211")
      val threadUpdate    = ThreadUpdate(
        firstName = Some("Luke"),
        surname = Some("Skywalker"),
        email = Some("luke@skywalker.com"),
        mobile = Some("1111111212121212"),
        nino = Some("ABCD1234"),
        caseReference = Some("CASE-REF-1"),
        requestedItems = List("P60"),
        message = "Luke I am your father!!",
        requestedBy = RequestedBy(25, 12, 2026)
      )

      underTest.updateThreadDetails(threadReference, threadUpdate).futureValue mustBe Right(succesResponse)
    }
    "return a 500 if a call to Ipaas to update thread details fails" in {

      val ipaaSClientAlgebra = new IpaaSClientAlgebra {
        override def post[A: Tag](path: String, body: A)(implicit
          hc:     HeaderCarrier,
          writes: Writes[A]
        ): Future[Either[UpstreamErrorResponse, HttpResponse]] = fail("unexpected call to post method")

        override def put[A: Tag](path: String, body: A)(implicit
          hc:     HeaderCarrier,
          writes: Writes[A]
        ): Future[Either[UpstreamErrorResponse, HttpResponse]] = Future.failed(new RuntimeException("bang bang!!"))

        override def get(path: String)(implicit
          hc: HeaderCarrier
        ): Future[Either[UpstreamErrorResponse, HttpResponse]] = fail("unexpected call to get method")
      }

      val underTest = new IpaasConnector(ipaaSClientAlgebra, appConfig = mockAppConfig)

      val threadReference = ThreadReference("KL897865432211")
      val threadUpdate    = ThreadUpdate(
        firstName = Some("Luke"),
        surname = Some("Skywalker"),
        email = Some("luke@skywalker.com"),
        mobile = Some("1111111212121212"),
        nino = Some("ABCD1234"),
        caseReference = Some("CASE-REF-1"),
        requestedItems = List("P60"),
        message = "Luke I am your father!!",
        requestedBy = RequestedBy(25, 12, 2026)
      )

      inside(underTest.updateThreadDetails(threadReference, threadUpdate).futureValue) {
        case Left(upstreamErrorResponse) => fail(s"expected Right but got Left($upstreamErrorResponse)")
        case Right(httpResponse)         => httpResponse.status mustBe 500
      }
    }
  }
}
