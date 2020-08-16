package me.moonsoo.travelerapplication.follow;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import me.moonsoo.commonmodule.account.Account;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

public class FollowAccountModel extends EntityModel<Account> {
    @JsonCreator
    public FollowAccountModel(@JsonProperty("content") Account content, @JsonProperty("_links") Iterable<Link> links) {
        super(content, links);
    }
}
