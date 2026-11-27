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

package uk.gov.hmrc.sdeccustomerapi.model

import play.api.libs.functional.syntax.*
import play.api.libs.json.*
import play.api.libs.json.{Writes, __}

final case class FileMetadata(
  threadReference: String,
  filename:        String,
  s3Bucket:        String,
  fileKey:         String,
  fileEtag:        String,
  fileSize:        Long,
  fileType:        String
)

object FileMetadata {
  implicit val writes: OWrites[FileMetadata] = (
    (__ \ "threadReference").write[String] and
      (__ \ "filename").write[String] and
      (__ \ "s3Bucket").write[String] and
      (__ \ "fileKey").write[String] and
      (__ \ "fileEtag").write[String] and
      (__ \ "fileSize").write[Long] and
      (__ \ "fileType").write[String]
  )(fileMetadata =>
    (
      fileMetadata.threadReference,
      fileMetadata.filename,
      fileMetadata.s3Bucket,
      fileMetadata.fileKey,
      fileMetadata.fileEtag,
      fileMetadata.fileSize,
      fileMetadata.fileType
    )
  )
}
