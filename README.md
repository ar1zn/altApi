# altApi
AltAPI CLI

AltAPI CLI is a command-line tool for interacting with the AltLinux API. It allows you to retrieve information about packages in various branches of the AltLinux project and compare them.
Usage

    Compile and run the application by entering the following command in your terminal:

java -jar altapi.jar

    The application will prompt you to choose two packages from the list provided in the console. To select packages, enter their numbers separated by commas.

    After selecting the packages, the application will make a request to the AltLinux API, retrieve information about the packages, and compare their versions. The result will be displayed in JSON format.

Dependencies

The application requires the following dependencies to function:

    Jakarta JSON API: Used for processing JSON data from AltLinux API responses.
    Jakarta RESTful Web Services (JAX-RS): Used for making HTTP requests to the AltLinux API.

Contribution

You can contribute to this project by making suggestions for improvement, creating pull requests, or opening issues in this repository.
License

This project is licensed under the terms of the MIT License. For more information, see the LICENSE file.
