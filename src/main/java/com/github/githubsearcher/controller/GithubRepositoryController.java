package com.github.githubsearcher.controller;



import com.github.githubsearcher.dto.GithubRepositoryResponse;
import com.github.githubsearcher.dto.GithubSearchRequest;
import com.github.githubsearcher.dto.GithubSearchResponse;
import com.github.githubsearcher.service.GithubRepositoryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/github")
public class GithubRepositoryController {

    private final GithubRepositoryService service;

    public GithubRepositoryController(
            GithubRepositoryService service) {
        this.service = service;
    }

    @PostMapping("/search")
    public GithubSearchResponse searchRepositories(
            @Valid @RequestBody GithubSearchRequest request) {

        String sort = request.getSort();

        if (sort == null || sort.isBlank()) {
            sort = "stars";
        }

        return service.searchAndSave(
                request.getQuery(),
                request.getLanguage(),
                sort
        );
    }

    @GetMapping("/repositories")
    public List<GithubRepositoryResponse> getRepositories(
            @RequestParam(required = false) String language,
            @RequestParam(required = false) Integer minStars,
            @RequestParam(defaultValue = "stars") String sort) {

        return service.getRepositories(
                language,
                minStars,
                sort
        );
    }
}