# CheckChallenge

Link to view the propertysearch page http://localhost:9000/CheckCouncilTax/propertySearch


# Check Council Tax – Developer Guide

## 1. Project overview

This project is a Scala Play frontend application built using the HMRC frontend scaffold (`hmrc-frontend-scaffold.g8`).

The aim is to build a six-page Council Tax journey:

| Page | Name | Purpose |
|---|---|---|
| 1 | Property Search | Enter a postcode to search for properties |
| 2 | Search Results | Display matching properties |
| 3 | Property Details | Display Council Tax details for a selected property |
| 4 | Responsibility Question | Ask whether the user is responsible for Council Tax |
| 5 | No Permission | Explain what happens if the user is not responsible |
| 6 | Check Your Answers | Show saved answers and allow the user to confirm |

The application uses:

- Scala
- Play Framework
- Twirl templates
- HMRC frontend libraries and GOV.UK Design System components
- MongoDB for storing journey answers
- ScalaTest for automated tests

This guide covers the implementation of Page 1 and the planned approach for Page 2.

---

# Part 1 – Page 1: Property Search

## 2. What Page 1 does

Page 1 allows a user to enter a postcode to search for a property.

The page needs to:

1. Display a postcode input.
2. Validate that the postcode is present.
3. Validate that the postcode follows an accepted UK postcode format.
4. Display validation errors if the input is invalid.
5. Save the postcode to the user's journey data in MongoDB.
6. Redirect the user to the next page after a successful submission.
7. Populate the input if the user returns to the page and has already entered a postcode.

The submitted form field is called `value`.

An example valid postcode is:

`SW1A 1AA`

## 3. Page 1 file structure

The main files involved in Page 1 are:

| File | Responsibility |
|---|---|
| `app/controllers/PropertySearchController.scala` | Handles GET and POST requests |
| `app/forms/PropertySearchFormProvider.scala` | Defines the form and validation |
| `app/pages/PropertySearchPage.scala` | Identifies the question in journey data |
| `app/views/PropertySearchView.scala.html` | Displays the page |
| `app/navigation/Navigator.scala` | Determines where the user goes next |
| `conf/messages.en` or `conf/messages` | Holds page text and validation messages |
| `conf/app.routes` | Maps URLs to controller actions |
| `test/controllers/PropertySearchControllerSpec.scala` | Tests the controller |
| `test/forms/PropertySearchFormProviderSpec.scala` | Tests form validation |
| `test/controllers/actions/DataRetrievalActionSpec.scala` | Tests retrieval of journey data |

There are also shared scaffold components that make the page work:

- `IdentifierAction`
- `DataRetrievalAction`
- `DataRequiredAction`
- `UserAnswers`
- `SessionRepository`

Understanding these shared components is important because the other five pages will use the same patterns.

---

## 4. Understanding the form provider

File:

`app/forms/PropertySearchFormProvider.scala`

The form provider defines what information the application expects from the user and how it validates that information.

The form is a `Form[String]` because the postcode is stored as a string.

The important parts are:

### Required validation

The user cannot submit an empty postcode.

This uses the scaffold's `text` mapping, which applies the required-field validation.

### Maximum length validation

The form checks that the postcode does not exceed the configured maximum length.

### Postcode validation

The regular expression checks the general structure of a UK postcode.

For example:

- `SW1A 1AA` – valid
- `M1 1AE` – valid
- `B33 8TH` – valid
- `NOTAPOSTCODE` – invalid

The regular expression is:

`(?i)^[A-Z]{1,2}[0-9][A-Z0-9]?\s?[0-9][A-Z]{2}$`

The `(?i)` makes the expression case-insensitive.

The `\s?` allows an optional space between the outward and inward parts of the postcode.

**Important:** this is a format check, not proof that a postcode exists or that a property is registered at that postcode. Actual address matching will be handled by Page 2.

---

## 5. Understanding the Page object

File:

`app/pages/PropertySearchPage.scala`

The page object is:

`PropertySearchPage`

It extends `QuestionPage[String]`.

This tells the application that the answer for this page is a string and identifies where it should be stored in the journey data.

The page's JSON path is based on its name:

`propertySearch`

For example, the saved journey data might look like:

```json
{
  "propertySearch": "SW1A 1AA"
}
```

The advantage of using a page object is that the controller doesn't need to know the underlying JSON structure. It can simply refer to `PropertySearchPage`.

The same approach will be used for future questions.

---

## 6. Understanding the controller

File:

`app/controllers/PropertySearchController.scala`

The controller handles two main actions:

### `onPageLoad`

This handles the GET request.

When the user opens Page 1, the controller:

1. Identifies the user.
2. Retrieves their existing journey data.
3. Ensures that journey data is available.
4. Checks whether the postcode has already been answered.
5. Displays the form.

The important code is:

```scala
val preparedForm = request.userAnswers.get(PropertySearchPage) match {
  case None        => form
  case Some(value) => form.fill(value)
}
```

This is how pre-population works.

If the user has not entered a postcode, the blank form is displayed.

If they have previously entered one, the form is filled with their saved postcode.

### `onSubmit`

This handles the POST request.

The controller:

1. Retrieves the user's journey data.
2. Binds the submitted form data.
3. Checks whether validation succeeds.
4. Displays the page again with errors if validation fails.
5. Updates the `UserAnswers` object if validation succeeds.
6. Saves the updated answers using `SessionRepository`.
7. Redirects to the next page using `Navigator`.

The important section is:

```scala
form.bindFromRequest().fold(
  formWithErrors =>
    Future.successful(BadRequest(view(formWithErrors, mode))),

  value =>
    for {
      updatedAnswers <- Future.fromTry(
        request.userAnswers.set(PropertySearchPage, value)
      )
      _ <- sessionRepository.set(updatedAnswers)
    } yield Redirect(
      navigator.nextPage(PropertySearchPage, mode, updatedAnswers)
    )
)
```

There are two important operations here:

**First:**

```scala
request.userAnswers.set(PropertySearchPage, value)
```

This updates the journey answers in memory.

**Second:**

```scala
sessionRepository.set(updatedAnswers)
```

This persists the updated journey data to MongoDB.

The distinction matters: `UserAnswers.set` alone does not save anything to MongoDB.

---

## 7. Understanding the authentication and data retrieval flow

This was one of the more important parts of getting Page 1 working locally.

The original HMRC scaffold expects an authenticated user. For local development, we wanted to run the application without redirecting to HMRC authentication.

We therefore introduced a local identifier action.

### 7.1 IdentifierAction

File:

`app/controllers/actions/LocalIdentifierAction.scala`

The local identifier action creates an identifier for the current request:

```scala
block(IdentifierRequest(request, "local-test-user"))
```

This gives the request a fixed user ID:

`local-test-user`

It means we can develop locally without needing an HMRC login or an existing session cookie.

### 7.2 Module.scala

File:

`app/config/Module.scala`

The module controls which implementation of `IdentifierAction` gets injected into the application.

For local development, the binding is:

```scala
bind(classOf[IdentifierAction])
  .to(classOf[LocalIdentifierAction])
  .asEagerSingleton()
```

The original scaffold uses:

```scala
AuthenticatedIdentifierAction
```

The local action is a development convenience. It must not be used as the production authentication mechanism.

Before deploying to an authenticated environment, restore the appropriate authenticated binding.

### 7.3 DataRetrievalAction

File:

`app/controllers/actions/DataRetrievalAction.scala`

This action uses the identifier to retrieve the user's saved answers.

It calls:

```scala
sessionRepository.get(request.userId)
```

Originally, if MongoDB had no document for the user, the action returned `None`.

For our local journey, we changed this behaviour:

```scala
val userAnswers =
  existingAnswers.getOrElse(UserAnswers(request.userId))
```

This means:

- If MongoDB contains saved answers, retrieve them.
- If MongoDB does not contain saved answers, create an empty `UserAnswers` object.

The action then supplies:

```scala
Some(userAnswers)
```

to the next action in the chain.

This allows a new local user to start a journey without being redirected to Journey Recovery.

### 7.4 DataRequiredAction

File:

`app/controllers/actions/DataRequiredAction.scala`

This action checks that the request has journey data.

If `userAnswers` is `None`, it redirects to Journey Recovery.

If `userAnswers` exists, it converts the optional request into a `DataRequest`, allowing the controller to access the answers.

Because our updated `DataRetrievalActionImpl` now creates empty answers for a new local user, the normal Page 1 request can continue through this action.

### 7.5 Full request flow

The controller uses:

```scala
identify andThen getData andThen requireData
```

The flow is:

```text
Browser request
      |
      v
IdentifierAction
      |
      v
LocalIdentifierAction
      |
      v
Creates user ID:
local-test-user
      |
      v
DataRetrievalAction
      |
      v
SessionRepository
      |
      v
Look for answers in MongoDB
      |
      +----------------------+
      |                      |
      v                      v
Answers found          No answers found
      |                      |
      v                      v
Retrieve answers       Create empty
                       UserAnswers
      |                      |
      +-----------+----------+
                  |
                  v
           DataRequiredAction
                  |
                  v
         PropertySearchController
                  |
                  v
           Display Page 1
```

---

## 8. Understanding MongoDB storage

File:

`app/repositories/SessionRepository.scala`

The repository handles communication with MongoDB.

The collection is:

`user-answers`

The database name comes from the application configuration. In this project, it is configured using:

`CheckCouncilTax`

When a user submits a valid postcode, the controller calls:

```scala
sessionRepository.set(updatedAnswers)
```

The repository saves or replaces the user's journey document using their ID.

A simplified example of the saved document is:

```json
{
  "_id": "local-test-user",
  "data": {
    "propertySearch": "SW1A 1AA"
  },
  "lastUpdated": "2026-11-02T..."
}
```

The repository also has a TTL index based on the configured cache time.

The application configuration currently sets the journey-data expiry to 900 seconds (15 minutes).

### Local development consideration

Because the local identifier is fixed as `local-test-user`, all local browser sessions using this identifier share the same journey data.

For now, this is useful for development, but it means you may need to clear the local document when testing a completely new journey.

---

## 9. Routes

File:

`conf/app.routes`

The GET route is:

```scala
GET /propertySearch controllers.PropertySearchController.onPageLoad(mode: Mode = NormalMode)
```

The POST route should be:

```scala
POST /propertySearch controllers.PropertySearchController.onSubmit(mode: Mode = NormalMode)
```

The generated Play router mounts the application under the service prefix, so the local page URL is:

`http://localhost:9000/CheckCouncilTax/propertySearch`

---

## 10. Testing Page 1

The project uses ScalaTest and the scaffold's fake actions to test controllers independently of HMRC authentication and MongoDB.

### Form provider tests

File:

`test/forms/PropertySearchFormProviderSpec.scala`

These test:

- Empty postcode validation.
- Valid postcode formats.
- Invalid postcode formats.
- Maximum length validation.

### Controller tests

File:

`test/controllers/PropertySearchControllerSpec.scala`

These test:

| Test | Expected result |
|---|---|
| GET Page 1 | 200 OK |
| GET with previously saved postcode | 200 OK with prefilled value |
| POST with valid postcode | 303 redirect |
| POST with invalid postcode | 400 Bad Request |
| GET with an empty journey | 200 OK |
| POST with a valid postcode and empty journey | 303 redirect |

### Data retrieval tests

File:

`test/controllers/actions/DataRetrievalActionSpec.scala`

These test:

- An empty `UserAnswers` object is created if no MongoDB document exists.
- Existing answers are returned when a MongoDB document exists.

The tests compare the ID and data rather than the full `UserAnswers` object because `lastUpdated` is generated using the current time.

### Running tests

Run only the Page 1 controller tests:

```bash
sbt "testOnly controllers.PropertySearchControllerSpec"
```

Run only the data retrieval tests:

```bash
sbt "testOnly controllers.actions.DataRetrievalActionSpec"
```

Run the full test suite:

```bash
sbt test
```

At this stage, the full test suite passes.

---

# Part 2 – Page 2: Search Results

## 11. What Page 2 needs to do

Page 2 displays properties matching the postcode entered on Page 1.

For the exercise, we will use hardcoded mock property data rather than a real Council Tax API.

The page should:

1. Retrieve the postcode saved on Page 1.
2. Display between three and five mock properties.
3. Display each property's address.
4. Make each address clickable.
5. Navigate to Page 3 when the user selects a property.

For example, entering `SW1A 1AA` might display:

```text
Properties found

Select the property you want to check.

1. 10 Downing Street, London, SW1A 2AA

2. 11 Downing Street, London, SW1A 2AB

3. 12 Downing Street, London, SW1A 2AC

4. 13 Downing Street, London, SW1A 2AD
```

These are illustrative mock results, not actual property matches.

---

## 12. Recommended Page 2 files

We will follow the same structure as Page 1.

| File | Purpose |
|---|---|
| `app/controllers/SearchResultsController.scala` | Handles the search results request |
| `app/views/SearchResultsView.scala.html` | Displays the results |
| `app/models/Property.scala` | Represents a mock property |
| `app/navigation/Navigator.scala` | Handles navigation to Page 3 |
| `conf/app.routes` | Adds the Page 2 route |
| `conf/messages` | Contains Page 2 text |
| `test/controllers/SearchResultsControllerSpec.scala` | Tests the results controller |

We may also introduce a small mock data provider if we want to keep the controller clean.

---

## 13. Step-by-step implementation plan

### Step 1 – Create the mock property model

Create:

`app/models/Property.scala`

The model should represent a property with fields such as:

- A unique property ID.
- An address.
- A Council Tax band.
- An effective date.
- A local council.
- A court code.

We will use a small list of mock properties initially.

Keeping the properties in a model makes it easier to reuse them on Page 3.

### Step 2 – Create the mock property data

For the exercise, create three to five mock properties.

Initially, we can keep these in a simple object or provider.

The provider should expose a method that returns properties for a postcode.

For example:

```scala
def propertiesForPostcode(postcode: String): Seq[Property]
```

Initially, this can return the same mock results for any valid postcode.

Later, we could make the mock results depend on the entered postcode.

### Step 3 – Create SearchResultsController

Create:

`app/controllers/SearchResultsController.scala`

The controller should follow the same pattern as Page 1:

```scala
identify andThen getData andThen requireData
```

It should retrieve the saved postcode:

```scala
request.userAnswers.get(PropertySearchPage)
```

Then it should retrieve the mock properties and display them.

This is important because Page 2 should not ask the user to enter the postcode again. It should use the answer already stored from Page 1.

### Step 4 – Create SearchResultsView

Create:

`app/views/SearchResultsView.scala.html`

The view should:

- Have a clear page heading.
- Explain that the user should select a property.
- Display the properties as a list.
- Make each property address a link.
- Use GOV.UK Design System styling.

Each address should link to Page 3 and identify the selected property.

### Step 5 – Add the Page 2 route

Add a GET route to:

`conf/app.routes`

For example:

```scala
GET /searchResults controllers.SearchResultsController.onPageLoad()
```

The exact controller signature and route parameters will depend on how we decide to identify the selected property.

### Step 6 – Update Navigator

Currently, the scaffold Navigator sends pages to the index page by default.

We need to add an explicit mapping for `PropertySearchPage` so that a successful postcode submission redirects to Search Results.

Conceptually:

```scala
case PropertySearchPage =>
  _ => routes.SearchResultsController.onPageLoad()
```

This is the point where Page 1 and Page 2 become connected.

### Step 7 – Add messages

All user-facing text should go into `conf/messages`.

For example:

```text
searchResults.title = Properties found
searchResults.heading = Select a property
searchResults.noResults = No properties were found for this postcode.
```

The actual wording can be refined as we build the page.

### Step 8 – Write controller tests

The Search Results controller should have tests covering:

| Scenario | Expected result |
|---|---|
| GET with a saved postcode | 200 OK |
| Results are displayed | Mock properties appear |
| Three to five results are returned | Correct result count |
| User has no saved postcode | Appropriate recovery behaviour |
| A selected property link is generated | Link points to Page 3 |

We should also test that the page uses the saved postcode rather than relying on a manually entered URL parameter.

---

## 14. How Page 1 and Page 2 will connect

The intended journey is:

```text
PAGE 1 – PROPERTY SEARCH
          |
          v
User enters postcode
          |
          v
Form validates postcode
          |
          v
Postcode saved to MongoDB
          |
          v
Navigator redirects to Page 2
          |
          v
PAGE 2 – SEARCH RESULTS
          |
          v
Retrieve saved postcode
          |
          v
Get mock properties
          |
          v
Display 3–5 property links
          |
          v
User selects a property
          |
          v
PAGE 3 – PROPERTY DETAILS
```

The key concept is that the journey data is shared between pages.

We don't need to pass the postcode manually through every controller. Page 2 can retrieve it from `UserAnswers`.

---

# Part 3 – Development approach for the remaining pages

## 15. Keep using the same scaffold pattern

As we build each page, we should keep following the same general structure:

1. Define the page's purpose.
2. Create the form provider if the page collects an answer.
3. Create the page object.
4. Create the controller.
5. Create the Twirl view.
6. Add messages.
7. Add routes.
8. Update navigation.
9. Write unit tests.
10. Run the tests before moving on.

Not every page needs a form provider. For example, Page 5 is a static information page.

## 16. Planned journey data

The journey will gradually build up saved answers.

A simplified example of the final data might look like:

```json
{
  "propertySearch": "SW1A 1AA",
  "selectedProperty": {
    "id": "property-1",
    "address": "10 Downing Street, London",
    "band": "H"
  },
  "responsibleForCouncilTax": "yes"
}
```

The actual JSON structure will depend on the page objects and models we create.

The Check Your Answers page will use these saved values to display the user's responses.

---

# Part 4 – Current development checklist

## Page 1 – Property Search

- [x] Postcode input
- [x] Required validation
- [x] Postcode format validation
- [x] Page object
- [x] Controller GET action
- [x] Controller POST action
- [x] Form pre-population
- [x] MongoDB journey storage
- [x] Local identifier action
- [x] Data retrieval for new journeys
- [x] Controller tests
- [x] Form provider tests
- [x] Data retrieval tests
- [x] Full test suite passing

## Page 2 – Search Results

- [ ] Property model
- [ ] Mock property data
- [ ] Search Results controller
- [ ] Search Results view
- [ ] Retrieve postcode from journey data
- [ ] Display three to five properties
- [ ] Property links to Page 3
- [ ] Add route
- [ ] Update Navigator
- [ ] Add messages
- [ ] Add controller tests

---

## 17. Useful commands

Start the application:

```bash
sbt run
```

Run all tests:

```bash
sbt test
```

Run a specific controller test:

```bash
sbt "testOnly controllers.PropertySearchControllerSpec"
```

Run a specific form test:

```bash
sbt "testOnly forms.PropertySearchFormProviderSpec"
```

Compile without running tests:

```bash
sbt compile
```

---

## 18. Important learning points

The most important concepts learned while building Page 1 are:

**Controllers:** handle requests and decide what response to return.

**Form providers:** define form fields and validation rules.

**Page objects:** identify where answers are stored in journey data.

**UserAnswers:** hold the answers collected during the journey.

**SessionRepository:** persists journey data to MongoDB.

**Action composition:** `identify andThen getData andThen requireData` prepares the request before it reaches the controller.

**Navigator:** decides which page comes next.

**Twirl views:** render the HTML shown to the user.

**Messages:** keep user-facing text separate from Scala code.

**Tests:** verify that the application behaves correctly without needing to manually use the browser for every scenario.

The goal is to understand how these pieces work together, rather than simply copy the implementation from one page to another.

---

**Current milestone:** Page 1 is implemented and the full test suite passes.

**Next milestone:** Build Page 2 (Search Results), using hardcoded mock properties and connecting it to the postcode saved by Page 1.