package me.moonsoo.travelerapplication.main.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import me.moonsoo.commonmodule.account.AccountRole;
import me.moonsoo.commonmodule.account.Sex;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

//세션에 저장할 사용자 dto class
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SessionAccount implements Serializable {

    private Integer id;

    private String email;

    private String profileImageUri;

    private String name;

    private String nickname;

    private Sex sex;

    private Set<AccountRole> roles;//권한
}
