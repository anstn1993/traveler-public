package me.moonsoo.travelerapplication.account;

import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.Link;

import java.util.List;

@Getter
@Setter
public class AccountModel {
    public Integer id;
    public String username;
    public String email;
    public String profileImageUri;
    public String name;
    public String nickname;
    public String introduce;
    public String sex;
    public List<Link> links;//리소스 링크
}
