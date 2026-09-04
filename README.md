# GitHub Repository Searcher

This is a Spring Boot application that searches GitHub repositories using the GitHub REST API and stores the repository details in PostgreSQL.

The application also provides APIs to get the repositories saved in the database with filters such as language, minimum stars, and sorting.

## Technologies Used

- Java 17
- Spring Boot
- Spring Data JPA
- PostgreSQL
- GitHub REST API
- Maven
- JUnit 5
- Mockito

## Features

- Search repositories from GitHub
- Filter repositories by programming language
- Sort repositories by stars, forks, or updated date
- Save GitHub repository details in PostgreSQL
- Update an existing repository instead of creating a duplicate
- Get saved repositories from the database
- Filter saved repositories by language
- Filter saved repositories by minimum number of stars
- Basic validation and error handling
- Unit tests for the service layer

## Project Structure

```text
src/main/java/com/github/githubsearcher
│
├── client
│   └── GithubApiClient.java
│
├── controller
│   └── GithubRepositoryController.java
│
├── dto
│   ├── GithubRepositoryResponse.java
│   ├── GithubSearchRequest.java
│   └── GithubSearchResponse.java
│
├── entity
│   └── GithubRepository.java
│
├── exception
│   ├── GithubApiException.java
│   └── GlobalExceptionHandler.java
│
├── repository
│   └── GithubRepositoryRepository.java
│
└── service
    └── GithubRepositoryService.java