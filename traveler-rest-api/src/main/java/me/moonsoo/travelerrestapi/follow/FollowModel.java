package me.moonsoo.travelerrestapi.follow;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

public class FollowModel extends EntityModel<Follow> {
    public FollowModel(Follow follow, Link... links) {
        super(follow, links);
        add(linkTo(Follow.class)
                .slash(follow.getFollowingAccount().getId())
                .slash("followings")
                .slash(follow.getFollowedAccount().getId()).withSelfRel());
    }
}
