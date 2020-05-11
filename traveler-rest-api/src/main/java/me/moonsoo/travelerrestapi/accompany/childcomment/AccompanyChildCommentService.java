package me.moonsoo.travelerrestapi.accompany.childcomment;

import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.travelerrestapi.accompany.comment.AccompanyComment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AccompanyChildCommentService {

    @Autowired
    AccompanyChildCommentRepository accompanyChildCommentRepository;

    //동행 게시물의 대댓글 db에 저장
    public AccompanyChildComment save(AccompanyChildComment childComment, AccompanyComment comment, Account account) {
        childComment.setAccompanyComment(comment);
        childComment.setAccount(account);
        childComment.setRegDate(LocalDateTime.now());
        return accompanyChildCommentRepository.save(childComment);
    }
}
