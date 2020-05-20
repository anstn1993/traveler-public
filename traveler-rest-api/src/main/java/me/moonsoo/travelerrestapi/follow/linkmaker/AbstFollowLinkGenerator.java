package me.moonsoo.travelerrestapi.follow.linkmaker;


import me.moonsoo.commonmodule.account.Account;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;

//조회한 사용자에 대한 팔로우 상태를 체크할 수 있는 helper interface
public abstract class AbstFollowLinkGenerator {

    protected abstract Link makeSelfLink(Account targetUser, String followingOrFollower);
    protected abstract boolean authorized();//api 요청자의 oauth 인증 상태 check
    protected abstract boolean checkFollowStatus(Account targetAccount);//targetAccount 팔로잉 여부를 반환해주는 메소드
    protected abstract Link makeFollowLink();//targetAccount를 follow하는 링크
    protected abstract Link makeUnfollowLink();//targetAccount를 unfollow하는 링크

    public final Links makeLinks(Account targetAccount, String followingOrFollower) {
        Link selfLink = makeSelfLink(targetAccount, followingOrFollower);
        if(!authorized()) {//인증이 안 된 상태면 follow link는 담지 않는다.
            return Links.of(selfLink);
        }
        Link followOrUnfollowLink;//팔로우 or 언팔로우 링크
        boolean following = checkFollowStatus(targetAccount);//targetAccount 팔로잉여부를 표시하는 flag
        if(following) {//팔로잉 상태인 경우 언팔로우 하는 링크를 제공
            followOrUnfollowLink = makeUnfollowLink();
        }
        else {//언팔로우 상태인 경우 팔로우 하는 링크를 제공
            followOrUnfollowLink = makeFollowLink();
        }

        return Links.of(selfLink, followOrUnfollowLink);
    }

}
