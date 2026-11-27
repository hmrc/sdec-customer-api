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
import play.api.http.HeaderNames.CONTENT_TYPE
import play.api.http.MimeTypes.JSON
import play.api.libs.json.{Json, Writes}
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps, UpstreamErrorResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait IpaasClientAlgebra {
  def get(path: String)(using
    hc: HeaderCarrier
  ): Future[Either[UpstreamErrorResponse, HttpResponse]]

  def post[A: Tag](path: String, body: A)(using
    hc:     HeaderCarrier,
    writes: Writes[A]
  ): Future[Either[UpstreamErrorResponse, HttpResponse]]

  def put[A: Tag](path: String, body: A)(using
    hc:     HeaderCarrier,
    writes: Writes[A]
  ): Future[Either[UpstreamErrorResponse, HttpResponse]]
}

class IpaasClient @Inject() (httpClientV2: HttpClientV2)(using ec: ExecutionContext) extends IpaasClientAlgebra {

  private val commonHeaders: List[(String, String)] = List(
    CONTENT_TYPE -> JSON
  )

  def get(path: String)(using
    hc: HeaderCarrier
  ): Future[Either[UpstreamErrorResponse, HttpResponse]] = {
    val requestHeaders = hc.headers(Seq("CorrelationId")) ++ commonHeaders
    httpClientV2
      .get(url"$path")
      .setHeader(requestHeaders*)
      .execute[Either[UpstreamErrorResponse, HttpResponse]]
  }

  def post[A: Tag](path: String, body: A)(using
    hc:     HeaderCarrier,
    writes: Writes[A]
  ): Future[Either[UpstreamErrorResponse, HttpResponse]] = {
    val requestHeaders =
      hc.headers(Seq("CorrelationId")) ++ commonHeaders
    httpClientV2
      .post(url"$path")
      .setHeader(requestHeaders*)
      .withBody(Json.toJson(body))
      .execute[Either[UpstreamErrorResponse, HttpResponse]]
  }

  def put[A: Tag](path: String, body: A)(using
    hc:     HeaderCarrier,
    writes: Writes[A]
  ): Future[Either[UpstreamErrorResponse, HttpResponse]] = {
    val requestHeaders =
      hc.headers(Seq("CorrelationId")) ++ commonHeaders
    httpClientV2
      .put(url"$path")
      .setHeader(requestHeaders*)
      .withBody(Json.toJson(body))
      .execute[Either[UpstreamErrorResponse, HttpResponse]]
  }

}
