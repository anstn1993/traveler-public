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
import org.springframework.http.ResponseEntity;
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
    AppProperties appProperties;

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
        if(account != null) {//인증 상태인 경우
            Link createPostCommentLink = linkTo(PostCommentController.class).slash(post.getId()).slash("comments").withRel("create-post-comment");
            postCommentModels.add(createPostCommentLink);
        }
        return ResponseEntity.ok(postCommentModels);
    }

}
