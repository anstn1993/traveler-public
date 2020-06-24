package me.moonsoo.travelerrestapi.post.like;

import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.travelerrestapi.follow.FollowAccountModel;
import me.moonsoo.travelerrestapi.follow.linkmaker.AbstFollowLinkGenerator;
import me.moonsoo.travelerrestapi.post.Post;
import me.moonsoo.travelerrestapi.post.PostModel;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.server.RepresentationModelAssembler;

public class FollowingAccountInLikeModelAssembler implements RepresentationModelAssembler<Like, LikeModel> {

    private AbstFollowLinkGenerator abstFollowLinkGenerator;


    public FollowingAccountInLikeModelAssembler(AbstFollowLinkGenerator abstFollowLinkGenerator) {
        this.abstFollowLinkGenerator = abstFollowLinkGenerator;
    }

    @Override
    public LikeModel toModel(Like like) {
        Links links = abstFollowLinkGenerator.makeLinks(like);//self link, follow link(조건에 따라 follow링크가 될 수도 있고, unfollow링크가 될 수도 있다.)
        return new LikeModel(like, links);
    }
}
