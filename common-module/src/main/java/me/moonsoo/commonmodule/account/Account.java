package me.moonsoo.commonmodule.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;


@Getter @Setter
@EqualsAndHashCode(of = "id")
@AllArgsConstructor @NoArgsConstructor
@Builder
@Entity
public class Account implements Serializable {
    @Id
    @GeneratedValue
    private Integer id;//pk

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(unique = true)
    private String profileImageUri;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Enumerated(EnumType.STRING)
    private Sex sex;//성별

    @JsonIgnore
    private boolean emailAuth;//회원가입 시 이메일 인증 여부

    @JsonIgnore
    private String authCode;//이메일 인증에 필요한 인증 코드

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @JsonIgnore
    private Set<AccountRole> roles;//권한

    @Column(nullable = false)
    @JsonIgnore
    private LocalDateTime regDate;//가입 일자
}
