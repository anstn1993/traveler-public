package me.moonsoo.travelerrestapi.post.comment;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

public class PostCommentModel extends EntityModel<PostComment> {

    public PostCommentModel(PostComment postComment, Link... links) {
        super(postComment, links);
        //self 링크 추가
        add(linkTo(PostCommentController.class)
                .slash(postComment.getPost().getId())
                .slash("comments")
                .slash(postComment.getId())
                .withSelfRel());
    }
}
