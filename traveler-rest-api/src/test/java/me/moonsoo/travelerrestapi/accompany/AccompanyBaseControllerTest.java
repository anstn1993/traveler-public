package me.moonsoo.travelerrestapi.accompany;

import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.commonmodule.account.AccountRole;
import me.moonsoo.commonmodule.account.Sex;
import me.moonsoo.travelerrestapi.BaseControllerTest;
import me.moonsoo.travelerrestapi.accompany.childcomment.AccompanyChildComment;
import me.moonsoo.travelerrestapi.accompany.childcomment.AccompanyChildCommentRepository;
import me.moonsoo.travelerrestapi.accompany.comment.AccompanyComment;
import me.moonsoo.travelerrestapi.accompany.comment.AccompanyCommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.util.Jackson2JsonParser;

import java.time.LocalDateTime;
import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class AccompanyBaseControllerTest extends BaseControllerTest {

    @Autowired
    protected AccompanyRepository accompanyRepository;

    @Autowired
    protected AccompanyCommentRepository accompanyCommentRepository;

    @Autowired
    protected AccompanyChildCommentRepository accompanyChildCommentRepository;



    protected AccompanyDto createAccompanyDto(int index) {
        return AccompanyDto.builder()
                .title("title" + index)
                .article("article")
                .location("somewhere")
                .latitude(30.1111)
                .longitude(120.1111)
                .startDate(LocalDateTime.of(2020, 4, 24, 13, 00, 00))
                .endDate(LocalDateTime.of(2020, 4, 25, 13, 00, 00))
                .build();
    }

    protected Accompany createAccompany(Account account, int index) {
        Accompany accompany = Accompany.builder()
                .title("title" + index)
                .article("article" + index)
                .location("somewhere")
                .latitude(30.1111)
                .longitude(120.1111)
                .startDate(LocalDateTime.of(2020, 4, 24, 13, 00, 00))
                .endDate(LocalDateTime.of(2020, 4, 25, 13, 00, 00))
                .account(account)
                .regDate(LocalDateTime.now())
                .viewCount(0)
                .build();
        return accompanyRepository.save(accompany);
    }



    protected AccompanyComment createComment(Account account, Accompany accompany, int index) {
        AccompanyComment accompanyComment = AccompanyComment.builder()
                .comment("This is comment" + index)
                .account(account)
                .accompany(accompany)
                .regDate(LocalDateTime.now())
                .build();
        return accompanyCommentRepository.save(accompanyComment);
    }

    protected AccompanyChildComment createChildComment(Account account, Accompany accompany, AccompanyComment accompanyComment, int index) {
        AccompanyChildComment accompanyChildComment = AccompanyChildComment.builder()
                .account(account)
                .accompany(accompany)
                .accompanyComment(accompanyComment)
                .comment("This is child comment" + index)
                .regDate(LocalDateTime.now())
                .build();
        return accompanyChildCommentRepository.save(accompanyChildComment);
    }

}
