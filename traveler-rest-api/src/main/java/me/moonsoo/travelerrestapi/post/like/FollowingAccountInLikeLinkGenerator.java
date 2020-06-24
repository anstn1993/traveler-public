package me.moonsoo.travelerrestapi.post.like;

import lombok.Builder;
import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.travelerrestapi.follow.Follow;
import me.moonsoo.travelerrestapi.follow.FollowController;
import me.moonsoo.travelerrestapi.follow.FollowService;
import me.moonsoo.travelerrestapi.follow.linkmaker.AbstFollowLinkGenerator;
import org.springframework.hateoas.Link;

import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;


//좋아요 리소스 목록 조회 시에 각 리소스의 소유자에 대한 팔로잉 여부에 따른 팔로우/언팔로우 링크를 생성해주는 클래스다.
@Builder
public class FollowingAccountInLikeLinkGenerator extends AbstFollowLinkGenerator {

    private Account currentUser;//현재 서비스 이용자(api 요청 주체)

    private Account targetUser;//팔로잉 여부의 대상이 되는 사용자(서비스 이용자가 이 사용자를 팔로우하고 있는지 여부에 따라 팔로우/언팔로우 링크가 분기되어 제공된다.)

    private FollowService followService;


    //self링크 생성
    @Override
    protected Link makeSelfLink(Object resource) {
        Like like = (Like) resource;
        return linkTo(LikeController.class).slash(like.getPost().getId()).slash("likes").slash(like.getId()).withSelfRel();
    }

    @Override
    protected boolean authorized() {
        if (currentUser != null) {
            return true;
        }
        return false;
    }

    //@param targetUser 팔로잉 여부의 대상이 되는 사용자(서비스 이용자가 이 사용자를 팔로우하고 있는지 여부에 따라 팔로우/언팔로우 링크가 분기되어 제공된다.)
    @Override
    protected boolean checkFollowStatus(Object resource) {
        Like like = (Like) resource;
        this.targetUser = like.getAccount();
        Optional<Follow> followOpt = followService.getFollow(currentUser, targetUser);
        if (followOpt.isPresent()) {
            return true;
        }
        return false;
    }

    @Override
    protected Link makeFollowLink() {
        return linkTo(FollowController.class).slash(currentUser.getId()).slash("followings").withRel("create-account-follow");
    }

    @Override
    protected Link makeUnfollowLink() {
        return linkTo(FollowController.class).slash(currentUser.getId()).slash("followings").slash(targetUser.getId()).withRel("delete-account-follow");
    }
}
