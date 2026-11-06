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
import views.html.SearchResultsView
import models.userAnswers
import org.mockito.Mockito
import org.mockito.ArgumentMatchers.any
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.Helper._
import pages.PropertySearchPage
import repositories.SessionRepository
import views.html.SearchResultsView

class SearchResultsControllerSpec extends SpecBase with MockitoSugar {

  "SearchResults Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers = UserAnswers("id")
        .set(PropertySearchPage, "SW1A 1AA").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.SearchResultsController.onPageLoad(models.NormalMode).url)

        val result = route(application, request).value

        /*val view = application.injector.instanceOf[SearchResultsView]*/

        status(result) mustEqual OK
        /*contentAsString(result) mustEqual view()(request, messages(application)).toString*/
        contentAsString(result) must include("Select your property")
      }
    }
  }
}
