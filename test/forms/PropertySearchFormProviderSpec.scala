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

package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError
import org.scalatest.matchers.must.Matchers.*

class PropertySearchFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "propertySearch.error.required"
  val lengthKey = "propertySearch.error.length"
  val invalidKey = "propertySearch.error.invalid"
  val maxLength = 12

  val form = new PropertySearchFormProvider()()

  ".value" - {

    val fieldName = "value"

    "bind valid UK postcodes" in {
      val validPostcodes = Seq("SW1A 1AA", "M1 1AE", "B33 8TH")

      validPostcodes.foreach { postcode =>
        val result = form.bind(Map(fieldName -> postcode))
        result.errors mustBe empty
      }
    }

    "reject an invalid UK postcode" in {
      val result = form.bind(Map(fieldName -> "NOTAPOSTCODE"))

      result.errors.map(_.message) must contain(invalidKey)
    }

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}

