package com.github.githubsearcher.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.githubsearcher.exception.GithubApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class GithubApiClient {

    private static final Logger log =
            LoggerFactory.getLogger(GithubApiClient.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String githubToken;

    public GithubApiClient(
            RestClient githubRestClient,
            ObjectMapper objectMapper,
            @Value("${github.token:}") String githubToken) {

        this.restClient = githubRestClient;
        this.objectMapper = objectMapper;
        this.githubToken = githubToken;
    }

    public List<GithubRepositoryData> searchRepositories(
            String query,
            String language,
            String sort) {

        // Search GitHub using the repository name/query.
        // Language filtering is done in Java after receiving the results.
        String uri = UriComponentsBuilder
                .fromPath("/search/repositories")
                .queryParam("q", query)
                .queryParam("sort", sort)
                .queryParam("order", "desc")
                .queryParam("per_page", 100)
                .toUriString();

        log.debug("Calling GitHub search API: {}", uri);

        try {
            RestClient.RequestHeadersSpec<?> request = restClient
                    .get()
                    .uri(uri)
                    .header("Accept", "application/vnd.github+json")
                    .header("X-GitHub-Api-Version", "2022-11-28");

            if (githubToken != null && !githubToken.isBlank()) {
                request.header("Authorization", "Bearer " + githubToken);
            }

            String response = request
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new GithubApiException(
                                "GitHub API returned status: "
                                        + res.getStatusCode()
                        );
                    })
                    .body(String.class);

            List<GithubRepositoryData> repositories =
                    parseRepositories(response);

            // Filter by language in our application
            if (language != null && !language.isBlank()) {
                repositories = repositories.stream()
                        .filter(repo ->
                                repo.language() != null
                                        && language.equalsIgnoreCase(
                                        repo.language()))
                        .toList();
            }

            return repositories;

        } catch (GithubApiException e) {
            throw e;

        } catch (Exception e) {
            log.error("Failed to communicate with GitHub API", e);

            throw new GithubApiException(
                    "Unable to communicate with GitHub API"
            );
        }
    }

    private List<GithubRepositoryData> parseRepositories(String response) {

        try {
            JsonNode root = objectMapper.readTree(response);

            List<GithubRepositoryData> repositories = new ArrayList<>();

            for (JsonNode item : root.path("items")) {

                Long id = item.path("id").asLong();

                String name = item.path("name").asText(null);

                String description =
                        item.path("description").isNull()
                                ? null
                                : item.path("description").asText();

                String owner =
                        item.path("owner")
                                .path("login")
                                .asText(null);

                String language =
                        item.path("language").isNull()
                                ? null
                                : item.path("language").asText();

                Integer stars =
                        item.path("stargazers_count").asInt();

                Integer forks =
                        item.path("forks_count").asInt();

                String updatedAt =
                        item.path("updated_at").asText(null);

                Instant lastUpdated =
                        updatedAt != null
                                ? Instant.parse(updatedAt)
                                : null;

                repositories.add(
                        new GithubRepositoryData(
                                id,
                                name,
                                description,
                                owner,
                                language,
                                stars,
                                forks,
                                lastUpdated
                        )
                );
            }

            return repositories;

        } catch (Exception e) {
            log.error("Failed to parse GitHub API response", e);

            throw new GithubApiException(
                    "Unable to process GitHub API response"
            );
        }
    }

    public record GithubRepositoryData(
            Long id,
            String name,
            String description,
            String owner,
            String language,
            Integer stars,
            Integer forks,
            Instant lastUpdated
    ) {
    }
}