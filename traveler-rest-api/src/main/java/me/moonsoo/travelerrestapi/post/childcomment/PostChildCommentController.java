package me.moonsoo.travelerrestapi.post.childcomment;

import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.commonmodule.account.CurrentAccount;
import me.moonsoo.travelerrestapi.errors.ErrorsModel;
import me.moonsoo.travelerrestapi.post.Post;
import me.moonsoo.travelerrestapi.post.comment.PostComment;
import me.moonsoo.travelerrestapi.properties.AppProperties;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.DirectFieldBindingResult;
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

    //post 게시물의 대댓글 목록 조회 핸들러
    @GetMapping("{postId}/comments/{commentId}/child-comments")
    public ResponseEntity getPostChildComments(Pageable pageable,
                                               @PathVariable("postId") Post post,
                                               @PathVariable("commentId") PostComment postComment,
                                               PagedResourcesAssembler<PostChildComment> assembler,
                                               @CurrentAccount Account account) {
        if (post == null || postComment == null) {//리소스가 존재하지 않는 경우
            return ResponseEntity.notFound().build();
        }

        if (!postComment.getPost().equals(post)) {//댓글이 post 게시물의 자식이 아닌 경우
            Errors errors = new DirectFieldBindingResult(postComment, "postComment");
            errors.reject("conflict", "The comment resource is not a child of the post resource.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorsModel(errors));
        }

        Page<PostChildComment> postChildComments = postChildCommentService.findAllByPostComment(postComment, pageable);//대댓글 목록 fetch

        //hateoas적용
        PagedModel<PostChildCommentModel> postChildCommentModels = assembler.toModel(postChildComments, postChildComment -> new PostChildCommentModel(postChildComment));
        Link profileLink = new Link(appProperties.getBaseUrl() + appProperties.getProfileUri() + appProperties.getGetPostChildCommentsAnchor()).withRel("profile");
        postChildCommentModels.add(profileLink);
        if(account != null) {//인증을 한 경우
            Link createPostChildCommentLink = linkTo(PostChildComment.class)
                    .slash(post.getId())
                    .slash("comments")
                    .slash(postComment.getId())
                    .slash("child-comments")
                    .withRel("create-post-child-comment");
            postChildCommentModels.add(createPostChildCommentLink);
        }
        return ResponseEntity.ok(postChildCommentModels);
    }

    //post게시물의 대댓글 하나 조회
    @GetMapping("{postId}/comments/{commentId}/child-comments/{childCommentId}")
    public ResponseEntity getPostChildComment(@PathVariable("postId") Post post,
                                              @PathVariable("commentId") PostComment postComment,
                                              @PathVariable("childCommentId") PostChildComment postChildComment,
                                              @CurrentAccount Account account) {
        if (post == null || postComment == null || postChildComment == null) {//리소스가 존재하지 않는 경우
            return ResponseEntity.notFound().build();
        }

        if (!postComment.getPost().equals(post)) {//댓글이 post 게시물의 자식이 아닌 경우
            Errors errors = new DirectFieldBindingResult(postComment, "postComment");
            errors.reject("conflict", "The comment resource is not a child of the post resource.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorsModel(errors));
        }

        if (!postChildComment.getPostComment().equals(postComment)) {//댓글이 post 게시물의 자식이 아닌 경우
            Errors errors = new DirectFieldBindingResult(postChildComment, "postChildComment");
            errors.reject("conflict", "The child comment resource is not a child of the comment resource.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorsModel(errors));
        }

        //hateoas적용
        PostChildCommentModel postChildCommentModel = new PostChildCommentModel(postChildComment);
        WebMvcLinkBuilder linkBuilder = linkTo(PostChildCommentController.class)
                .slash(post.getId())
                .slash("comments")
                .slash(postComment.getId())
                .slash("child-comments");
        Link profileLink = new Link(appProperties.getBaseUrl() + appProperties.getProfileUri() + appProperties.getGetPostChildCommentAnchor()).withRel("profile");
        Link getPostChildCommentsLink = linkBuilder.withRel("get-post-child-comments");
        postChildCommentModel.add(profileLink, getPostChildCommentsLink);

        if(account != null && postChildComment.getAccount().equals(account)) {//인증한 상태 && 자신의 대댓글인 경우
            Link updatePostChildCommentLink = linkBuilder.slash(postChildComment.getId()).withRel("update-post-child-comment");
            Link deletePostChildCommentLink = linkBuilder.slash(postChildComment.getId()).withRel("delete-post-child-comment");
            postChildCommentModel.add(updatePostChildCommentLink, deletePostChildCommentLink);
        }
        return ResponseEntity.ok(postChildCommentModel);
    }

    //post 게시물의 대댓글 수정 핸들러
    @PutMapping("{postId}/comments/{commentId}/child-comments/{childCommentId}")
    public ResponseEntity updatePostChildComment(@PathVariable("postId") Post post,
                                                 @PathVariable("commentId") PostComment postComment,
                                                 @PathVariable("childCommentId") PostChildComment postChildComment,
                                                 @RequestBody @Valid PostChildCommentDto postChildCommentDto,
                                                 Errors errors,
                                                 @CurrentAccount Account account) {

        if(post == null || postComment == null || postChildComment == null) {//리소스가 없는 경우
            return ResponseEntity.notFound().build();
        }

        if (!postComment.getPost().equals(post)) {//댓글이 post 게시물의 자식이 아닌 경우
            errors.reject("conflict", "The comment resource is not a child of the post resource.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorsModel(errors));
        }

        if (!postChildComment.getPostComment().equals(postComment)) {//댓글이 post 게시물의 자식이 아닌 경우
            errors.reject("conflict", "The child comment resource is not a child of the comment resource.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorsModel(errors));
        }

        if(!postChildComment.getAccount().equals(account)) {//다른 사용자의 대댓글인 경우
            errors.reject("forbidden", "You can not update other user's contents.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorsModel(errors));
        }

        if(errors.hasErrors()) {//값이 유효하지 않은 경우
            return ResponseEntity.badRequest().body(new ErrorsModel(errors));
        }

        PostChildComment updatedPostChildComment = postChildCommentService.update(postChildComment, postChildCommentDto);//db에 댓글 update

        //hateoas적용
        PostChildCommentModel postChildCommentModel = new PostChildCommentModel(updatedPostChildComment);
        WebMvcLinkBuilder linkBuilder = linkTo(PostChildCommentController.class)
                .slash(post.getId())
                .slash("comments")
                .slash(postComment.getId())
                .slash("child-comments");
        Link profileLink = new Link(appProperties.getBaseUrl() + appProperties.getProfileUri() + appProperties.getUpdatePostChildCommentAnchor()).withRel("profile");
        Link getPostChildCommentsLink = linkBuilder.withRel("get-post-child-comments");
        Link deletePostChildCommentLink = linkBuilder.slash(updatedPostChildComment.getId()).withRel("delete-post-child-comment");
        postChildCommentModel.add(profileLink, getPostChildCommentsLink, deletePostChildCommentLink);
        return ResponseEntity.ok(postChildCommentModel);
    }

    //post게시물 대댓글 삭제 핸들러
    @DeleteMapping("{postId}/comments/{commentId}/child-comments/{childCommentId}")
    public ResponseEntity deletePostChildComment(@PathVariable("postId") Post post,
                                                 @PathVariable("commentId") PostComment postComment,
                                                 @PathVariable("childCommentId") PostChildComment postChildComment,
                                                 @CurrentAccount Account account) {
        if(post == null || postComment == null || postChildComment == null) {//리소스가 없는 경우
            return ResponseEntity.notFound().build();
        }

        if (!postComment.getPost().equals(post)) {//댓글이 post 게시물의 자식이 아닌 경우
            Errors errors = new DirectFieldBindingResult(postChildComment, "postChildComment");
            errors.reject("conflict", "The comment resource is not a child of the post resource.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorsModel(errors));
        }

        if (!postChildComment.getPostComment().equals(postComment)) {//댓글이 post 게시물의 자식이 아닌 경우
            Errors errors = new DirectFieldBindingResult(postChildComment, "postChildComment");
            errors.reject("conflict", "The child comment resource is not a child of the comment resource.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorsModel(errors));
        }

        if(!postChildComment.getAccount().equals(account)) {//다른 사용자의 대댓글인 경우
            Errors errors = new DirectFieldBindingResult(postChildComment, "postChildComment");
            errors.reject("forbidden", "You can not update other user's contents.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorsModel(errors));
        }

        postChildCommentService.delete(postChildComment);//db에서 대댓글 삭제
        return ResponseEntity.noContent().build();
    }
}
