package me.moonsoo.travelerrestapi.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostCustomRepository {
    Page<Post> findAllByTagContains(String search, Pageable pageable);
}
