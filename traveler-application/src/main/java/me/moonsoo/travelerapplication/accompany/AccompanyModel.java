package me.moonsoo.travelerapplication.accompany;

import lombok.Getter;
import lombok.Setter;
import me.moonsoo.travelerapplication.account.AccountModel;
import org.springframework.hateoas.Link;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
public class AccompanyModel {
    public Integer id;
    public AccountModel account;
    public String title;
    public String article;
    public LocalDateTime startDate;
    public LocalDateTime endDate;
    public String location;
    public Double latitude;
    public Double longitude;
    public ZonedDateTime regDate;
    public Integer viewCount;
    public List<Link> links;//리소스 링크
}
