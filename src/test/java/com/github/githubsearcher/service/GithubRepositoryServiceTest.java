package com.github.githubsearcher.service;


import com.github.githubsearcher.client.GithubApiClient;
import com.github.githubsearcher.client.GithubApiClient.GithubRepositoryData;
import com.github.githubsearcher.dto.GithubRepositoryResponse;
import com.github.githubsearcher.dto.GithubSearchResponse;
import com.github.githubsearcher.entity.GithubRepository;
import com.github.githubsearcher.repository.GithubRepositoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GithubRepositoryServiceTest {

    @Mock
    private GithubApiClient githubApiClient;

    @Mock
    private GithubRepositoryRepository repository;

    @InjectMocks
    private GithubRepositoryService service;

    @Test
    void shouldSearchAndSaveRepositories() {

        GithubRepositoryData data = new GithubRepositoryData(
                1L,
                "spring-boot",
                "Spring Boot repository",
                "spring-projects",
                "Java",
                80000,
                40000,
                Instant.now()
        );

        when(githubApiClient.searchRepositories(
                "spring",
                "Java",
                "stars"
        )).thenReturn(List.of(data));

        when(repository.findById(1L))
                .thenReturn(Optional.empty());

        GithubSearchResponse response =
                service.searchAndSave(
                        "spring",
                        "Java",
                        "stars"
                );

        assertEquals(1, response.getRepositories().size());
        assertEquals("spring-boot",
                response.getRepositories().get(0).getName());

        verify(repository).save(any(GithubRepository.class));
    }

    @Test
    void shouldReturnNoRepositoriesWhenGithubReturnsEmptyList() {

        when(githubApiClient.searchRepositories(
                "unknown",
                "Python",
                "stars"
        )).thenReturn(List.of());

        GithubSearchResponse response =
                service.searchAndSave(
                        "unknown",
                        "Python",
                        "stars"
                );

        assertEquals(
                "No repositories found",
                response.getMessage()
        );

        assertTrue(response.getRepositories().isEmpty());

        verify(repository, never())
                .save(any(GithubRepository.class));
    }

    @Test
    void shouldFilterRepositoriesByLanguage() {

        GithubRepository javaRepo = createRepository(
                1L,
                "spring-boot",
                "Java",
                80000,
                40000
        );

        GithubRepository pythonRepo = createRepository(
                2L,
                "django",
                "Python",
                70000,
                30000
        );

        when(repository.findByLanguageIgnoreCase("Python"))
                .thenReturn(List.of(pythonRepo));

        List<GithubRepositoryResponse> response =
                service.getRepositories(
                        "Python",
                        null,
                        "stars"
                );

        assertEquals(1, response.size());
        assertEquals("Python",
                response.get(0).getLanguage());

        verify(repository)
                .findByLanguageIgnoreCase("Python");
    }

    @Test
    void shouldFilterRepositoriesByMinimumStars() {

        GithubRepository repo =
                createRepository(
                        1L,
                        "spring-boot",
                        "Java",
                        80000,
                        40000
                );

        when(repository.findByStarsGreaterThanEqual(50000))
                .thenReturn(List.of(repo));

        List<GithubRepositoryResponse> response =
                service.getRepositories(
                        null,
                        50000,
                        "stars"
                );

        assertEquals(1, response.size());
        assertTrue(response.get(0).getStars() >= 50000);

        verify(repository)
                .findByStarsGreaterThanEqual(50000);
    }

    @Test
    void shouldRejectInvalidSortValue() {

        when(repository.findAll())
                .thenReturn(List.of());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.getRepositories(
                                null,
                                null,
                                "invalid"
                        )
                );

        assertEquals(
                "Invalid sort value. Allowed values: stars, forks, updated",
                exception.getMessage()
        );
    }

    private GithubRepository createRepository(
            Long id,
            String name,
            String language,
            Integer stars,
            Integer forks) {

        GithubRepository repository =
                new GithubRepository();

        repository.setId(id);
        repository.setName(name);
        repository.setLanguage(language);
        repository.setStars(stars);
        repository.setForks(forks);
        repository.setOwner("test-owner");
        repository.setDescription("Test repository");
        repository.setLastUpdated(Instant.now());

        return repository;
    }
}
