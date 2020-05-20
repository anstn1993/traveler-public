package me.moonsoo.travelerrestapi.follow;

import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.travelerrestapi.follow.linkmaker.AbstFollowLinkGenerator;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.server.RepresentationModelAssembler;

/*
* FollowAccountModelAssembler는 RepresentationModelAssembler의 구현체로 서비스 이용자가 조회한 특정 사용자의 팔로잉 사용자들을 팔로잉하고 있는지 여부에 따라서 팔로우, 혹은 언팔로우 링크를 제공하기 위해
* 만들어진 클래스다.
* */
public class FollowAccountModelAssembler implements RepresentationModelAssembler<Account, FollowAccountModel> {

    private AbstFollowLinkGenerator abstFollowLinkGenerator;

    private String followingOrFollower;//팔로잉에 대한 요청인지, 팔로워에 대한 요청인지 구분하는 flag 문자열

    public FollowAccountModelAssembler(AbstFollowLinkGenerator abstFollowLinkGenerator, String followingOrFollower) {
        this.abstFollowLinkGenerator = abstFollowLinkGenerator;
        this.followingOrFollower = followingOrFollower;
    }

    @Override
    public FollowAccountModel toModel(Account targetAccount) {
        Links links = abstFollowLinkGenerator.makeLinks(targetAccount, followingOrFollower);//self link, follow link(조건에 따라 follow링크가 될 수도 있고, unfollow링크가 될 수도 있다.)
        return new FollowAccountModel(targetAccount, links);
    }

    @Override
    public CollectionModel<FollowAccountModel> toCollectionModel(Iterable<? extends Account> accounts) {
        return null;
    }
}
