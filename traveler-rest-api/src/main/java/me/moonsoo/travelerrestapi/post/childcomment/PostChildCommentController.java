package me.moonsoo.travelerrestapi.post.childcomment;

import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.commonmodule.account.CurrentAccount;
import me.moonsoo.travelerrestapi.errors.ErrorsModel;
import me.moonsoo.travelerrestapi.post.Post;
import me.moonsoo.travelerrestapi.post.comment.PostComment;
import me.moonsoo.travelerrestapi.properties.AppProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping("/api/posts")
public class PostChildCommentController {

    @Autowired
    private PostChildCommentService postChildCommentService;

    @Autowired
    private AppProperties appProperties;

    //post게시물의 대댓글 리소스 생성 핸들러
    @PostMapping("{postId}/comments/{commentId}/child-comments")
    public ResponseEntity createPostChildComment(@PathVariable("postId") Post post,
                                                 @PathVariable("commentId") PostComment postComment,
                                                 @RequestBody @Valid PostChildCommentDto postChildCommentDto,
                                                 Errors errors,
                                                 @CurrentAccount Account account) {
        if (post == null || postComment == null) {//리소스가 존재하지 않는 경우
            return ResponseEntity.notFound().build();
        }

        if (!postComment.getPost().equals(post)) {//댓글이 post 게시물의 자식이 아닌 경우
            errors.reject("conflict", "The comment resource is not a child of the post resource.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorsModel(errors));
        }

        if (errors.hasErrors()) {//요청 본문의 값이 유효하지 않은 경우
            return ResponseEntity.badRequest().body(new ErrorsModel(errors));
        }

        //db에 저장
        PostChildComment postChildComment = postChildCommentService.save(post, postComment, account, postChildCommentDto);

        //hateoas적용
        PostChildCommentModel postChildCommentModel = new PostChildCommentModel(postChildComment);
        URI uri = postChildCommentModel.getLink("self").get().toUri();
        WebMvcLinkBuilder linkBuilder = linkTo(PostChildCommentController.class)
                .slash(post.getId())
                .slash("comments")
                .slash(postComment.getId())
                .slash("child-comments");
        Link profileLink = new Link(appProperties.getBaseUrl() + appProperties.getProfileUri() + appProperties.getCreatePostChildCommentAnchor()).withRel("profile");
        Link getPostChildCommentsLink = linkBuilder.withRel("get-post-child-comments");
        Link updatePostChildCommentLink = linkBuilder.slash(postChildComment.getId()).withRel("update-post-child-comment");
        Link deletePostChildCommentLink = linkBuilder.slash(postChildComment.getId()).withRel("delete-post-child-comment");

        postChildCommentModel.add(profileLink, getPostChildCommentsLink, updatePostChildCommentLink, deletePostChildCommentLink);//링크 추가
        return ResponseEntity.created(uri).body(postChildCommentModel);
    }

}
