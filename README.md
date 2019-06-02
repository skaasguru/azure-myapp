# Azure MyApp

This repository has the source code for the Azure Architecture Training.

## Application Functionalities
* Simple Authentication for demonstrating session and statelessness
* “Photo gallery” functionality for emphasizing right storage for user uploads
* “My contacts” functionality for using database services

The initial version of this application is built to be a traditional monolithic application. Later, this is modified to use Azure services using APIs and SDKs.

The cloud best practices will be implemented in this application to make it cloud native. The architectural best practices are implemented in the process of converting this app to cloud native.

The broader flow is given below.
* Deploying a monolith app on a single VM and discussing its limitations on performance, scaling, etc.
* Splitting the monolith to a set of microservices to separate the responsibilities. Deploying each service on a docker container.

