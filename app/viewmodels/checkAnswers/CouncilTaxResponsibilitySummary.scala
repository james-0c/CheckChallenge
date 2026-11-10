package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.CouncilTaxResponsibilityPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object CouncilTaxResponsibilitySummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(CouncilTaxResponsibilityPage).map {
      answer =>

        val value = ValueViewModel(
          HtmlContent(
            HtmlFormat.escape(messages(s"councilTaxResponsibility.$answer"))
          )
        )

        SummaryListRowViewModel(
          key     = "councilTaxResponsibility.checkYourAnswersLabel",
          value   = value,
          actions = Seq(
            ActionItemViewModel("site.change", routes.CouncilTaxResponsibilityController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("councilTaxResponsibility.change.hidden"))
          )
        )
    }
}
