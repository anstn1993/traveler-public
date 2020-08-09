package me.moonsoo.travelerapplication.account;

import lombok.*;
import me.moonsoo.commonmodule.account.AccountRole;
import me.moonsoo.commonmodule.account.Sex;

import java.io.Serializable;
import java.util.Set;

//세션에 저장할 사용자 dto class
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SessionAccount implements Serializable {

    private Integer id;

    private String username;

    private String email;

    private String profileImageUri;

    private String name;

    private String nickname;

    private String introduce;

    private Sex sex;

    private Set<AccountRole> roles;//권한
}
