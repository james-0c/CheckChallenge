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

import com.google.inject.Inject
import controllers.actions._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView
import models.CouncilTaxResponsibility
import models.CheckMode
import pages.{CouncilTaxResponsibilityPage, SelectedPropertyPage}
import uk.gov.hmrc.govukfrontend.views.Aliases.Text

class CheckYourAnswersController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: CheckYourAnswersView
                                          ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val messages = messagesApi.preferred(request)

      val propertyRow = request.userAnswers.get(SelectedPropertyPage).map { property =>
        SummaryListRowViewModel(
            key = KeyViewModel(Text(messages("checkYourAnswers.property"))),
            value = ValueViewModel(
              Text(s"${property.address}, ${messages("checkYourAnswers.band")} ${property.councilTaxBand}")
            ),
            actions = Seq(
              ActionItemViewModel(
                content = Text(messages("site.change")),
                href = routes.PropertyDetailsController.onPageLoad(property.localAuthorityRef).url
              ).withVisuallyHiddenText(messages("checkYourAnswers.property"))
            )
        )
      }

      val responsibilityRow = request.userAnswers.get(CouncilTaxResponsibilityPage).map { answer =>
          val answerText = answer match {
              case CouncilTaxResponsibility.Yes =>
                  messages("councilTaxResponsibility.yes")
              case CouncilTaxResponsibility.No =>
                  messages("councilTaxResponsibility.no")
              case CouncilTaxResponsibility.Acting =>
                  messages("councilTaxResponsibility.acting")
          }

          SummaryListRowViewModel(
              key = KeyViewModel(Text(messages("checkYourAnswers.responsibility"))),
              value = ValueViewModel(Text(answerText)),
              actions = Seq(
                  ActionItemViewModel(
                      content = Text(messages("site.change")),
                      href = routes.CouncilTaxResponsibilityController.onPageLoad(CheckMode).url
                  ).withVisuallyHiddenText(messages("checkYourAnswers.responsibility"))
              )
          )
        }

      val rows = Seq(propertyRow, responsibilityRow).flatten
      val list = SummaryListViewModel(rows)

      Ok(view(list))
  }

  def onSubmit(): Action[AnyContent] =
      (identify andThen getData andThen requireData) {
          implicit request =>

              val selectedProperty = request.userAnswers.get(SelectedPropertyPage)
              val responsibility = request.userAnswers.get(CouncilTaxResponsibilityPage)

              (selectedProperty, responsibility) match {
                  case (Some(_), Some(CouncilTaxResponsibility.Yes)) |
                       (Some(_), Some(CouncilTaxResponsibility.Acting)) =>
                      Redirect(routes.ConfirmationController.onPageLoad())
                  
                  case(Some(_), Some(CouncilTaxResponsibility.No)) =>
                      Redirect(routes.NoPermissionController.onPageLoad())

                  case _ =>
                      Redirect(routes.IndexController.onPageLoad())
              }
      }
}
