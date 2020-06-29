package me.moonsoo.travelerrestapi.post.childcomment;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

public class PostChildCommentModel extends EntityModel<PostChildComment> {

    public PostChildCommentModel(PostChildComment postChildComment, Link... links) {
        super(postChildComment, links);
        //self링크
        add(linkTo(PostChildCommentController.class)
                .slash(postChildComment.getPost().getId())
                .slash("comments")
                .slash(postChildComment.getPostComment().getId())
                .slash("child-comments")
                .slash(postChildComment.getId())
                .withSelfRel());
    }
}
