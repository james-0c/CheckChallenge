package models

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class CouncilTaxResponsibilitySpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "CouncilTaxResponsibility" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(CouncilTaxResponsibility.values.toSeq)

      forAll(gen) {
        councilTaxResponsibility =>

          JsString(councilTaxResponsibility.toString).validate[CouncilTaxResponsibility].asOpt.value mustEqual councilTaxResponsibility
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!CouncilTaxResponsibility.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[CouncilTaxResponsibility] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(CouncilTaxResponsibility.values.toSeq)

      forAll(gen) {
        councilTaxResponsibility =>

          Json.toJson(councilTaxResponsibility) mustEqual JsString(councilTaxResponsibility.toString)
      }
    }
  }
}
