package me.moonsoo.travelerrestapi.accompany.comment;

import me.moonsoo.travelerrestapi.properties.AppProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

public class AccompanyCommentModel extends EntityModel<AccompanyComment> {

    @Autowired
    AppProperties appProperties;

    public AccompanyCommentModel(AccompanyComment accompanyComment, Link... links) {
        super(accompanyComment, links);
        add(linkTo(AccompanyCommentController.class).slash(accompanyComment.getAccompany().getId()).slash("comments").slash(accompanyComment.getId()).withSelfRel());
    }
}
