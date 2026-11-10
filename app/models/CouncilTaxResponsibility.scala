package models

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait CouncilTaxResponsibility

object CouncilTaxResponsibility extends Enumerable.Implicits {

  case object Yes extends WithName("yes") with CouncilTaxResponsibility
  case object No extends WithName("no") with CouncilTaxResponsibility
  case object Acting extends WithName("acting") with CouncilTaxResponsibility

  val values: Seq[CouncilTaxResponsibility] = Seq(
    Yes, No, Acting
  )

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map {
    case (value, index) =>
      RadioItem(
        content = Text(messages(s"councilTaxResponsibility.${value.toString}")),
        value   = Some(value.toString),
        id      = Some(s"value_$index")
      )
  }

  implicit val enumerable: Enumerable[CouncilTaxResponsibility] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
