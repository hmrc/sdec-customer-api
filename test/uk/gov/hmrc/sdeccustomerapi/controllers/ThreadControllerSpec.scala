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

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*

class ThreadControllerSpec extends AnyWordSpec with Matchers {

  private val controller = new ThreadController(stubControllerComponents())

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
