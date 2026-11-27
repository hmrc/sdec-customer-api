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

package uk.gov.hmrc.sdeccustomerapi.service

import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, UpstreamErrorResponse}
import uk.gov.hmrc.sdeccustomerapi.connector.IpaasConnectorAlgebra
import uk.gov.hmrc.sdeccustomerapi.model.{RequestedBy, ThreadReference, ThreadUpdate}

import scala.concurrent.Future

class IpaasServiceSpec extends AnyWordSpec with Matchers {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  private val ipaasResponse = HttpResponse(200)

  private val ipaasConnector = new IpaasConnectorAlgebra {
    override def getThreadDetails(threadReference: ThreadReference)(using
      hc: HeaderCarrier
    ): Future[Either[UpstreamErrorResponse, HttpResponse]] = Future.successful(Right(ipaasResponse))

    override def updateThreadDetails(threadReference: ThreadReference, threadUpdate: ThreadUpdate)(implicit
      hc: HeaderCarrier
    ): Future[Either[UpstreamErrorResponse, HttpResponse]] = Future.successful(Right(ipaasResponse))
  }

  val underTest = new IpaasService(ipaasConnector)

  ".getThreadDetails" must {
    "facilitate a call to Ipaas to get thread details" in {

      val threadReference = ThreadReference("THREAD123456AA")

      underTest.getThreadDetails(threadReference).futureValue mustBe Right(ipaasResponse)
    }
  }

  ".updateThreadDetails" must {
    "facilitate a call to Ipaas to update thread details" in {

      val threadReference = ThreadReference("KL897865432211")
      val threadUpdate    = ThreadUpdate(
        firstName = Some("Maria"),
        surname = Some("Smith"),
        email = Some("maria.smith@example.com"),
        mobile = Some("1111111212121212"),
        nino = Some("ABCD1234"),
        caseReference = Some("CASE-REF-1"),
        requestedItems = List("P60"),
        message = "Hello world!",
        requestedBy = RequestedBy(25, 12, 2026)
      )

      underTest.updateThreadDetails(threadReference, threadUpdate).futureValue mustBe Right(ipaasResponse)
    }
  }

}
