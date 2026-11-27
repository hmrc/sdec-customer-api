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

import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, UpstreamErrorResponse}
import uk.gov.hmrc.sdeccustomerapi.config.AppConfigAlgebra
import uk.gov.hmrc.sdeccustomerapi.model.{ThreadReference, ThreadUpdate}
import uk.gov.hmrc.sdeccustomerapi.util.RequestAwareLogger

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait IpaasConnectorAlgebra {
  def getThreadDetails(threadReference: ThreadReference)(using
    hc: HeaderCarrier
  ): Future[Either[UpstreamErrorResponse, HttpResponse]]

  def updateThreadDetails(
    threadReference: ThreadReference,
    threadUpdate:    ThreadUpdate
  )(using hc: HeaderCarrier): Future[Either[UpstreamErrorResponse, HttpResponse]]
}

class IpaasConnector @Inject() (ipaasClient: IpaasClientAlgebra, appConfig: AppConfigAlgebra)(using
  ec: ExecutionContext
) extends IpaasConnectorAlgebra {

  private val logger = new RequestAwareLogger(this.getClass)

  def getThreadDetails(threadReference: ThreadReference)(using
    hc: HeaderCarrier
  ): Future[Either[UpstreamErrorResponse, HttpResponse]] = {

    val path = s"${appConfig.ipaasThreadUrl}/api/v1/thread/${threadReference.value}"
    ipaasClient.get(path).recover { e =>
      logger.error(e.getMessage)
      Right(HttpResponse(500))
    }
  }

  def updateThreadDetails(
    threadReference: ThreadReference,
    threadUpdate:    ThreadUpdate
  )(using hc: HeaderCarrier): Future[Either[UpstreamErrorResponse, HttpResponse]] = {

    val path = s"${appConfig.ipaasThreadUrl}/api/v1/thread/${threadReference.value}"
    ipaasClient.put(path, threadUpdate).recover { e =>
      logger.error(e.getMessage)
      Right(HttpResponse(500))
    }
  }

}
