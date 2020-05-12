package me.moonsoo.travelerrestapi.accompany.comment;

import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.travelerrestapi.accompany.Accompany;
import me.moonsoo.travelerrestapi.accompany.childcomment.AccompanyChildCommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AccompanyCommentService {
    @Autowired
    AccompanyCommentRepository accompanyCommentRepository;

    @Autowired
    AccompanyChildCommentRepository accompanyChildCommentRepository;

    //동행 게시물에 댓글 추가
    public AccompanyComment save(Accompany accompany, Account account, AccompanyComment accompanyComment) {
        accompanyComment.setAccompany(accompany);
        accompanyComment.setAccount(account);
        accompanyComment.setRegDate(LocalDateTime.now());
        return accompanyCommentRepository.save(accompanyComment);
    }

    public Page<AccompanyComment> findAllByAccompany(Accompany accompany, Pageable pageable) {
        return accompanyCommentRepository.findAllByAccompany(accompany, pageable);
    }

    public AccompanyComment updateComment(AccompanyComment accompanyComment, AccompanyCommentDto accompanyCommentDto) {
        accompanyComment.setComment(accompanyCommentDto.getComment());//수정된 댓글을 comment에 set
        return accompanyCommentRepository.save(accompanyComment);
    }

    @Transactional
    public void delete(AccompanyComment accompanyComment) {
        accompanyChildCommentRepository.deleteAllByAccompanyComment(accompanyComment);//대댓글 삭제
        accompanyCommentRepository.delete(accompanyComment);//댓글 삭제
    }
}
