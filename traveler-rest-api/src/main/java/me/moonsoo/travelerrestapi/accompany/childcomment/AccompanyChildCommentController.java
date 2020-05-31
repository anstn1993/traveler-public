package me.moonsoo.travelerrestapi.accompany.childcomment;


import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.commonmodule.account.CurrentAccount;
import me.moonsoo.travelerrestapi.accompany.Accompany;
import me.moonsoo.travelerrestapi.accompany.comment.AccompanyComment;
import me.moonsoo.travelerrestapi.errors.ErrorsModel;
import me.moonsoo.travelerrestapi.properties.AppProperties;
import org.apache.coyote.Response;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.DirectFieldBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping("/api/accompanies")
public class AccompanyChildCommentController {

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    AccompanyChildCommentService accompanyChildCommentService;

    @Autowired
    AppProperties appProperties;

    //대댓글 생성
    @PostMapping("/{accompanyId}/comments/{commentId}/child-comments")
    public ResponseEntity createChildComment(@PathVariable("accompanyId") Accompany accompany,
                                             @PathVariable("commentId") AccompanyComment comment,
                                             @RequestBody @Valid AccompanyChildCommentDto commentDto,
                                             Errors errors,
                                             @CurrentAccount Account account) {

        if (errors.hasErrors()) {//요청 본문이 잘못된 경우
            return ResponseEntity.badRequest().body(new ErrorsModel(errors));
        }

        if (accompany == null || comment == null || !comment.getAccompany().equals(accompany)) {//동행 게시물, 댓글이 존재하지 않거나 댓글이 해당 동행 게시물에 달린 댓글이 아닌 경우
            errors.reject("resource not found error", "Url resource you requested was not found.");
            return ResponseEntity.badRequest().body(new ErrorsModel(errors));
        }

        AccompanyChildComment childComment = modelMapper.map(commentDto, AccompanyChildComment.class);
        AccompanyChildComment savedChildComment = accompanyChildCommentService.save(childComment, accompany, comment, account);//대댓글 db에 저장

        //Hateoas적용
        AccompanyChildCommentModel childCommentModel = new AccompanyChildCommentModel(savedChildComment);

        Link getAccompanyChildCommentsLink = linkTo(AccompanyChildCommentController.class)//대댓글 목록 조회 링크
                .slash(accompany.getId())
                .slash("comments")
                .slash(comment.getId())
                .slash("child-comments")
                .withRel("get-accompany-child-comments");
        Link selfLink = childCommentModel.getLink("self").get();
        URI uri = selfLink.toUri();//LOCATION header uri
        Link updateAccompanyChildCommentLink = selfLink.withRel("update-accompany-child-comment");//대댓글 수정 링크
        Link deleteAccompanyChildCommentLink = selfLink.withRel("delete-accompany-child-comment");//대댓글 삭제 링크
        Link profileLink = new Link(appProperties.getBaseUrl() + appProperties.getProfileUri() + appProperties.getCreateAccompanyChildCommentAnchor()).withRel("profile");//프로필 링크
        childCommentModel.add(getAccompanyChildCommentsLink, updateAccompanyChildCommentLink, deleteAccompanyChildCommentLink, profileLink);
        return ResponseEntity.created(uri).body(childCommentModel);
    }

    //대댓글 목록 조회
    @GetMapping("/{accompanyId}/comments/{commentId}/child-comments")
    public ResponseEntity getChildComments(Pageable pageable,
                                           @PathVariable("accompanyId") Accompany accompany,
                                           @PathVariable("commentId") AccompanyComment comment,
                                           PagedResourcesAssembler<AccompanyChildComment> pagedResourcesAssembler,
                                           @CurrentAccount Account account) {

        //동행 게시물이나 댓글이 존재하지 않거나 댓글이 해당 동행 게시물에 달린 댓글이 아닌 경우
        if (accompany == null || comment == null || !comment.getAccompany().equals(accompany)) {
            return ResponseEntity.notFound().build();
        }

        Page<AccompanyChildComment> childComments = accompanyChildCommentService.findAllByAccompanyComment(comment, pageable);//대댓글 리스트 조회
        //Hateoas 적용
        PagedModel<AccompanyChildCommentModel> childCommentModels = pagedResourcesAssembler.toModel(childComments, c -> new AccompanyChildCommentModel(c));//각 요소에 self링크 추가
        Link profileLink = new Link(appProperties.getBaseUrl() + appProperties.getProfileUri() + appProperties.getGetAccompanyChildCommentsAnchor()).withRel("profile");//프로필 링크
        if (account != null) {
            Link createAccompanyChildCommentLink = linkTo(AccompanyChildCommentController.class)
                    .slash(accompany.getId())
                    .slash("comments")
                    .slash(comment.getId())
                    .slash("child-comments").withRel("create-accompany-child-comment");
            childCommentModels.add(createAccompanyChildCommentLink);
        }
        childCommentModels.add(profileLink);
        return ResponseEntity.ok(childCommentModels);
    }

    //대댓글 하나 조회
    @GetMapping("/{accompanyId}/comments/{commentId}/child-comments/{childCommentId}")
    public ResponseEntity getChildComment(@PathVariable("accompanyId") Accompany accompany,
                                          @PathVariable("commentId") AccompanyComment comment,
                                          @PathVariable("childCommentId") AccompanyChildComment childComment,
                                          @CurrentAccount Account account) {
        //동행 게시물, 댓글, 대댓글 리소스가 존재하지 않거나, 해당 게시물에 달린 댓글이 아니거나, 해당 댓글에 달린 대댓글이 아닌 경우
        if (accompany == null || comment == null || childComment == null || !comment.getAccompany().equals(accompany) || !childComment.getAccompanyComment().equals(comment)) {
            return ResponseEntity.notFound().build();
        }

        //Hateoas 적용
        AccompanyChildCommentModel childCommentModel = new AccompanyChildCommentModel(childComment);
        Link profileLink = new Link(appProperties.getBaseUrl() + appProperties.getProfileUri() + appProperties.getGetAccompanyChildCommentAnchor()).withRel("profile");//프로필 링크
        Link getAccompanyChildCommentsLink = linkTo(AccompanyChildCommentController.class)//대댓글 목록 조회 링크
                .slash(accompany.getId())
                .slash("comments")
                .slash(comment.getId())
                .slash("child-comments")
                .withRel("get-accompany-child-comments");
        childCommentModel.add(profileLink, getAccompanyChildCommentsLink);
        //인증 && 자신의 대댓글인 경우
        if (account != null && childComment.getAccount().equals(account)) {
            Link updateAccompanyChildCommentLink = childCommentModel.getLink("self").get().withRel("update-accompany-child-comment");//대댓글 수정 링크
            Link deleteAccompanyChildCommentLink = childCommentModel.getLink("self").get().withRel("delete-accompany-child-comment");//대댓글 삭제 링크
            childCommentModel.add(updateAccompanyChildCommentLink, deleteAccompanyChildCommentLink);
        }
        return ResponseEntity.ok(childCommentModel);
    }

    @PutMapping("/{accompanyId}/comments/{commentId}/child-comments/{childCommentId}")
    public ResponseEntity updateChildComment(@PathVariable("accompanyId") Accompany accompany,
                                             @PathVariable("commentId") AccompanyComment comment,
                                             @PathVariable("childCommentId") AccompanyChildComment childComment,
                                             @RequestBody @Valid AccompanyChildCommentDto childCommentDto,
                                             Errors errors,
                                             @CurrentAccount Account account) {
        //동행 게시물, 댓글, 대댓글 리소스가 존재하지 않거나, 해당 게시물에 달린 댓글이 아니거나, 해당 댓글에 달린 대댓글이 아닌 경우
        if (accompany == null || comment == null || childComment == null || !comment.getAccompany().equals(accompany) || !childComment.getAccompanyComment().equals(comment)) {
            return ResponseEntity.notFound().build();
        }

        //자신의 대댓글이 아닌 경우
        if (!childComment.getAccount().equals(account)) {
            errors.reject("forbidden", "You can not update other user's contents.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorsModel(errors));
        }

        //요청 본문이 유효하지 않은 경우
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(new ErrorsModel(errors));
        }

        AccompanyChildComment updatedChildComment = accompanyChildCommentService.update(childComment, childCommentDto);//대댓글 수정

        //Hateoas 적용
        AccompanyChildCommentModel childCommentModel = new AccompanyChildCommentModel(updatedChildComment);
        Link profileLink = new Link(appProperties.getBaseUrl() + appProperties.getProfileUri() + appProperties.getUpdateAccompanyChildCommentAnchor()).withRel("profile");//프로필 링크
        Link getAccompanyChildCommentsLink = linkTo(AccompanyChildCommentController.class)//대댓글 목록 조회 링크
                .slash(accompany.getId())
                .slash("comments")
                .slash(comment.getId())
                .slash("child-comments")
                .withRel("get-accompany-child-comments");
        Link deleteAccompanyChildCommentLink = childCommentModel.getLink("self").get().withRel("delete-accompany-child-comment");//대댓글 삭제 링크
        childCommentModel.add(profileLink, getAccompanyChildCommentsLink, deleteAccompanyChildCommentLink);
        return ResponseEntity.ok(childCommentModel);
    }

    @DeleteMapping("/{accompanyId}/comments/{commentId}/child-comments/{childCommentId}")
    public ResponseEntity deleteChildComment(@PathVariable("accompanyId") Accompany accompany,
                                             @PathVariable("commentId") AccompanyComment comment,
                                             @PathVariable("childCommentId") AccompanyChildComment childComment,
                                             @CurrentAccount Account account) {
        //동행 게시물, 댓글, 대댓글 리소스가 존재하지 않거나, 해당 게시물에 달린 댓글이 아니거나, 해당 댓글에 달린 대댓글이 아닌 경우
        if (accompany == null || comment == null || childComment == null || !comment.getAccompany().equals(accompany) || !childComment.getAccompanyComment().equals(comment)) {
            return ResponseEntity.notFound().build();
        }

        //자신의 대댓글이 아닌 경우
        if (!childComment.getAccount().equals(account)) {
            Errors errors = new DirectFieldBindingResult(account,"account");
            errors.reject("forbidden", "You can not delete other user's contents.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorsModel(errors));
        }

        accompanyChildCommentService.delete(childComment);
        return ResponseEntity.noContent().build();

    }

}
