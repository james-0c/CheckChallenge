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

package controllers

import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.PropertyDetailsView
import controllers.actions._
import javax.inject.Inject
import models.{Mode, PropertyData, SelectedProperty}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import forms._
import repositories.SessionRepository
import scala.concurrent.{ExecutionContext, Future}
import pages.SelectedPropertyPage

class PropertyDetailsController @Inject()(
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    val controllerComponents: MessagesControllerComponents,
    view: PropertyDetailsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

    def onPageLoad(localAuthorityRef: String, mode: Mode): Action[AnyContent] = 
        (identify andThen getData andThen requireData).async {
            implicit request =>

                PropertyData.getPropertyByLocalAuthorityRef(localAuthorityRef) match {
                    case Some(property) =>
                        for {
                            updatedAnswers <- Future.fromTry(
                                request.userAnswers.set(
                                    SelectedPropertyPage,
                                    SelectedProperty(
                                        address = property.address,
                                        councilTaxBand = property.councilTaxBand,
                                        localAuthorityRef = property.localAuthorityRef
                                    )
                                )
                            )
                            _ <- sessionRepository.set(updatedAnswers)
                        }
                        yield Ok(view(property, mode))

                    case None =>
                        Future.successful(NotFound)
                }
        }
}