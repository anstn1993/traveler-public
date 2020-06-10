package me.moonsoo.travelerrestapi.post;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostTagRepository extends JpaRepository<PostTag, Integer> {
    List<PostTag> findAllByPost(Post post);
}
