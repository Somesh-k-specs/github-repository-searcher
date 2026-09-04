package com.github.githubsearcher.service;

import com.github.githubsearcher.client.GithubApiClient;
import com.github.githubsearcher.client.GithubApiClient.GithubRepositoryData;
import com.github.githubsearcher.dto.GithubRepositoryResponse;
import com.github.githubsearcher.dto.GithubSearchResponse;
import com.github.githubsearcher.entity.GithubRepository;
import com.github.githubsearcher.repository.GithubRepositoryRepository;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class GithubRepositoryService {

    private final GithubApiClient githubApiClient;
    private final GithubRepositoryRepository repository;

    public GithubRepositoryService(
            GithubApiClient githubApiClient,
            GithubRepositoryRepository repository) {

        this.githubApiClient = githubApiClient;
        this.repository = repository;
    }

    public GithubSearchResponse searchAndSave(
            String query,
            String language,
            String sort) {

        List<GithubRepositoryData> githubRepositories =
                githubApiClient.searchRepositories(
                        query,
                        language,
                        sort
                );

        for (GithubRepositoryData data : githubRepositories) {
            saveOrUpdate(data);
        }

        List<GithubRepositoryResponse> responses =
                githubRepositories.stream()
                        .map(this::toResponse)
                        .toList();

        String message = responses.isEmpty()
                ? "No repositories found"
                : "Repositories fetched and saved successfully";

        return new GithubSearchResponse(message, responses);
    }

    private void saveOrUpdate(GithubRepositoryData data) {

        GithubRepository repositoryEntity =
                repository.findById(data.id())
                        .orElse(new GithubRepository());

        repositoryEntity.setId(data.id());
        repositoryEntity.setName(data.name());
        repositoryEntity.setDescription(data.description());
        repositoryEntity.setOwner(data.owner());
        repositoryEntity.setLanguage(data.language());
        repositoryEntity.setStars(data.stars());
        repositoryEntity.setForks(data.forks());
        repositoryEntity.setLastUpdated(data.lastUpdated());

        repository.save(repositoryEntity);
    }

    public List<GithubRepositoryResponse> getRepositories(
            String language,
            Integer minStars,
            String sort) {

        List<GithubRepository> repositories;

        if (language != null && !language.isBlank()
                && minStars != null) {

            repositories =
                    repository
                            .findByLanguageIgnoreCaseAndStarsGreaterThanEqual(
                                    language,
                                    minStars
                            );

        } else if (language != null && !language.isBlank()) {

            repositories =
                    repository.findByLanguageIgnoreCase(language);

        } else if (minStars != null) {

            repositories =
                    repository.findByStarsGreaterThanEqual(minStars);

        } else {

            repositories = repository.findAll();
        }

        // Create a mutable list before sorting
        repositories = new ArrayList<>(repositories);

        sortRepositories(repositories, sort);

        return repositories.stream()
                .map(this::toResponse)
                .toList();
    }

    private void sortRepositories(
            List<GithubRepository> repositories,
            String sort) {

        Comparator<GithubRepository> comparator;

        switch (sort) {

            case "forks" ->
                    comparator = Comparator.comparing(
                            GithubRepository::getForks,
                            Comparator.nullsLast(
                                    Comparator.reverseOrder()
                            )
                    );

            case "updated" ->
                    comparator = Comparator.comparing(
                            GithubRepository::getLastUpdated,
                            Comparator.nullsLast(
                                    Comparator.reverseOrder()
                            )
                    );

            case "stars" ->
                    comparator = Comparator.comparing(
                            GithubRepository::getStars,
                            Comparator.nullsLast(
                                    Comparator.reverseOrder()
                            )
                    );

            default ->
                    throw new IllegalArgumentException(
                            "Invalid sort value. Allowed values: stars, forks, updated"
                    );
        }

        repositories.sort(comparator);
    }

    private GithubRepositoryResponse toResponse(
            GithubRepository repository) {

        return new GithubRepositoryResponse(
                repository.getId(),
                repository.getName(),
                repository.getDescription(),
                repository.getOwner(),
                repository.getLanguage(),
                repository.getStars(),
                repository.getForks(),
                repository.getLastUpdated()
        );
    }

    private GithubRepositoryResponse toResponse(
            GithubRepositoryData data) {

        return new GithubRepositoryResponse(
                data.id(),
                data.name(),
                data.description(),
                data.owner(),
                data.language(),
                data.stars(),
                data.forks(),
                data.lastUpdated()
        );
    }
}