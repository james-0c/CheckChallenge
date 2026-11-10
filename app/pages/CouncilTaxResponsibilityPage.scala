package pages

import models.CouncilTaxResponsibility
import play.api.libs.json.JsPath

case object CouncilTaxResponsibilityPage extends QuestionPage[CouncilTaxResponsibility] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "councilTaxResponsibility"
}
