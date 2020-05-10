package me.moonsoo.travelerrestapi.accompany.comment;

import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.commonmodule.account.CurrentAccount;
import me.moonsoo.travelerrestapi.accompany.Accompany;
import me.moonsoo.travelerrestapi.errors.ErrorsModel;
import me.moonsoo.travelerrestapi.properties.AppProperties;
import org.apache.coyote.Response;
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
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping(value = "/api/accompanies")
public class CommentController {

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    CommentService commentService;

    @Autowired
    AppProperties appProperties;

    //댓글 추가
    @PostMapping("/{accompanyId}/comments")
    public ResponseEntity createComment(@PathVariable("accompanyId") Accompany accompany,
                                        @RequestBody @Valid CommentDto commentDto,
                                        Errors errors,
                                        @CurrentAccount Account account) {
        //동행 게시물 리소스가 존재하지 않는 경우
        if (accompany == null) {
            errors.reject("accompany.id", "Accompany resource is not found");
            return ResponseEntity.badRequest().body(new ErrorsModel(errors));
        }
        //요청 본문에 문제가 있는 경우
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(new ErrorsModel(errors));
        }
        //실제 db로 들어갈 엔티티
        Comment comment = modelMapper.map(commentDto, Comment.class);
        Comment savedComment = commentService.save(accompany, account, comment);//댓글 db에 저장
        //Hateoas 적용
        CommentModel commentModel = new CommentModel(savedComment);
        WebMvcLinkBuilder linkBuilder = linkTo(CommentController.class).slash(accompany.getId()).slash("comments");
        Link getAccompanyComments = linkBuilder.withRel("get-accompany-comments");
        Link updateAccompanyComment = linkBuilder.slash(savedComment.getId()).withRel("update-accompany-comment");
        Link deleteAccompanyComment = linkBuilder.slash(savedComment.getId()).withRel("delete-accompany-comment");
        Link profileLink = new Link(appProperties.getBaseUrl() + appProperties.getProfileUri() + appProperties.getCreateAccompanyCommentAnchor()).withRel("profile");
        commentModel.add(getAccompanyComments, updateAccompanyComment, deleteAccompanyComment, profileLink);
        URI uri = linkTo(CommentController.class).slash(1).toUri();
        return ResponseEntity.created(uri).body(commentModel);
    }

    //댓글 목록 조회
    @GetMapping("/{accompanyId}/comments")
    public ResponseEntity getComments(@PathVariable("accompanyId") Accompany accompany,
                                      Pageable pageable,
                                      PagedResourcesAssembler<Comment> assembler,
                                      @CurrentAccount Account account) {
        if (accompany == null) {//동행 게시물 리소스가 존재하지 않는 경우
            return ResponseEntity.notFound().build();
        }
        Page<Comment> accompanyComments = commentService.findAllByAccompany(accompany, pageable);
        PagedModel<CommentModel> commentModels = assembler.toModel(accompanyComments, c -> new CommentModel(c));
        commentModels.add(new Link(appProperties.getBaseUrl() + appProperties.getProfileUri() + appProperties.getGetAccompanyCommentsAnchor()).withRel("profile"));
        if (account != null) {//인증된 사용자의 요청인 경우
            commentModels.add(linkTo(CommentController.class).slash(accompany.getId()).slash("comments").withRel("create-accompany-comment"));
        }
        return ResponseEntity.ok(commentModels);
    }

    //댓글 하나 조회
    @GetMapping("/{accompanyId}/comments/{commentId}")
    public ResponseEntity getComment(@PathVariable("accompanyId") Accompany accompany,
                                     @PathVariable("commentId") Comment comment,
                                     @CurrentAccount Account account) {
        if (accompany == null || comment == null || !comment.getAccompany().equals(accompany)) {//존재하지 않는 게시물, 댓글 리소스이거나 해당 게시물의 댓글이 아닌 경우
            return ResponseEntity.notFound().build();
        }

        //Hateoas적용
        CommentModel commentModel = new CommentModel(comment);
        WebMvcLinkBuilder linkBuilder = linkTo(CommentController.class).slash(accompany.getId()).slash("comments");
        Link profileLink = new Link(appProperties.getBaseUrl() + appProperties.getProfileUri() + appProperties.getGetAccompanyCommentAnchor()).withRel("profile");
        Link getAccompanyComments = linkBuilder.withRel("get-accompany-comments");
        commentModel.add(getAccompanyComments, profileLink);
        if (account != null) {
            Link updateAccompanyComment = linkBuilder.slash(comment.getId()).withRel("update-accompany-comment");
            Link deleteAccompanyComment = linkBuilder.slash(comment.getId()).withRel("delete-accompany-comment");
            commentModel.add(updateAccompanyComment, deleteAccompanyComment);
        }
        return ResponseEntity.ok(commentModel);
    }

    //댓글 수정
    @PutMapping("/{accompanyId}/comments/{commentId}")
    public ResponseEntity updateComment(@PathVariable("accompanyId") Accompany accompany,
                                        @PathVariable("commentId") Comment comment,
                                        @RequestBody @Valid CommentDto commentDto,
                                        Errors errors,
                                        @CurrentAccount Account account) {
        if(accompany == null || comment == null || !comment.getAccompany().equals(accompany)) {//존재하지 않는 게시물, 댓글 리소스이거나 해당 게시물의 댓글이 아닌 경우
            return ResponseEntity.notFound().build();
        }

        if(!comment.getAccount().equals(account)) {//다른 사용자의 댓글을 수정하려고 하는 경우
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if(errors.hasErrors()) {//요청 본문이 유효하지 않은 경우
            return ResponseEntity.badRequest().body(new ErrorsModel(errors));
        }

        Comment updatedComment = commentService.updateComment(comment, commentDto);//수정된 댓글 update

        //Hateoas적용
        CommentModel commentModel = new CommentModel(updatedComment);
        WebMvcLinkBuilder linkBuilder = linkTo(CommentController.class).slash(accompany.getId()).slash("comments");
        Link getAccompanyComments = linkBuilder.withRel("get-accompany-comments");
        Link deleteAccompanyComment = linkBuilder.withRel("delete-accompany-comment");
        Link profileLink = new Link(appProperties.getBaseUrl() + appProperties.getProfileUri() + appProperties.getUpdateAccompanyCommentAnchor()).withRel("profile");
        commentModel.add(getAccompanyComments, deleteAccompanyComment, profileLink);
        return ResponseEntity.ok(commentModel);
    }

    //댓글 삭제
    @DeleteMapping("/{accompanyId}/comments/{commentId}")
    public ResponseEntity deleteComment(@PathVariable("accompanyId") Accompany accompany,
                                        @PathVariable("commentId") Comment comment,
                                        @CurrentAccount Account account) {
        if(accompany == null || comment == null || !comment.getAccompany().equals(accompany)) {//리소스가 존재하지 않거나 요청한 게시물에 달린 댓글이 아닌 경우
            return ResponseEntity.notFound().build();
        }

        if(!comment.getAccount().equals(account)) {//자신의 댓글이 아닌 경우
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        commentService.delete(comment);
        return ResponseEntity.noContent().build();
    }
}
