package me.moonsoo.travelerrestapi.accompany.comment;

import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.travelerrestapi.accompany.Accompany;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CommentService {
    @Autowired
    CommentRepository commentRepository;

    //동행 게시물에 댓글 추가
    public Comment save(Accompany accompany, Account account, Comment comment) {
        comment.setAccompany(accompany);
        comment.setAccount(account);
        comment.setRegDate(LocalDateTime.now());
        return commentRepository.save(comment);
    }

    public Page<Comment> findAllByAccompany(Accompany accompany, Pageable pageable) {
        return commentRepository.findAllByAccompany(accompany, pageable);
    }

    public Comment updateComment(Comment comment, CommentDto commentDto) {
        comment.setComment(commentDto.getComment());//수정된 댓글을 comment에 set
        return commentRepository.save(comment);
    }

    public void delete(Comment comment) {
        commentRepository.delete(comment);
    }
}
