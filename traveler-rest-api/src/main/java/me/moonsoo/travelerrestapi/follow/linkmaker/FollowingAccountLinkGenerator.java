package me.moonsoo.travelerrestapi.follow.linkmaker;

import lombok.Builder;
import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.travelerrestapi.follow.Follow;
import me.moonsoo.travelerrestapi.follow.FollowController;
import me.moonsoo.travelerrestapi.follow.FollowService;
import org.springframework.hateoas.Link;

import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

/*
* 특정 사용자가 팔로잉하고 있는 사용자들을 해당 api요청 사용자가 팔로우하고 있는지 여부에 따라서 링크를 제공하는 역할을 하는 클래스다.
* 특정 사용자가 팔로잉하고 있는 사용자 목록을 반환해주는 핸들러에서 사용된다.
* 'GET /api/accounts/{accountId}/followings'요청을 보내게 되면 accountId에 맵핑된 사용자가 팔로잉하고 있는 사용자들의 목록을 받게 되는데
* 이 클래스는 그 목록의 각 사용자들을 api요청자가 팔로우하고 있는지를 판단한다.
* 팔로잉상태인 경우에는 unfollow링크를, 언팔로우 상태인 경우에는 follow링크를 제공하게 된다.
* */

@Builder
public class FollowingAccountLinkGenerator extends AbstFollowLinkGenerator{

    private Account currentUser;//현재 서비스 이용자(api 요청 주체)

    private Account targetUser;//팔로잉 여부의 대상이 되는 사용자(서비스 이용자가 이 사용자를 팔로우하고 있는지 여부에 따라 팔로우/언팔로우 링크가 분기되어 제공된다.)

    private Account resourceAccount;//api요청시 리소스 account(api요청시 이 사용자의 팔로잉 사용자 목록을 조회한다)

    private FollowService followService;

    @Override
    protected Link makeSelfLink(Account targetUser, String followingOrFollower) {
        if(followingOrFollower.equals("following")) {
            return linkTo(FollowController.class).slash(resourceAccount.getId()).slash("followings").slash(targetUser.getId()).withSelfRel();
        }
        else {
            return linkTo(FollowController.class).slash(resourceAccount.getId()).slash("followers").slash(targetUser.getId()).withSelfRel();
        }
    }

    @Override
    protected boolean authorized() {
        if(currentUser != null) {
            return true;
        }
        return false;
    }

    //@param targetUser 팔로잉 여부의 대상이 되는 사용자(서비스 이용자가 이 사용자를 팔로우하고 있는지 여부에 따라 팔로우/언팔로우 링크가 분기되어 제공된다.)
    @Override
    protected boolean checkFollowStatus(Account targetUser) {
        this.targetUser = targetUser;
        Optional<Follow> followOpt = followService.getFollow(currentUser, targetUser);
        if(followOpt.isPresent()) {
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
