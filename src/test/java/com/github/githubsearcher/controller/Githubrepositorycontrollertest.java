package com.github.githubsearcher.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.githubsearcher.dto.GithubRepositoryResponse;
import com.github.githubsearcher.dto.GithubSearchRequest;
import com.github.githubsearcher.dto.GithubSearchResponse;
import com.github.githubsearcher.service.GithubRepositoryService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GithubRepositoryController.class)
class GithubRepositoryControllerTest{

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GithubRepositoryService service;

    @Test
    void shouldReturnBadRequestWhenQueryIsBlank() throws Exception {

        GithubSearchRequest request = new GithubSearchRequest();
        request.setQuery("");
        request.setLanguage("Java");
        request.setSort("stars");

        mockMvc.perform(
                post("/api/github/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isBadRequest());
    }

    @Test
    void shouldSearchAndReturnSavedRepositories() throws Exception {

        GithubRepositoryResponse repo = new GithubRepositoryResponse(
                123456L,
                "spring-boot-example",
                "An example repository for Spring Boot",
                "user123",
                "Java",
                450,
                120,
                Instant.parse("2024-01-01T12:00:00Z")
        );

        GithubSearchResponse response = new GithubSearchResponse(
                "Repositories fetched and saved successfully",
                List.of(repo)
        );

        when(service.searchAndSave("spring boot", "Java", "stars"))
                .thenReturn(response);

        String requestBody = """
                {
                  "query": "spring boot",
                  "language": "Java",
                  "sort": "stars"
                }
                """;

        mockMvc.perform(
                post("/api/github/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message")
                .value("Repositories fetched and saved successfully"))
        .andExpect(jsonPath("$.repositories[0].name")
                .value("spring-boot-example"));
    }

    @Test
    void shouldDefaultSortToStarsWhenNotProvidedInSearchRequest() throws Exception {

        when(service.searchAndSave("spring", null, "stars"))
                .thenReturn(new GithubSearchResponse("No repositories found", List.of()));

        String requestBody = """
                { "query": "spring" }
                """;

        mockMvc.perform(
                post("/api/github/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        ).andExpect(status().isOk());

        verify(service).searchAndSave("spring", null, "stars");
    }

    @Test
    void shouldReturnRepositoriesWrappedInObjectFromGetEndpoint() throws Exception {

        GithubRepositoryResponse repo = new GithubRepositoryResponse(
                123456L,
                "spring-boot-example",
                "An example repository for Spring Boot",
                "user123",
                "Java",
                450,
                120,
                Instant.parse("2024-01-01T12:00:00Z")
        );

        when(service.getRepositories("Java", 100, "stars"))
                .thenReturn(List.of(repo));

        mockMvc.perform(
                get("/api/github/repositories")
                        .param("language", "Java")
                        .param("minStars", "100")
                        .param("sort", "stars")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.repositories").isArray())
        .andExpect(jsonPath("$.repositories[0].id").value(123456));
    }

    @Test
    void shouldReturnEmptyWrappedListWhenNoRepositoriesMatchFilters() throws Exception {

        when(service.getRepositories(null, null, "stars"))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/github/repositories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repositories").isArray())
                .andExpect(jsonPath("$.repositories.length()").value(0));
    }
}