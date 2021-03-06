package me.moonsoo.travelerrestapi.follow;

import com.fasterxml.jackson.annotation.JsonCreator;
import me.moonsoo.commonmodule.account.Account;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

public class FollowAccountModel extends EntityModel<Account> {
    @JsonCreator
    public FollowAccountModel(Account account, Iterable<Link> links) {
        super(account, links);
    }
}
