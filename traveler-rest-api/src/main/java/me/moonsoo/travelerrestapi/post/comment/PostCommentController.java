package me.moonsoo.travelerrestapi.post.comment;

import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.commonmodule.account.CurrentAccount;
import me.moonsoo.travelerrestapi.errors.ErrorsModel;
import me.moonsoo.travelerrestapi.post.Post;
import me.moonsoo.travelerrestapi.post.PostDto;
import me.moonsoo.travelerrestapi.properties.AppProperties;
import org.modelmapper.ModelMapper;
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
public class PostCommentController {

    @Autowired
    private PostCommentService postCommentService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AppProperties appProperties;

    //post 게시물에 댓글 리소스 추가 핸들러
    @PostMapping("/{postId}/comments")
    public ResponseEntity createPostComment(@PathVariable("postId") Post post,
                                            @RequestBody @Valid PostCommentDto postCommentDto,
                                            Errors errors,
                                            @CurrentAccount Account account) {
        if (post == null) {//post 리소스가 존재하지 않는 경우
            return ResponseEntity.notFound().build();
        }

        if (errors.hasErrors()) {//요청 본문의 값이 유효하지 않은 경우
            return ResponseEntity.badRequest().body(new ErrorsModel(errors));
        }

        PostComment postComment = modelMapper.map(postCommentDto, PostComment.class);
        PostComment savedPostComment = postCommentService.save(account, post, postComment);//db에 댓글 리소스 저장

        //Hateoas 적용
        PostCommentModel postCommentModel = new PostCommentModel(savedPostComment);
        URI uri = postCommentModel.getLink("self").get().toUri();
        Link profileLink = new Link(appProperties.getBaseUrl() + appProperties.getProfileUri() + appProperties.getCreatePostCommentAnchor()).withRel("profile");
        WebMvcLinkBuilder linkBuilder = linkTo(PostCommentController.class).slash(post.getId()).slash("comments");
        Link getPostCommentsLink = linkBuilder.withRel("get-post-comments");
        Link updatePostCommentLink = linkBuilder.slash(savedPostComment.getId()).withRel("update-post-comment");
        Link deletePostCommentLink = linkBuilder.slash(savedPostComment.getId()).withRel("delete-post-comment");
        postCommentModel.add(profileLink, getPostCommentsLink, updatePostCommentLink, deletePostCommentLink);
        return ResponseEntity.created(uri).body(postCommentModel);
    }

    //post 게시물의 댓글 목록 조회 핸들러
    @GetMapping("/{postId}/comments")
    public ResponseEntity getPostComments(Pageable pageable,
                                          @PathVariable("postId") Post post,
                                          PagedResourcesAssembler<PostComment> assembler,
                                          @CurrentAccount Account account) {
        if (post == null) {//존재하지 않은 post 게시물인 경우
            return ResponseEntity.notFound().build();
        }

        Page<PostComment> postComments = postCommentService.findAllByPost(post, pageable);//댓글 목록 fetch

        //Hateoas적용
        PagedModel<PostCommentModel> postCommentModels =
                assembler.toModel(postComments,
                        postComment -> new PostCommentModel(postComment, linkTo(PostCommentController.class)
                                .slash(post.getId())
                                .slash("comments")
                                .slash(postComment.getId())
                                .slash("child-comments")
                                .withRel("get-post-child-comments")));
        Link profileLink = new Link(appProperties.getBaseUrl() + appProperties.getProfileUri() + appProperties.getGetPostCommentsAnchor()).withRel("profile");
        postCommentModels.add(profileLink);
        if (account != null) {//인증 상태인 경우
            Link createPostCommentLink = linkTo(PostCommentController.class).slash(post.getId()).slash("comments").withRel("create-post-comment");
            postCommentModels.add(createPostCommentLink);
        }
        return ResponseEntity.ok(postCommentModels);
    }

    //post 게시물의 댓글 하나 조회 핸들러
    @GetMapping("/{postId}/comments/{commentId}")
    public ResponseEntity getPostComment(@PathVariable("postId") Post post,
                                         @PathVariable("commentId") PostComment postComment,
                                         @CurrentAccount Account account) {
        if (post == null || postComment == null) {//post리소스나 댓글 리소스가 존재하지 않는 경우
            return ResponseEntity.notFound().build();
        }

        if (!post.equals(postComment.getPost())) {//댓글이 post 게시물의 자식이 아닌 경우
            Errors errors = new DirectFieldBindingResult(postComment, "postComment");
            errors.reject("conflict", "The comment resource is not a child of the post resource.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorsModel(errors));
        }

        //hateoas적용
        PostCommentModel postCommentModel = new PostCommentModel(postComment);
        Link profileLink = new Link(appProperties.getBaseUrl() + appProperties.getProfileUri() + appProperties.getGetPostCommentAnchor()).withRel("profile");
        WebMvcLinkBuilder linkBuilder = linkTo(PostCommentController.class).slash(post.getId()).slash("comments");
        Link getPostCommentsLink = linkBuilder.withRel("get-post-comments");//댓글 목록 조회 링크
        Link getPostChildCommentsLink = linkBuilder.slash(postComment.getId()).slash("child-comments").withRel("get-post-child-comments");//댓글의 대댓글 목록 조회 링크
        postCommentModel.add(profileLink, getPostCommentsLink, getPostChildCommentsLink);
        if (account != null && postComment.getAccount().equals(account)) {//인증된 상태에서 자신의 댓글을 조회한 경우
            //댓글 수정, 삭제 링크 추가
            Link updatePostComment = linkBuilder.slash(postComment.getId()).withRel("update-post-comment");
            Link deletePostComment = linkBuilder.slash(postComment.getId()).withRel("delete-post-comment");
            postCommentModel.add(updatePostComment, deletePostComment);
        }
        return ResponseEntity.ok(postCommentModel);
    }

    //post게시물의 댓글 수정 핸들러
    @PutMapping("/{postId}/comments/{commentId}")
    public ResponseEntity updatePostComment(@PathVariable("postId") Post post,
                                            @PathVariable("commentId") PostComment postComment,
                                            @RequestBody @Valid PostCommentDto postCommentDto,
                                            Errors errors,
                                            @CurrentAccount Account account) {

        if (post == null || postComment == null) {//post 게시물이나 댓글 리소스가 존재하지 않는 경우
            return ResponseEntity.notFound().build();
        }

        if (!postComment.getPost().equals(post)) {//post 게시물의 자식 댓글이 아닌 경우
            errors.reject("conflict", "The comment resource is not a child of the post resource.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorsModel(errors));
        }

        if (!postComment.getAccount().equals(account)) {//다른 사용자의 댓글 리소스를 수정하려고 하는 경우
            errors.reject("forbidden", "You can not update other user's contents.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorsModel(errors));
        }

        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(new ErrorsModel(errors));
        }

        PostComment updatedComment = postCommentService.update(postComment, postCommentDto);//db에 댓글 update
        //hateoas 적용
        PostCommentModel postCommentModel = new PostCommentModel(updatedComment);
        WebMvcLinkBuilder linkBuilder = linkTo(PostCommentController.class).slash(post.getId()).slash("comments");
        Link profileLink = new Link(appProperties.getBaseUrl() + appProperties.getProfileUri() + appProperties.getUpdatePostCommentAnchor()).withRel("profile");
        Link getPostComments = linkBuilder.withRel("get-post-comments");
        Link deletePostComments = linkBuilder.slash(updatedComment.getId()).withRel("delete-post-comment");
        postCommentModel.add(profileLink, getPostComments, deletePostComments);
        return ResponseEntity.ok(postCommentModel);
    }

    //post게시물의 댓글 삭제 핸들러
    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity deletePostComment(@PathVariable("postId") Post post,
                                            @PathVariable("commentId") PostComment postComment,
                                            @CurrentAccount Account account) {
        if (post == null || postComment == null) {//post 게시물이나 댓글 리소스가 존재하지 않는 경우
            return ResponseEntity.notFound().build();
        }

        if (!postComment.getPost().equals(post)) {//post 게시물의 자식 댓글이 아닌 경우
            Errors errors = new DirectFieldBindingResult(postComment, "postComment");
            errors.reject("conflict", "The comment resource is not a child of the post resource.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorsModel(errors));
        }

        if (!postComment.getAccount().equals(account)) {//다른 사용자의 댓글 리소스를 수정하려고 하는 경우
            Errors errors = new DirectFieldBindingResult(postComment, "postComment");
            errors.reject("forbidden", "You can not update other user's contents.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorsModel(errors));
        }

        postCommentService.delete(postComment);//db에서 댓글 삭제
        return ResponseEntity.noContent().build();
    }
}
