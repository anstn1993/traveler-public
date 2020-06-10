package me.moonsoo.travelerrestapi.post;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostImageRepository extends JpaRepository<PostImage, Integer> {
    List<PostImage> findAllByPost(Post post);
}
