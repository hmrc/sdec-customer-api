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

package uk.gov.hmrc.sdeccustomerapi.fixtures

import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.{HttpResponse, UpstreamErrorResponse}
import uk.gov.hmrc.sdeccustomerapi.model.{RequestedBy, ThreadReference, ThreadUpdate}

object ThreadFixtures {

  val validReference:   String          = "THREAD1000AA11"
  val invalidReference: String          = "bad-ref"
  val threadReference:  ThreadReference = ThreadReference(validReference)

  val threadDetails: JsObject = Json.obj("threadReference" -> validReference)

  val ipaasFound:       Either[UpstreamErrorResponse, HttpResponse] = Right(HttpResponse(200, threadDetails.toString))
  val ipaasNotFound:    Either[UpstreamErrorResponse, HttpResponse] = Left(UpstreamErrorResponse("not found", 404))
  val ipaasServerError: Either[UpstreamErrorResponse, HttpResponse] = Left(UpstreamErrorResponse("boom", 500))

  val threadUpdate: ThreadUpdate = ThreadUpdate(
    firstName = Some("Jane"),
    surname = Some("Smith"),
    email = Some("jane.smith@example.com"),
    mobile = Some("1111111212121212"),
    nino = Some("ABCD1234"),
    caseReference = Some("CASE-REF-1"),
    requestedItems = List("P60"),
    message = "Hello world!",
    requestedBy = RequestedBy(25, 12, 2026)
  )

}
