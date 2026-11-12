# Check Council Tax

A Scala Play Framework application built using the HMRC Frontend template. The application demonstrates a Council Tax checking journey, allowing a user to search for a property, review its Council Tax details, confirm their responsibility and check their answers.

The project uses mock property data and MongoDB session persistence to demonstrate how a multi-page HMRC-style frontend application works.

## Contents

- [Overview](#overview)
- [Technology Stack](#technology-stack)
- [Application Journey](#application-journey)
- [Project Structure](#project-structure)
- [Property Data](#property-data)
- [MongoDB Persistence](#mongodb-persistence)
- [Navigation and Routing](#navigation-and-routing)
- [Validation](#validation)
- [Running the Application](#running-the-application)
- [Testing](#testing)
- [Future Improvements](#future-improvements)

## Overview

The purpose of this project is to build a multi-page Council Tax checking service using the conventions and architecture of an HMRC frontend application.

The application guides the user through the following process:

1. Enter a property postcode.
2. View matching properties.
3. Select a property and review its Council Tax details.
4. Confirm whether they are responsible for paying Council Tax.
5. Display a relevant message if they are not responsible.
6. Review their answers and confirm completion.

The application demonstrates:

- Scala and Play Framework development.
- HMRC frontend architecture and GOV.UK components.
- Form validation and error handling.
- Multi-page navigation and conditional journeys.
- Session management using MongoDB.
- Unit testing of controllers and application functionality.

## Technology Stack

| Technology | Purpose |
|---|---|
| Scala | Main programming language |
| Play Framework | Web application framework |
| HMRC Frontend | HMRC application scaffolding and conventions |
| GOV.UK Design System | User interface components |
| Twirl | Server-side HTML templates |
| MongoDB | Session and answer persistence |
| SBT | Build and dependency management |
| ScalaTest / Mockito | Automated testing |

## Application Journey

### Page 1 — Property Search

The user enters the postcode of the property they want to check.

Features:
- Postcode input field.
- Required-field validation.
- UK postcode format validation.
- Maximum length validation.
- Stores the postcode in the user's answers.
- Saves the answer through the session repository.

Route:

`GET /propertySearch`

### Page 2 — Search Results

The application searches the mock property dataset using the postcode entered on Page 1.

Features:
- Displays matching properties.
- Shows property address, Council Tax band and local authority.
- Allows the user to select a property.
- Handles postcodes with no matching properties.

Route:

`GET /searchResults`

### Page 3 — Property Details

Displays the details of the selected property.

The information includes:
- Property address.
- Council Tax band.
- Effective date.
- Local authority.
- Court code.

The selected property address, Council Tax band and local authority reference are stored in the user's answers.

The `localAuthorityRef` is used to identify individual property records, allowing multiple properties at the same postcode to be distinguished.

Route:

`GET /propertyDetails/:localAuthorityRef`

### Page 4 — Council Tax Responsibility

The user is asked:

**Are you responsible for paying Council Tax for this property?**

Available answers:

- Yes
- No
- I am acting on behalf of someone else

The answer is validated and saved to the user's answers.

Navigation depends on the selected answer:

| Answer | Destination |
|---|---|
| Yes | Check Your Answers |
| No | No Permission |
| Acting on behalf of someone else | Check Your Answers |

Route:

`GET /councilTaxResponsibility`

### Page 5 — No Permission

Displays a static message explaining that the user cannot continue if they are not responsible for paying Council Tax and are not acting on behalf of someone who is.

The user can navigate back to the selected property's details.

Route:

`GET /noPermission`

### Page 6 — Check Your Answers

Displays a summary of the information entered throughout the journey.

The summary includes:
- Selected property address and Council Tax band.
- Council Tax responsibility answer.

Each answer has a Change link, allowing the user to return to the relevant page and amend their answer.

The user can then select Confirm.

Route:

`GET /check-your-answers`

### Confirmation Page

After confirming their answers, the user is taken to a simple confirmation content page.

This is the end of the exercise. No external submission or reference-number generation is required.

Route:

`GET /confirmation`

## Project Structure

The application follows the standard HMRC frontend project structure.

Important directories:

```text
app/
├── controllers/
│   ├── PropertySearchController.scala
│   ├── SearchResultsController.scala
│   ├── PropertyDetailsController.scala
│   ├── CouncilTaxResponsibilityController.scala
│   ├── NoPermissionController.scala
│   ├── CheckYourAnswersController.scala
│   └── ConfirmationController.scala
│
├── forms/
│   ├── PropertySearchFormProvider.scala
│   └── CouncilTaxResponsibilityFormProvider.scala
│
├── models/
│   ├── Property.scala
│   ├── PropertyData.scala
│   ├── SelectedProperty.scala
│   └── CouncilTaxResponsibility.scala
│
├── navigation/
│   └── Navigator.scala
│
├── pages/
│   ├── PropertySearchPage.scala
│   ├── SelectedPropertyPage.scala
│   └── CouncilTaxResponsibilityPage.scala
│
├── repositories/
│   └── SessionRepository.scala
│
└── views/
    ├── PropertySearchView.scala.html
    ├── SearchResultsView.scala.html
    ├── PropertyDetailsView.scala.html
    ├── CouncilTaxResponsibilityView.scala.html
    ├── NoPermissionView.scala.html
    ├── CheckYourAnswersView.scala.html
    └── ConfirmationView.scala.html
```

The project also uses shared HMRC frontend components, including:

- `UserAnswers` for managing journey answers.
- `QuestionPage` for identifying individual answers.
- `Navigator` for controlling journey navigation.
- `SessionRepository` for persisting user sessions.
- `DataRetrievalAction` and `DataRequiredAction` for retrieving and checking journey data.

## Property Data

The application currently uses a mock property dataset rather than a live Council Tax property service.

The data is held in:

`app/models/PropertyData.scala`

The `Property` model contains:

| Field | Description |
|---|---|
| id | Internal mock record identifier |
| address | Property address |
| localAuthority | Local authority name |
| localAuthorityRef | Property reference used to identify the record |
| councilTaxBand | Council Tax band |
| improvementIndicator | Property improvement indicator |
| effectiveDate | Effective date of the Council Tax band |
| mixedUseIndicator | Indicates mixed-use property |
| courtCode | Court code |

The mock dataset includes properties in Westminster and Lewisham, including multiple flats sharing the same postcode.

Two lookup methods are provided:

- `getPropertiesByPostcode(postcode)` — returns matching properties.
- `getPropertyByLocalAuthorityRef(ref)` — returns an individual property using its local authority reference.

## MongoDB Persistence

MongoDB is used to persist the user's journey answers through the HMRC session repository.

The database configuration is held in:

`conf/application.conf`

The MongoDB configuration uses:

```hocon
mongodb {
  uri = "mongodb://localhost:27017/"${appName}
  timeToLiveInSeconds = 900
}
```

The application stores session data in the `user-answers` collection.

Journey answers are stored within the user's `data` document.

Example structure:

```json
{
  "_id": "local-test-user",
  "data": {
    "propertySearch": "se8 4pb",
    "selectedProperty": {
      "address": "FLAT 2, 9-12 DEPTFORD BROADWAY, LONDON, SE8 4PB",
      "councilTaxBand": "C",
      "localAuthorityRef": "02512000900027"
    },
    "councilTaxResponsibility": "yes"
  }
}
```

The actual document also contains session metadata, including the last updated timestamp.

The application uses the `UserAnswers` model to manage the data and the `SessionRepository` to save changes.

## Navigation and Routing

Navigation is managed centrally through:

`app/navigation/Navigator.scala`

The navigator uses the current page, journey mode and saved user answers to determine the next destination.

Two journey modes are supported:

- `NormalMode` — standard forward navigation.
- `CheckMode` — navigation when changing an existing answer from Check Your Answers.

The responsibility question uses conditional navigation to determine whether the user proceeds to Check Your Answers or the No Permission page.

## Validation

Form validation is implemented using form providers.

The postcode form validates:

- Whether a postcode has been entered.
- Whether it matches the expected UK postcode format.
- Whether it exceeds the maximum permitted length.

The responsibility question requires the user to select one of the available answers.

Validation errors are displayed through the GOV.UK error summary and form components.

## Running the Application

### Prerequisites

You will need:

- Java (a version supported by the project).
- Scala and SBT.
- MongoDB running locally.
- MongoDB Compass (optional, for viewing stored session data).

### Start MongoDB

Make sure your local MongoDB instance is running and accessible at:

`mongodb://localhost:27017`

### Start the application

From the project root directory, run:

```bash
sbt run
```

The application will start using the configured Play development server.

Open:

`http://localhost:1234`

Use the port displayed by SBT if your local configuration uses a different port.

## Testing

The project uses ScalaTest and Mockito for automated testing.

Run the complete test suite with:

```bash
sbt test
```

To run an individual controller test, use:

```bash
sbt "testOnly controllers.PropertyDetailsControllerSpec"
```

Testing should cover:

- Valid and invalid postcode submissions.
- Property search results.
- Property details retrieval.
- Selected property persistence.
- Council Tax responsibility validation.
- Conditional navigation for each responsibility answer.
- Check Your Answers content.
- Change links and CheckMode navigation.
- Confirmation page access.

Manual end-to-end testing should also verify the complete journey and inspect the saved answers in MongoDB Compass.

## Future Improvements

The core frontend journey is implemented using mock data. Potential future improvements include:

- Connecting to a real property or Council Tax API.
- Replacing mock property records with live data.
- Improving automated end-to-end test coverage.
- Reviewing accessibility and GOV.UK Design System compliance.
- Refining page styling and content.
- Adding further error handling and journey recovery scenarios.

## Project Status

**Core frontend journey implemented.**

The application demonstrates a complete multi-page Council Tax checking journey, including postcode validation, property selection, conditional navigation, answer persistence, a Check Your Answers page and a confirmation page.

The remaining work is to complete end-to-end testing, verify all navigation paths and refine the application where necessary.

