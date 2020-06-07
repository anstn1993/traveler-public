package me.moonsoo.travelerrestapi.post;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

public class PostModel extends EntityModel<Post> {
    public PostModel(Post post, Link... links) {
        super(post, links);
        add(linkTo(PostController.class).slash(post.getId()).withSelfRel());//self링크 추가
    }
}
