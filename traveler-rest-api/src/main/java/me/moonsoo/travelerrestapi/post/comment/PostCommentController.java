package me.moonsoo.travelerrestapi.post.comment;

import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.commonmodule.account.CurrentAccount;
import me.moonsoo.travelerrestapi.errors.ErrorsModel;
import me.moonsoo.travelerrestapi.post.Post;
import me.moonsoo.travelerrestapi.post.PostDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/posts")
public class PostCommentController {

    @Autowired
    private PostCommentService postCommentService;

    @Autowired
    private ModelMapper modelMapper;

    //post 게시물에 댓글 리소스 추가 핸들러
    @PostMapping("/{postId}/comments")
    public ResponseEntity createPostComment(@PathVariable("postId") Post post,
                                            @RequestBody @Valid PostCommentDto postCommentDto,
                                            Errors errors,
                                            @CurrentAccount Account account) {
        if(post == null) {//post 리소스가 존재하지 않는 경우
            return ResponseEntity.notFound().build();
        }

        if(errors.hasErrors()) {//요청 본문의 값이 유효하지 않은 경우
            return ResponseEntity.badRequest().body(new ErrorsModel(errors));
        }

        PostComment postComment = modelMapper.map(postCommentDto, PostComment.class);
        PostComment savedPostComment = postCommentService.save(account, post, postComment);//db에 댓글 리소스 저장

        //Hateoas 적용
        PostCommentModel postCommentModel = new PostCommentModel(savedPostComment);


    }

}
