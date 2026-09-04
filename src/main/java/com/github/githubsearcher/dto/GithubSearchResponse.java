package com.github.githubsearcher.dto;



import java.util.List;

public class GithubSearchResponse {

    private String message;

    private List<GithubRepositoryResponse> repositories;

    public GithubSearchResponse() {
    }

    public GithubSearchResponse(String message,
                                List<GithubRepositoryResponse> repositories) {
        this.message = message;
        this.repositories = repositories;
    }

    public String getMessage() {
        return message;
    }

    public List<GithubRepositoryResponse> getRepositories() {
        return repositories;
    }
}
