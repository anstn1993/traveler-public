package me.moonsoo.travelerrestapi.follow;

import me.moonsoo.commonmodule.account.Account;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

public class FollowAccountModel extends EntityModel<Account> {
    public FollowAccountModel(Account content, Iterable<Link> links) {
        super(content, links);
    }
}
