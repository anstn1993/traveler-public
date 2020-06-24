package me.moonsoo.travelerrestapi.post.like;

import me.moonsoo.travelerrestapi.post.Post;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

public class LikeModel extends EntityModel<Like> {
    public LikeModel(Like like, Link... links) {
        super(like, links);
        //self 링크 추가
        add(linkTo(LikeController.class).slash(like.getPost().getId()).slash("likes").slash(like.getId()).withSelfRel());
    }
}
