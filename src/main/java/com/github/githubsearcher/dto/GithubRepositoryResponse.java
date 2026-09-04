package com.github.githubsearcher.dto;



import java.time.Instant;

public class GithubRepositoryResponse {

    private Long id;
    private String name;
    private String description;
    private String owner;
    private String language;
    private Integer stars;
    private Integer forks;
    private Instant lastUpdated;

    public GithubRepositoryResponse() {
    }

    public GithubRepositoryResponse(Long id, String name, String description,
                                    String owner, String language,
                                    Integer stars, Integer forks,
                                    Instant lastUpdated) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.owner = owner;
        this.language = language;
        this.stars = stars;
        this.forks = forks;
        this.lastUpdated = lastUpdated;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getOwner() {
        return owner;
    }

    public String getLanguage() {
        return language;
    }

    public Integer getStars() {
        return stars;
    }

    public Integer getForks() {
        return forks;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }
}
