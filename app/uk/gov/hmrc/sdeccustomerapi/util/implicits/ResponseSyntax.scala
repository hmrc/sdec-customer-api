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

package uk.gov.hmrc.sdeccustomerapi.util.implicits

import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.mvc.Results.{InternalServerError, Ok}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, UpstreamErrorResponse}
import uk.gov.hmrc.sdeccustomerapi.model.SdecErrorResponse
import uk.gov.hmrc.sdeccustomerapi.util.RequestAwareLogger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ResponseSyntax {
  implicit class ResponseOps(f: Future[Either[UpstreamErrorResponse, HttpResponse]]) {

    private val logger = new RequestAwareLogger(this.getClass)
    def withResponseHandling()(implicit headerCarrier: HeaderCarrier): Future[Result] =
      f.map {
        case Left(upstreamErrorResponse: UpstreamErrorResponse) =>
          InternalServerError(
            Json.toJson(SdecErrorResponse(upstreamErrorResponse.message, Some(upstreamErrorResponse.statusCode)))
          )
        case Right(httpResponse: HttpResponse) =>
          httpResponse.status match {
            case OK =>
              if httpResponse.body.isEmpty then Ok
              else Ok(httpResponse.json)
            case _ => InternalServerError(Json.toJson(SdecErrorResponse("unexpected response found", None)))
          }
      }.recover { e =>
        logger.error(e.getMessage)
        InternalServerError(Json.toJson(SdecErrorResponse("unexpected error", None)))
      }
  }
}
