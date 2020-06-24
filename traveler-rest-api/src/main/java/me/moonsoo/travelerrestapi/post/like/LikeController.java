package me.moonsoo.travelerrestapi.post.like;

import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.commonmodule.account.CurrentAccount;
import me.moonsoo.travelerrestapi.errors.ErrorsModel;
import me.moonsoo.travelerrestapi.follow.Follow;
import me.moonsoo.travelerrestapi.follow.FollowController;
import me.moonsoo.travelerrestapi.follow.FollowService;
import me.moonsoo.travelerrestapi.follow.linkmaker.AbstFollowLinkGenerator;
import me.moonsoo.travelerrestapi.follow.linkmaker.FollowingAccountLinkGenerator;
import me.moonsoo.travelerrestapi.post.Post;
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
    private AppProperties appProperties;

    @Autowired
    private FollowService followService;

    //좋아요 리소스 추가 핸들러
    @PostMapping("/{postId}/likes")
    public ResponseEntity createLike(@PathVariable("postId") Post post,
                                     @CurrentAccount Account account) {
        if (post == null) {//존재하지 않는 post 게시물인 경우
            return ResponseEntity.notFound().build();
        }

        //이미 해당 게시물에 좋아요를 했는지 검사
        Optional<Like> likeOpt = likeService.findByAccountAndPost(account, post);
        if (likeOpt.isPresent()) {//이미 좋아요를 한 상태인 경우
            Errors errors = new DirectFieldBindingResult(likeOpt, "like");
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

    //좋아요 목록 조회 핸들러
    @GetMapping("/{postId}/likes")
    public ResponseEntity getLikes(Pageable pageable,
                                   @PathVariable("postId") Post post,
                                   PagedResourcesAssembler<Like> assembler,
                                   @CurrentAccount Account account) {
        Page<Like> likes = likeService.findAllByPost(post, pageable);//좋아요 리소스 목록 fetch

        //hateoas 적용
        AbstFollowLinkGenerator followLinkGenerator = FollowingAccountInLikeLinkGenerator.builder()
                .currentUser(account)
                .followService(followService)
                .build();

        FollowingAccountInLikeModelAssembler followingAccountInLikeModelAssembler = new FollowingAccountInLikeModelAssembler(followLinkGenerator);
        PagedModel<LikeModel> likeModels = assembler.toModel(likes, followingAccountInLikeModelAssembler);
        Link profileLink = new Link(appProperties.getBaseUrl() + appProperties.getProfileUri() + appProperties.getGetLikesAnchor()).withRel("profile");//profile 링크
        likeModels.add(profileLink);
        return ResponseEntity.ok(likeModels);
    }

    //좋아요 리소스 하나 조회 핸들러
    @GetMapping("/{postId}/likes/{likeId}")
    public ResponseEntity getLike(@PathVariable("postId") Post post,
                                  @PathVariable("likeId") Like like,
                                  @CurrentAccount Account account) {

        if(post == null || like == null) {//존재하지 않는 post, like리소스인 경우
            return ResponseEntity.notFound().build();
        }

        if(!post.equals(like.getPost())) {//post리소스의 자식 like리소스가 아닌 경우
            Errors errors = new DirectFieldBindingResult(like, "like");
            errors.reject("conflict", "The like resource is not a child of the post");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorsModel(errors));
        }

        //hateoas적용
        LikeModel likeModel = new LikeModel(like);
        Link profileLink = new Link(appProperties.getBaseUrl() + appProperties.getProfileUri() + appProperties.getGetLikeAnchor()).withRel("profile");//profile 링크
        Link getLikesLink = linkTo(LikeController.class).slash(post.getId()).slash("likes").withRel("get-likes");
        likeModel.add(profileLink, getLikesLink);
        if(account != null && !account.equals(like.getAccount())) {//api요청 사용자와 like리소스의 주인이 동일하지 않은 경우에만 follow/unfollow링크 추가
            Optional<Follow> follow = followService.getFollow(account, like.getAccount());
            if(follow.isEmpty()) {//팔로잉중이지 않은 경우
                //팔로잉 링크 제공
                Link followLink = linkTo(FollowController.class).slash(account.getId()).slash("followings").withRel("create-account-follow");
                likeModel.add(followLink);
            }
            else {//팔로잉 중인 경우
                //언팔로우 링크 제공
                Link unfollowLink = linkTo(FollowController.class).slash(account.getId()).slash("followings").slash(like.getAccount().getId()).withRel("delete-account-follow");
                likeModel.add(unfollowLink);
            }
        }
        else if(account != null && account.equals(like.getAccount())) {//like 리소스가 자신의 리소스인 경우
            Link deleteLikeLink = linkTo(LikeController.class).slash(post.getId()).slash("likes").slash(like.getId()).withRel("delete-like");
            likeModel.add(deleteLikeLink);
        }

        return ResponseEntity.ok(likeModel);
    }

    //좋아요 리소스 삭제 핸들러
    @DeleteMapping("/{postId}/likes/{likeId}")
    public ResponseEntity deleteLike(@PathVariable("postId") Post post,
                                     @PathVariable("likeId") Like like,
                                     @CurrentAccount Account account) {
        if(post == null || like == null) {//존재하지 않는 post, like리소스인 경우
            return ResponseEntity.notFound().build();
        }

        if(!post.equals(like.getPost())) {//post리소스의 자식 like리소스가 아닌 경우
            Errors errors = new DirectFieldBindingResult(like, "like");
            errors.reject("conflict", "The like resource is not a child of the post");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorsModel(errors));
        }

        if(!like.getAccount().equals(account)) {//자신의 리소스가 아닌 경우
            Errors errors = new DirectFieldBindingResult(like, "like");
            errors.reject("forbidden", "You can not delete other user's like resource");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorsModel(errors));
        }

        likeService.delete(like);
        return ResponseEntity.noContent().build();
    }

}
