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

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.PropertyDetailsView
import models.{NormalMode, SelectedProperty, UserAnswers, PropertyData}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.SelectedPropertyPage
import play.api.inject.bind
import repositories.SessionRepository
import scala.concurrent.Future

class PropertyDetailsControllerSpec extends SpecBase with MockitoSugar {

  private val propertyRef = "02512000900027"

  private lazy val propertyDetailsRoute =
    routes.PropertyDetailsController.onPageLoad(propertyRef).url

  "PropertyDetails Controller" - {

    "must return OK and the selected property details for a valid reference" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, propertyDetailsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PropertyDetailsView]

        val property = PropertyData
          .getPropertyByLocalAuthorityRef(propertyRef)
          .value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(property, NormalMode)(request, messages(application)).toString
      }
    }

    "must save the selected property's address and Council Tax band" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()
        
        running(application) {
          val request = FakeRequest(GET, propertyDetailsRoute)

          val result = route(application, request).value

          status(result) mustEqual OK

          val captor = ArgumentCaptor.forClass(classOf[UserAnswers])

          verify(mockSessionRepository).set(captor.capture())

          val savedAnswers = captor.getValue

          savedAnswers.get(SelectedPropertyPage) mustEqual Some(
            SelectedProperty(
              address = "FLAT 2, 9-12 DEPTFORD BROADWAY, LONDON, SE8 4PB",
              councilTaxBand = "C"
            )
          )
        }
    }

    "must return NOT_FOUND when the property refrence does not exist" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val invalidRoute =
          routes.PropertyDetailsController
            .onPageLoad("UNKNOWN-REFERENCE")
            .url

        val request = FakeRequest(GET, invalidRoute)

        val result = route(application, request).value

        status(result) mustEqual NOT_FOUND
      }

    }

  }
}
