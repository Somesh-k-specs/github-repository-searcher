package com.github.githubsearcher.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.githubsearcher.client.GithubApiClient.GithubRepositoryData;
import com.github.githubsearcher.exception.GithubApiException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GithubApiClientTest {

    private MockRestServiceServer mockServer;
    private GithubApiClient githubApiClient;

    @BeforeEach
    void setUp() {

        RestClient.Builder builder =
                RestClient.builder().baseUrl("https://api.github.com");

        mockServer = MockRestServiceServer.bindTo(builder).build();

        RestClient restClient = builder.build();

        githubApiClient = new GithubApiClient(restClient, new ObjectMapper(), "");
    }

    @Test
    void shouldParseRepositoriesFromGithubResponse() {

        String responseJson = """
                {
                  "items": [
                    {
                      "id": 123456,
                      "name": "spring-boot-example",
                      "description": "An example repository for Spring Boot",
                      "owner": { "login": "user123" },
                      "language": "Java",
                      "stargazers_count": 450,
                      "forks_count": 120,
                      "updated_at": "2024-01-01T12:00:00Z"
                    }
                  ]
                }
                """;

        mockServer.expect(requestTo(containsString("/search/repositories")))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        List<GithubRepositoryData> result =
                githubApiClient.searchRepositories("spring boot", null, "stars");

        assertEquals(1, result.size());
        assertEquals("spring-boot-example", result.get(0).name());
        assertEquals("user123", result.get(0).owner());
        assertEquals(450, result.get(0).stars());
        assertEquals(120, result.get(0).forks());

        mockServer.verify();
    }

    @Test
    void shouldPushLanguageFilterIntoGithubQueryInsteadOfFilteringAfterFetch() {

        String responseJson = """
                { "items": [] }
                """;

        mockServer.expect(requestTo(containsString("/search/repositories")))
                .andExpect(queryParam("q", containsString("language:Java")))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        githubApiClient.searchRepositories("spring", "Java", "stars");

        mockServer.verify();
    }

    @Test
    void shouldReturnEmptyListWhenGithubReturnsNoItems() {

        String responseJson = """
                { "items": [] }
                """;

        mockServer.expect(requestTo(containsString("/search/repositories")))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        List<GithubRepositoryData> result =
                githubApiClient.searchRepositories("zzzznonexistentqueryzzzz", null, "stars");

        assertTrue(result.isEmpty());

        mockServer.verify();
    }

    @Test
    void shouldThrowGithubApiExceptionWhenGithubReturnsErrorStatus() {

        mockServer.expect(requestTo(containsString("/search/repositories")))
                .andRespond(withStatus(HttpStatus.FORBIDDEN)
                        .body("{\"message\":\"API rate limit exceeded\"}")
                        .contentType(MediaType.APPLICATION_JSON));

        assertThrows(
                GithubApiException.class,
                () -> githubApiClient.searchRepositories("spring boot", null, "stars")
        );

        mockServer.verify();
    }

    @Test
    void shouldThrowGithubApiExceptionWhenResponseBodyIsMalformed() {

        mockServer.expect(requestTo(containsString("/search/repositories")))
                .andRespond(withSuccess("not valid json", MediaType.APPLICATION_JSON));

        assertThrows(
                GithubApiException.class,
                () -> githubApiClient.searchRepositories("spring boot", null, "stars")
        );

        mockServer.verify();
    }
}