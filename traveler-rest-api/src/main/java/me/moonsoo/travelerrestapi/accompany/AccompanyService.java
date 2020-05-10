package me.moonsoo.travelerrestapi.accompany;

import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.travelerrestapi.accompany.comment.Comment;
import me.moonsoo.travelerrestapi.accompany.comment.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AccompanyService {

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    AccompanyRepository accompanyRepository;

    //동행 게시물 저장
    public Accompany save(Accompany accompany, Account account) {
        accompany.setAccount(account);//작성자 set
        accompany.setViewCount(0);//조회수 set
        accompany.setRegDate(LocalDateTime.now());//게시물 생성 시간 set
        return accompanyRepository.save(accompany);
    }

    public Accompany save(Accompany accompany) {
        return accompanyRepository.save(accompany);
    }

    //페이징, 검색어 조건에 따른 동행 게시물 return
    public Page<Accompany> findAccompanies(Pageable pageable, String filter, String search) {
        //검색어와 필터 중 하나라도 유효하지 않은 경우 필터링을 하지 않고 목록 출력
        if (filter == null || filter.isBlank() || search == null || search.isBlank()) {
            return accompanyRepository.findAll(pageable);
        }
        //필터링 조건이 작성자인 경우
        else if (filter.equals("writer")) {
            return accompanyRepository.findAllByAccount_NicknameContains(search, pageable);
        }
        //필터링 조건이 게시물의 제목인 경우
        else if (filter.equals("title")) {
            return accompanyRepository.findAllByTitleContains(search, pageable);
        }
        //필터링 조건이 게시물의 본문인 경우
        else if (filter.equals("article")) {
            return accompanyRepository.findAllByArticleContains(search, pageable);
        }
        //필터링 조건이 장소명인 경우
        else {//filter.equals("location")
            return accompanyRepository.findAllByLocationContains(search, pageable);
        }
    }

    @Transactional
    public void delete(Accompany accompany) {
        List<Comment> comments = commentRepository.findAllByAccompany(accompany);

        if(comments.size() != 0) {
            commentRepository.deleteAllByAccompany(accompany);
        }
        accompanyRepository.delete(accompany);
    }

    //동행 게시물 조회시 해당 게시물 조회수를 1 증가
    public Accompany updateViewCount(Accompany accompany) {
        accompany.setViewCount(accompany.getViewCount() + 1);//조회수 1증가
        return accompanyRepository.save(accompany);
    }
}
