package me.moonsoo.travelerrestapi.accompany.comment;

import me.moonsoo.travelerrestapi.properties.AppProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

public class CommentModel extends EntityModel<Comment> {

    @Autowired
    AppProperties appProperties;

    public CommentModel(Comment comment, Link... links) {
        super(comment, links);
        add(linkTo(CommentController.class).slash(comment.getAccompany().getId()).slash("comments").slash(comment.getId()).withSelfRel());
    }
}
