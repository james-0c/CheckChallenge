# CheckCouncilTax – Development Progress

## Project overview

A Scala Play HMRC frontend application that guides users through a Council Tax property-checking journey.

The application uses the standard HMRC frontend architecture, including:
- Scala and Play Framework
- Twirl templates
- GOV.UK Design System components
- MongoDB for persisting user answers
- HMRC session and navigation patterns
- ScalaTest for unit testing

The intended journey consists of six pages:

1. Property Search
2. Search Results
3. Property Details
4. Responsible for Council Tax?
5. No Permission
6. Check Your Answers

---

## Page 1 – Property Search

**Status: Completed**

### Purpose
Allows the user to enter a postcode to search for their property.

### Implementation

The following files were created or updated:

- `app/controllers/PropertySearchController.scala`
- `app/forms/PropertySearchFormProvider.scala`
- `app/pages/PropertySearchPage.scala`
- `app/views/PropertySearchView.scala.html`
- `conf/messages.en`
- `conf/app.routes`

### Functionality

- Accepts a UK postcode.
- Validates that the postcode is present and follows the expected UK postcode format.
- Displays validation errors when the input is invalid.
- Saves the submitted postcode into `UserAnswers`.
- Uses `SessionRepository` to persist the user's answers to MongoDB.
- Supports normal and check modes.
- Uses the HMRC `Navigator` to determine the next page.

### Important implementation details

The postcode is stored against `PropertySearchPage` as a `String`.

The controller follows the standard HMRC pattern:
- `onPageLoad` retrieves and prefills existing answers.
- `onSubmit` binds and validates the submitted form.
- Valid answers are saved through the session repository.
- Invalid answers return the page with validation errors.

---

## Page 2 – Search Results

**Status: Implemented – compilation successful**

### Purpose
Displays mock properties matching the postcode submitted on Page 1.

### Files created or updated

- `app/models/Property.scala`
- `app/models/PropertyData.scala`
- `app/controllers/SearchResultsController.scala`
- `app/views/SearchResultsView.scala.html`
- `app/navigation/Navigator.scala`
- `conf/app.routes`
- `conf/messages.en`

### Property model

The `Property` model represents a Council Tax property and contains information such as:

- Property ID
- Address
- Council Tax band
- Effective date
- Local authority
- Court code

The exact field names are defined in `Property.scala`.

### Mock property data

`PropertyData.scala` contains a collection of mock properties used to simulate search results.

The data is currently held in memory rather than retrieved from an external service.

The lookup method currently used by the controller is:

`PropertyData.getPropertyById(postcode)`

**Note:** This method name should be reviewed. The controller passes a postcode and expects a collection of matching properties. A name such as `getPropertiesByPostcode` would describe that operation more accurately.

### Search Results controller

`SearchResultsController.scala`:
- Retrieves the previously submitted postcode from `UserAnswers`.
- Uses `PropertyData` to retrieve matching properties.
- Passes the results and journey mode to `SearchResultsView`.

### Search Results view

The view uses a GOV.UK Design System table rather than a simple list.

The table displays:

| Column | Description |
|---|---|
| Address | Property address |
| Council Tax band | The property's Council Tax band |
| Local authority | The responsible local authority |

The view also handles the situation where no properties are found.

### Navigation

The `Navigator.scala` normal route map has been updated so that submitting Page 1 takes the user to Page 2.

The relevant navigation case is:

`PropertySearchPage => _ => routes.SearchResultsController.onPageLoad()`

### Important routing detail

The Search Results route uses a default mode:

`GET /searchResults controllers.SearchResultsController.onPageLoad(mode: Mode = NormalMode)`

The default is important because Play's generated reverse route uses a parameterless `onPageLoad()` call.

We initially tried passing `NormalMode` explicitly, but Play generated a reverse route without parameters. Removing the default caused a missing `QueryStringBindable` error because `Mode` is not automatically supported as a query-string parameter.

The current default-mode approach compiles successfully.

---

## Page 3 – Property Details

**Status: In progress**

### Purpose

Displays the details of the property selected by the user on Page 2.

The page will show:
- Property address
- Council Tax band
- Effective date
- Local council
- Court code

The page will use the GOV.UK Design System Summary List component.

### Work in progress

The controller and route are the current focus.

Planned controller:

`app/controllers/PropertyDetailsController.scala`

Planned route:

`GET /propertyDetails/:id controllers.PropertyDetailsController.onPageLoad(id: String, mode: Mode = NormalMode)`

The controller will retrieve the property using its unique ID from `PropertyData`.

### Remaining Page 3 work

1. Confirm the existing `Property.scala` field names.
2. Complete and compile `PropertyDetailsController`.
3. Create `PropertyDetailsView.scala.html`.
4. Build the GOV.UK Summary List.
5. Add the property-selection action.
6. Save the selected property address and Council Tax band to `UserAnswers` and MongoDB.
7. Add the relevant page object and form provider if required by the selection implementation.
8. Add messages and unit tests.
9. Update navigation to Page 4.

---

## Current application architecture

The project follows the standard HMRC frontend structure:

| Component | Responsibility |
|---|---|
| Controllers | Handle page loading and form submissions |
| Forms | Validate submitted user input |
| Pages | Define the `UserAnswers` storage paths |
| Models | Represent application data |
| Views | Render the Twirl HTML pages |
| Navigator | Determine the next journey page |
| SessionRepository | Persist user answers to MongoDB |
| Messages | Store user-facing text and validation messages |
| Routes | Map URLs to controller actions |

### User answers and persistence

The application uses `UserAnswers` to store answers throughout the journey.

`SessionRepository` persists these answers to MongoDB.

This allows answers to be retrieved when navigating backwards and forwards through the journey.

---

## Development and testing status

- Page 1 postcode form implemented.
- UK postcode validation implemented.
- Page 1 answer persistence working.
- Page 1 prefill/back-navigation behaviour working.
- Page 2 mock property data implemented.
- Page 2 results table implemented.
- Page 2 navigation configured.
- Application successfully compiles after the latest routing changes.
- Earlier `sbt test` run completed successfully before Page 2 development.

Some compiler configuration warnings remain, including warnings relating to `-Wconf` and repeated compiler flags. These have not prevented compilation.

---

## Next development milestone

**Complete Page 3 – Property Details.**

The immediate next task is to confirm the existing `Property.scala` model so the controller and Summary List view use the correct field names.

After Page 3, development will continue with:

4. Responsible for Council Tax?
5. No Permission
6. Check Your Answers

The final objective is a complete working Council Tax journey with validation, MongoDB persistence, navigation, change-answer functionality and automated tests.