package me.moonsoo.travelerrestapi.account;

import me.moonsoo.commonmodule.account.Account;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

public class AccountModel extends EntityModel<Account> {
    public AccountModel(Account account, Link... links) {
        super(account, links);
    }
}
