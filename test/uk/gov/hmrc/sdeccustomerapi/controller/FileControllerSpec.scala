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

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.scalatest.matchers.must.Matchers.mustBe
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.AnyContentAsJson
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.sdeccustomerapi.controller.FileController

class FileControllerSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {
  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .build()

  given system: ActorSystem = ActorSystem("test-system")

  given Materializer = Materializer(system)

  private val controller = app.injector.instanceOf[FileController]

  val fakeBody: JsObject = Json.obj(
    "filename" -> "someFilename",
    "s3Bucket" -> "someS3Bucket",
    "fileKey"  -> "someFileKey",
    "fileEtag" -> "someFileEtag",
    "fileSize" -> 1,
    "fileType" -> "someFileType"
  )

  def fakeRequest(threadRef: String): FakeRequest[AnyContentAsJson] =
    FakeRequest("POST", s"/api/v1/thread/$threadRef/file ")
      .withJsonBody(fakeBody)

  "saveFile" should {
    "return the file metadata with the given threadRef when given valid body" in {
      val threadRef = "threadRef1"
      val request   = fakeRequest("threadRef1")

      val result = call(controller.saveFile(threadRef), request)

      status(result)      shouldBe Status.CREATED
      contentType(result) shouldBe Some("application/json")
      contentAsJson(result) mustBe Json.obj("threadReference" -> threadRef) ++ fakeBody
    }

    "return the 400 if given invalid body" in {
      val threadRef   = "threadRef1"
      val invalidBody = Json.obj(
        "filename1"   -> "someFilename",
        "s3Bucket123" -> "someS3Bucket"
      )
      val request = fakeRequest("threadRef1")
        .withJsonBody(invalidBody)

      val result = call(controller.saveFile(threadRef), request)

      status(result) shouldBe Status.BAD_REQUEST
    }
  }

}
