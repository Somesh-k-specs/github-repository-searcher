# GitHub Repository Searcher

A Spring Boot service that searches GitHub repositories via the GitHub REST API, saves the results in PostgreSQL, and lets you query what's been saved with a few filters. Built for the "GitHub Repository Searcher" backend assignment.

## Stack

- Java 17
- Spring Boot (Web, Data JPA, Validation)
- PostgreSQL
- GitHub REST Search API
- JUnit 5 + Mockito

## Setup

You'll need Postgres running locally. A GitHub token is optional but recommended (unauthenticated calls to GitHub's search API are capped at 60 requests/hour; a personal access token bumps that to 5,000/hour).

1. Create a database:
   ```sql
   CREATE DATABASE github_searcher;
   ```

2. Set the following environment variables (copy `.env.example` for reference, or set them in your IDE's run configuration):

   ```
   DB_URL=jdbc:postgresql://localhost:5432/github_searcher
   DB_USERNAME=postgres
   DB_PASSWORD=<your postgres password>
   GITHUB_TOKEN=<optional — a GitHub personal access token, no scopes needed>
   ```

   `DB_PASSWORD` is required — the app won't start without it. `GITHUB_TOKEN` can be left unset; the app will just call GitHub's API unauthenticated.

3. Run it:
   ```bash
   ./mvnw spring-boot:run
   ```

   Tables are created automatically on startup (`ddl-auto=update`).

4. Run tests:
   ```bash
   ./mvnw test
   ```

## API

### `POST /api/github/search`

Searches GitHub, saves (or updates) every result in the database, and returns them.

Request:
```json
{
  "query": "spring boot",
  "language": "Java",
  "sort": "stars"
}
```

- `query` — required, matched against repo name/description (whatever GitHub's search endpoint matches on).
- `language` — optional. Passed straight into the GitHub search query (`language:Java`) rather than fetched-then-filtered, so it doesn't silently drop matches.
- `sort` — optional, one of `stars` / `forks` / `updated`. Defaults to `stars` if omitted.

Response:
```json
{
  "message": "Repositories fetched and saved successfully",
  "repositories": [
    {
      "id": 123456,
      "name": "spring-boot-example",
      "description": "An example repository for Spring Boot",
      "owner": "user123",
      "language": "Java",
      "stars": 450,
      "forks": 120,
      "lastUpdated": "2024-01-01T12:00:00Z"
    }
  ]
}
```

If GitHub returns zero matches, `message` becomes `"No repositories found"` and `repositories` is an empty array — not an error.

### `GET /api/github/repositories`

Returns repositories already saved in the database (no call to GitHub).

Query params (all optional):
- `language` — filter by language, case-insensitive.
- `minStars` — only repos with at least this many stars.
- `sort` — `stars` / `forks` / `updated`, defaults to `stars`.

Example:
```
GET /api/github/repositories?language=Java&minStars=100&sort=stars
```

Response:
```json
{
  "repositories": [
    {
      "id": 123456,
      "name": "spring-boot-example",
      "description": "An example repository for Spring Boot",
      "owner": "user123",
      "language": "Java",
      "stars": 450,
      "forks": 120,
      "lastUpdated": "2024-01-01T12:00:00Z"
    }
  ]
}
```

## Error handling

| Situation | Response |
|---|---|
| Blank/missing `query` in search request | `400`, field-level validation message |
| Invalid `sort` value on the GET endpoint | `400` with an explicit "allowed values" message |
| GitHub API returns an error status (including rate limiting) | `502`, with the upstream status in the message |
| Unexpected failure | `500`, generic message |

## Design notes

- **Upsert, not insert-or-fail.** `GithubRepositoryService` looks up each incoming repo by its GitHub ID first; if it exists, the existing row is updated in place rather than duplicated. This directly covers the "don't duplicate on re-search" requirement.
- **Language filtering happens in the GitHub query, not after the fact.** Early on this was done by fetching a page of unfiltered results and filtering by language in Java afterwards — which meant a valid match could get silently dropped if it fell outside that page. Folding `language:X` into the actual GitHub search query fixes that.
- **`GithubApiClient` is isolated from the rest of the app.** It's the only class that knows about GitHub's response shape; everything downstream works with the plain `GithubRepositoryData` record, so if GitHub's API shape changes, the blast radius is one file.

## Known limitations

- No Swagger/OpenAPI UI — tested via Postman. Can add springdoc-openapi if needed.
- No pagination on the GET endpoint — returns everything matching the filters in one response.
- Only public repository search is covered (matches the assignment scope); no auth-scoped/private repo search.