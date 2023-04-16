# Getting Started
This project is built using Gradle, Java 17 and Spring Boot. 
the package `com.deBijenkorf.imageresizeservice.controller` 
that exposes three endpoints for image resizing.

### Show Image Endpoint
`Endpoint: GET /image/show/{predefined-type-name}/{dummy-seo-name}/?reference=<unique-original-image-filename>`

This endpoint returns a resized image with a predefined type name .

#### Query parameters:

`reference: The original filename of the image.`

### Flush Image Endpoint
`Endpoint: DELETE /image/flush/{predefined-image-type}/?reference=<unique-original-image-filename>`

This endpoint removes a resized image with a predefined type name.

#### Query parameters:

`reference: The original filename of the image.`

### Dependencies

* `Spring Boot Starter Web: for building web applications using Spring MVC.`
* `Lombok: for reducing boilerplate code.`
* `Java 17`

### Building and Running the Application
To build and run this application, follow these steps:

* Run the following command to build the project:
   - ` gradle build `
   - ` gradle bootRun `

### Testing and Coverage

This project includes unit tests for all functionality of the Image Resizer Service. 
The tests cover all main use cases and have been designed to ensure the reliability of the system.

### Unit and Integration Tests
The unit and Integration tests have been implemented using JUnit and Mockito. 
These tests have been designed to validate the functionality of each component of the service in isolation, ensuring that they behave as expected.
The unit tests can be found in the `src/test` directory.



