package me.moonsoo.travelerrestapi.follow;


import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import me.moonsoo.commonmodule.account.Account;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

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
    Integer id;

    @ManyToOne
    @JsonSerialize(using = FollowAccountSerializer.class)
    Account followingAccount;

    @ManyToOne
    @JsonSerialize(using = FollowAccountSerializer.class)
    Account followedAccount;

}
