package me.moonsoo.travelerrestapi.accompany.childcomment;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

public class AccompanyChildCommentModel extends EntityModel<AccompanyChildComment> {
    public AccompanyChildCommentModel(AccompanyChildComment childComment, Link... links) {
        super(childComment, links);
        //self 링크 추가
        add(linkTo(AccompanyChildComment.class)
                .slash(childComment.getAccompanyComment().getAccompany().getId())
                .slash("comments").slash(childComment.getAccompanyComment().getId())
                .slash("child-comments")
                .slash(childComment.getId()).withSelfRel());
    }
}
