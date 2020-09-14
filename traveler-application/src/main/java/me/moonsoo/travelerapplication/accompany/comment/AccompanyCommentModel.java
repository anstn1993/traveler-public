package me.moonsoo.travelerapplication.accompany.comment;

import me.moonsoo.travelerapplication.accompany.AccompanyModel;
import me.moonsoo.travelerapplication.account.AccountModel;
import org.springframework.hateoas.Link;

import java.time.ZonedDateTime;
import java.util.List;

public class AccompanyCommentModel {
    public Integer id;
    public AccountModel account;
    public AccompanyModel accompany;
    public String comment;
    public ZonedDateTime regDate;
    public List<Link> links;
}
