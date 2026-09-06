package com.github.githubsearcher.dto;

import java.util.List;

public class GithubRepositoryListResponse {

    private List<GithubRepositoryResponse> repositories;

    public GithubRepositoryListResponse() {
    }

    public GithubRepositoryListResponse(List<GithubRepositoryResponse> repositories) {
        this.repositories = repositories;
    }

    public List<GithubRepositoryResponse> getRepositories() {
        return repositories;
    }
}