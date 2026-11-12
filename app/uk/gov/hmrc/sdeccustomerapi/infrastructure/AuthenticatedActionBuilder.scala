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

package uk.gov.hmrc.sdeccustomerapi.infrastructure

import com.nimbusds.jwt.JWTClaimsSet
import jakarta.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.*

import scala.concurrent.Future

@Singleton
class AuthenticatedActionBuilder @Inject() (
  cc:           ControllerComponents,
  jwtValidator: TokenValidator
) extends AbstractController(cc) {

  def async(
    block: AuthenticatedRequest[AnyContent] => Future[Result]
  ): Action[AnyContent] =
    Action.async(cc.parsers.default) { request =>
      validateRequest(request) match {
        case Left(error) =>
          Future.successful(error)

        case Right(claims) =>
          block(
            new AuthenticatedRequest(
              claims,
              request
            )
          )
      }
    }

  private def validateRequest(
    request: Request[AnyContent]
  ): Either[Result, JWTClaimsSet] =
    for {
      token  <- extractToken(request)
      claims <- jwtValidator
                  .validate(token)
                  .toEither
                  .left
                  .map { _ =>
                    Unauthorized(
                      Json.obj("message" -> "Invalid bearer token")
                    )
                  }
    } yield claims

  private def extractToken(
    request: Request[AnyContent]
  ): Either[Result, String] =
    request.headers
      .get("Authorization")
      .map(_.stripPrefix("Bearer ").trim)
      .filter(_.nonEmpty)
      .toRight(
        Unauthorized(
          Json.obj(
            "message" -> "Bearer token not supplied"
          )
        )
      )
}
