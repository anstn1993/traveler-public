package me.moonsoo.travelerrestapi.accompany.comment;

import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.commonmodule.account.CurrentAccount;
import me.moonsoo.travelerrestapi.accompany.Accompany;
import me.moonsoo.travelerrestapi.errors.ErrorsModel;
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
import org.springframework.ui.ModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.time.LocalDateTime;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/api/accompanies")
public class CommentController {

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    AppProperties appProperties;

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
        comment.setAccompany(accompany);
        comment.setAccount(account);
        comment.setRegDate(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);//댓글 db에 저장
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

    @GetMapping("/{accompanyId}/comments")
    public ResponseEntity getComment(@PathVariable("accompanyId") Accompany accompany,
                                     Pageable pageable,
                                     PagedResourcesAssembler<Comment> assembler,
                                     @CurrentAccount Account account) {
        if (accompany == null) {
            Errors errors = new BeanPropertyBindingResult(accompany, "accompany");
            errors.reject("accompany.id", "Accompany resource is not found");
            return ResponseEntity.badRequest().body(new ErrorsModel(errors));
        }

        Page<Comment> accompanyComments = commentRepository.findAllByAccompany(accompany, pageable);
        PagedModel<CommentModel> commentModels = assembler.toModel(accompanyComments, c -> new CommentModel(c));
        commentModels.add(new Link(appProperties.getBaseUrl() + appProperties.getProfileUri() + appProperties.getGetAccompanyCommentsAnchor()).withRel("profile"));
        if(account != null) {//인증된 사용자의 요청인 경우
            commentModels.add(linkTo(CommentController.class).slash(accompany.getId()).slash("comments").withRel("create-accompany-comment"));
        }

        return ResponseEntity.ok(commentModels);
    }
}