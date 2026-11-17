# CheckCouncilTax – Page 3 Development Progress

## Page 3: Property Details

**Status: Functionally complete**

Page 3 of the Council Tax journey has been implemented. This page displays the details of the property selected by the user on the Search Results page and saves the selected property information to the user's MongoDB session.

---

## 1. Journey Overview

The user journey leading to Page 3 is:

1. The user enters a postcode on the Property Search page.
2. The application retrieves matching mock properties.
3. The Search Results page displays the matching properties.
4. The user selects a property.
5. The Property Details page displays the selected property's information.
6. The selected property address and Council Tax band are saved to the user's answers in MongoDB.

---

## 2. Property Information Displayed

The Property Details page displays the following information:

- Property address
- Council Tax band
- Effective date
- Local authority
- Court code

The information is presented using a GOV.UK summary list component.

---

## 3. Property Identification

Properties are identified using `localAuthorityRef`.

This reference is passed from the Search Results page to the Property Details controller.

The application uses:

`PropertyData.getPropertyByLocalAuthorityRef(localAuthorityRef)`

This retrieves the matching property from the mock property dataset.

Using `localAuthorityRef` allows the application to distinguish between multiple properties sharing the same postcode.

The journey does not rely on the property's internal `id` field.

---

## 4. Files Created or Updated

### Controllers

- `app/controllers/PropertyDetailsController.scala`
  - Retrieves the selected property.
  - Displays the Property Details page.
  - Saves the selected address and Council Tax band.
  - Returns `NotFound` if the property reference cannot be found.

- `app/controllers/SearchResultsController.scala`
  - Retrieves properties matching the submitted postcode.

### Views

- `app/views/PropertyDetailsView.scala.html`
  - Displays the selected property's information.
  - Includes navigation back to Search Results.

- `app/views/SearchResultsView.scala.html`
  - Displays matching properties.
  - Provides links to the Property Details page.

### Models

- `app/models/SelectedProperty.scala`
  - Stores the selected property address and Council Tax band.
  - Includes a JSON format for MongoDB serialisation.

- `app/models/Property.scala`
  - Defines the property data structure.

- `app/models/PropertyData.scala`
  - Contains the mock property dataset.
  - Provides postcode lookup and `localAuthorityRef` lookup methods.

### Pages

- `app/pages/SelectedPropertyPage.scala`
  - Defines the `QuestionPage` used to store the selected property in `UserAnswers`.

### Routes

- `conf/app.routes`
  - Defines the Property Details route using `localAuthorityRef`.
  - Supports the journey's normal and check-mode routing conventions where applicable.

### Tests

- `test/controllers/PropertyDetailsControllerSpec.scala`
  - Tests successful property retrieval.
  - Tests persistence of the selected address and Council Tax band.
  - Tests the `NotFound` response for an unknown property reference.

- `test/controllers/SearchResultsControllerSpec.scala`
  - Existing Search Results controller tests, with route and import corrections made during test compilation.

---

## 5. MongoDB Persistence

The selected property is saved using `SessionRepository`.

The controller updates the user's answers with:

`SelectedPropertyPage`

The saved data contains:

- `address`
- `councilTaxBand`

### Verified MongoDB document

The following structure was observed in MongoDB Compass in the `CheckCouncilTax` database, under the `user-answers` collection:

```json
{
  "_id": "local-test-user",
  "data": {
    "selectedProperty": {
      "address": "FLAT 2, 9-12 DEPTFORD BROADWAY, LONDON, SE8 4PB",
      "councilTaxBand": "C"
    },
    "propertySearch": "se8 4pb"
  }
}
```

This confirms that the selected property information is successfully persisted.

---

## 6. Testing

The following controller behaviours have been covered:

| Test | Expected result |
|---|---|
| Valid property reference | HTTP 200 OK |
| Selected property is saved | Address and Council Tax band are persisted |
| Unknown property reference | HTTP 404 Not Found |

The Page 3 controller specification was run using:

```bash
sbt "testOnly controllers.PropertyDetailsControllerSpec"
```

**Result: Tests passed.**

MongoDB persistence was also manually verified using MongoDB Compass.

---

## 7. Technical Concepts Learned

During Page 3 implementation, the following concepts were covered:

- Passing parameters between pages using Play routes.
- Using `localAuthorityRef` to identify individual properties.
- Retrieving data from a mock dataset.
- Creating a new model for selected property information.
- Using `QuestionPage` to store data in `UserAnswers`.
- JSON serialisation using Play JSON formats.
- Persisting updated answers through `SessionRepository`.
- Writing controller tests using ScalaTest and Mockito.
- Capturing repository arguments to verify saved data.
- Inspecting MongoDB documents using MongoDB Compass.

---

## 8. Current Project Progress

| Page | Description | Status |
|---|---|---|
| Page 1 | Property Search | Complete |
| Page 2 | Search Results | Complete |
| Page 3 | Property Details | Complete |
| Page 4 | Responsible for Council Tax? | Not started |
| Page 5 | Not responsible / no permission | Not started |
| Page 6 | Check your answers and confirmation | Not started |

---

## 9. Next Development Stage

The next stage is **Page 4 – Responsible for Council Tax?**

This page will ask the user whether they are:

- Responsible for Council Tax.
- Not responsible for Council Tax.
- Acting on behalf of someone else.

The answer will determine the next page in the journey:

- **No:** Navigate to Page 5.
- **Yes:** Navigate to Page 6.
- **Acting for someone:** Navigate to Page 6.

Page 4 will follow the same HMRC frontend structure used throughout the project, including:

- Controller
- Form provider
- Question page
- Navigator routing
- Twirl view
- Messages
- Controller and form tests

---

**End of Page 3 development checkpoint.**