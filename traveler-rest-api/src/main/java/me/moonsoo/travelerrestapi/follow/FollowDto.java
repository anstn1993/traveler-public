package me.moonsoo.travelerrestapi.follow;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.commonmodule.account.AccountSerializer;

import javax.validation.constraints.NotNull;

//request body의 데이터를 바인딩할 모델 클래스
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FollowDto {

    @NotNull
    @JsonSerialize(using = AccountSerializer.class)
    private Account followedAccount;

}
