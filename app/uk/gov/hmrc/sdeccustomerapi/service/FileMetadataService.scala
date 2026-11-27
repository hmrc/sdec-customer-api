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

import play.api.Logging
import play.api.libs.json.Json
import uk.gov.hmrc.sdeccustomerapi.model.FileMetadata

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait FileMetadataService {
  def saveFile(fileMetadata: FileMetadata)(using ExecutionContext): Future[FileMetadata]
}

@Singleton()
class DefaultFileMetadataService @Inject() extends FileMetadataService with Logging {

  override def saveFile(fileMetadata: FileMetadata)(using ExecutionContext): Future[FileMetadata] =
    Future
      .successful(logger.info(s"Writing File Metadata for uploaded file: ${Json.toJson(fileMetadata)}"))
      .map(_ => fileMetadata)
}
