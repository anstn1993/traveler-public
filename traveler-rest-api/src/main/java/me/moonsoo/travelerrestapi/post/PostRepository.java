package me.moonsoo.travelerrestapi.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Integer>, PostCustomRepository {

    @EntityGraph(attributePaths = {"postImageList", "postTagList"})
    Page<Post> findAllByAccount_NicknameContains(String search, Pageable pageable);

    @EntityGraph(attributePaths = {"postImageList", "postTagList"})
    Page<Post> findAllByArticleContains(String search, Pageable pageable);

    @EntityGraph(attributePaths = {"postImageList", "postTagList"})
    Page<Post> findAllByLocationContains(String search, Pageable pageable);
}
