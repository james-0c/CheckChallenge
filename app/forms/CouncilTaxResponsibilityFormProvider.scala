package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms._
import models.CouncilTaxResponsibility

class CouncilTaxResponsibilityFormProvider @Inject() extends Mappings {

  def apply(): Form[CouncilTaxResponsibility] =
    Form(
      "value" -> enumerable[CouncilTaxResponsibility]("councilTaxResponsibility.error.required")
    )
}
