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

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.sdeccustomerapi.infrastructure.AuthenticatedActionBuilder
import uk.gov.hmrc.sdeccustomerapi.model.{SdecErrorResponse, ThreadReference, ThreadUpdate}
import uk.gov.hmrc.sdeccustomerapi.service.IpaasServiceAlgebra
import uk.gov.hmrc.sdeccustomerapi.util.RequestAwareLogger
import uk.gov.hmrc.sdeccustomerapi.util.implicits.RequestSyntax.JsonRequestOps
import uk.gov.hmrc.sdeccustomerapi.util.implicits.ResponseSyntax.ResponseOps

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class ThreadController @Inject() (
  cc:           ControllerComponents,
  authAction:   AuthenticatedActionBuilder,
  ipaasService: IpaasServiceAlgebra
) extends BackendController(cc) {

  private val logger = new RequestAwareLogger(this.getClass)

  def requestAccess: Action[AnyContent] = Action(Ok)

  def updateThread(threadRef: String): Action[JsValue] =
    authAction.async { request =>
      implicit val headerCarrier: HeaderCarrier = hc(request)
      request.bodyAs[ThreadUpdate] match {
        case Right(update) =>
          ipaasService.updateThreadDetails(ThreadReference(threadRef), update).withResponseHandling()
        case Left(errorMessage) =>
          logger.error(errorMessage.mkString(","))
          Future.successful(BadRequest(Json.toJson(SdecErrorResponse("incompatible payload sent"))))
      }
    }
}
