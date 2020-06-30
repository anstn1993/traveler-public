package me.moonsoo.travelerrestapi.follow;


import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import me.moonsoo.commonmodule.account.Account;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Entity
public class Follow {

    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne
    @JsonSerialize(using = FollowAccountSerializer.class)
    private Account followingAccount;

    @ManyToOne
    @JsonSerialize(using = FollowAccountSerializer.class)
    private Account followedAccount;

}
