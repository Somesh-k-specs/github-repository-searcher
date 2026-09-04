package com.github.githubsearcher.repository;



import com.github.githubsearcher.entity.GithubRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GithubRepositoryRepository
        extends JpaRepository<GithubRepository, Long> {

    List<GithubRepository> findByLanguageIgnoreCase(String language);

    List<GithubRepository> findByStarsGreaterThanEqual(Integer minStars);

    List<GithubRepository> findByLanguageIgnoreCaseAndStarsGreaterThanEqual(
            String language,
            Integer minStars
    );
}
