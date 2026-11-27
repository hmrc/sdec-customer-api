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

import play.api.libs.json.*
import play.api.mvc.*
import uk.gov.hmrc.sdeccustomerapi.implicits.RequestSyntax.*
import uk.gov.hmrc.sdeccustomerapi.model.FileMetadata
import uk.gov.hmrc.sdeccustomerapi.model.api.FileMetadataRequest
import uk.gov.hmrc.sdeccustomerapi.service.FileMetadataService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class FileController @Inject (
  cc:                  ControllerComponents,
  fileMetadataService: FileMetadataService
) extends AbstractController(cc) {

  def saveFile(threadRef: String): Action[JsValue] =
    Action.async(cc.parsers.json) { request =>
      request.bodyAs[FileMetadataRequest] match {
        case Right(fileMetadataRequest) =>
          fileMetadataService
            .saveFile(fileMetadataRequest.toFileMetadata(threadRef))
            .map(fm => Created(Json.toJson(fm)))
        case Left(errorMessage) =>
          Future.successful(BadRequest(s"Malformed file metadata body: $errorMessage"))
      }
    }

}
