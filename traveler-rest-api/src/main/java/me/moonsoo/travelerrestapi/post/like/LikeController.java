package me.moonsoo.travelerrestapi.post.like;

import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.commonmodule.account.CurrentAccount;
import me.moonsoo.travelerrestapi.errors.ErrorsModel;
import me.moonsoo.travelerrestapi.post.Post;
import me.moonsoo.travelerrestapi.properties.AppProperties;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.DirectFieldBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/api/posts")
public class LikeController {

    @Autowired
    private LikeService likeService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AppProperties appProperties;

    @PostMapping("/{postId}/likes")
    public ResponseEntity createLike(@PathVariable("postId") Post post,
                                     @CurrentAccount Account account) {
        if (post == null) {//존재하지 않는 post 게시물인 경우
            return ResponseEntity.notFound().build();
        }

        //이미 해당 게시물에 좋아요를 했는지 검사
        Optional<Like> likeOpt = likeService.findByAccountAndPost(account, post);
        if(likeOpt.isPresent()) {//이미 좋아요를 한 상태인 경우
            Errors errors = new DirectFieldBindingResult(likeOpt, "likeOpt");
            errors.reject("conflict", "You already added a like resource on this post.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorsModel(errors));
        }

        Like like = Like.builder()
                .account(account)
                .post(post)
                .build();
        Like savedLike = likeService.save(like);//리소스 db에 저장
        //Hateoas적용
        LikeModel likeModel = new LikeModel(savedLike);
        URI uri = likeModel.getLink("self").get().toUri();
        Link profileLink = new Link(appProperties.getBaseUrl() + appProperties.getProfileUri() + appProperties.getCreateLikeAnchor()).withRel("profile");//profile 링크
        WebMvcLinkBuilder linkBuilder = linkTo(LikeController.class).slash(post.getId()).slash("likes");
        Link getLikesLink = linkBuilder.withRel("get-likes");
        Link deleteLikeLink = linkBuilder.slash(savedLike.getId()).withRel("delete-like");
        likeModel.add(profileLink, getLikesLink, deleteLikeLink);
        return ResponseEntity.created(uri).body(likeModel);
    }

}
