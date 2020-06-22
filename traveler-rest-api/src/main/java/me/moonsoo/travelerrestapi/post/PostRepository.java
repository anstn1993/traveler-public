package me.moonsoo.travelerrestapi.post;

import me.moonsoo.commonmodule.account.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Integer>, PostCustomRepository {

    @EntityGraph(attributePaths = {"postImageList", "postTagList"})
    Page<Post> findAllByAccount_NicknameContains(String search, Pageable pageable);

    @EntityGraph(attributePaths = {"postImageList", "postTagList"})
    Page<Post> findAllByArticleContains(String search, Pageable pageable);

    @EntityGraph(attributePaths = {"postImageList", "postTagList"})
    Page<Post> findAllByLocationContains(String search, Pageable pageable);

    Optional<Post> findByAccount(Account account);

    void deleteByAccount(Account account);
}
