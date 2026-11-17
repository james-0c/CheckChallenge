package forms

import forms.behaviours.OptionFieldBehaviours
import models.CouncilTaxResponsibility
import play.api.data.FormError

class CouncilTaxResponsibilityFormProviderSpec extends OptionFieldBehaviours {

  val form = new CouncilTaxResponsibilityFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "councilTaxResponsibility.error.required"

    behave like optionsField[CouncilTaxResponsibility](
      form,
      fieldName,
      validValues  = CouncilTaxResponsibility.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
