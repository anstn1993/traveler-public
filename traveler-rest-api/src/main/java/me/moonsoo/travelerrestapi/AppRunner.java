package me.moonsoo.travelerrestapi;

import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.commonmodule.account.AccountRepository;
import me.moonsoo.commonmodule.account.AccountRole;
import me.moonsoo.commonmodule.account.Sex;
import me.moonsoo.travelerrestapi.accompany.Accompany;
import me.moonsoo.travelerrestapi.accompany.AccompanyRepository;
import me.moonsoo.travelerrestapi.accompany.childcomment.AccompanyChildComment;
import me.moonsoo.travelerrestapi.accompany.childcomment.AccompanyChildCommentRepository;
import me.moonsoo.travelerrestapi.accompany.comment.AccompanyComment;
import me.moonsoo.travelerrestapi.accompany.comment.AccompanyCommentRepository;
import me.moonsoo.travelerrestapi.follow.FollowRepository;
import me.moonsoo.travelerrestapi.post.PostRepository;
import me.moonsoo.travelerrestapi.post.childcomment.PostChildCommentRepository;
import me.moonsoo.travelerrestapi.post.comment.PostCommentRepository;
import me.moonsoo.travelerrestapi.schedule.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.IntStream;

//@Component
//@Component
public class AppRunner implements ApplicationRunner {
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private FollowRepository followRepository;
    @Autowired
    private AccompanyRepository accompanyRepository;
    @Autowired
    private AccompanyCommentRepository accompanyCommentRepository;
    @Autowired
    private AccompanyChildCommentRepository accompanyChildCommentRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private PostCommentRepository postCommentRepository;
    @Autowired
    private PostChildCommentRepository postChildCommentRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private ScheduleLocationRepository scheduleLocationRepository;
    @Autowired
    private ScheduleDetailRepository scheduleDetailRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Account account = Account.builder()
                .username("testuser")
                .authCode("authCode")
                .email("testuser@email.com")
                .password(passwordEncoder.encode("11111111"))
                .emailAuth(true)
                .name("김이름")
                .roles(Set.of(AccountRole.USER))
                .nickname("testuser")
                .profileImageUri(null)
                .introduce("반가워요 테스트 계정 입니다.")
                .regDate(ZonedDateTime.now())
                .sex(Sex.MALE)
                .build();
        Account savedAccount = accountRepository.save(account);
        //동행 게시물 추가
        IntStream.rangeClosed(1, 100).forEach(i -> {
            Accompany accompany = createAccompany(savedAccount, i);
            //동행 댓글 추가
            IntStream.rangeClosed(1, 5).forEach(j -> {
                AccompanyComment accompanyComment = createAccompanyComment(savedAccount, accompany, j);
                //동행 대댓글 추가
                IntStream.rangeClosed(1, 3).forEach(k -> {
                    createAccompanyChildComment(savedAccount, accompany, accompanyComment, k);
                });
            });
        });
        //일정 게시물 추가
        IntStream.rangeClosed(1, 100).forEach(i -> {
            Schedule schedule = createSchedule(savedAccount, i);
        });

    }

    private Schedule createSchedule(Account account, int i) {

        Schedule schedule = Schedule.builder()
                .account(account)
                .regDate(ZonedDateTime.now())
                .scope(Scope.ALL)
                .title("일정 게시물" + i)
                .viewCount(0)
                .build();
        Schedule savedSchedule = scheduleRepository.save(schedule);
        Set<ScheduleLocation> scheduleLocations = new LinkedHashSet<>();
        IntStream.rangeClosed(1, 3).forEach(k -> {
            //schedule location 생성
            ScheduleLocation savedScheduleLocation = createScheduleLocation(savedSchedule, k);
            scheduleLocations.add(savedScheduleLocation);
            //schedule detail 생성
            Set<ScheduleDetail> scheduleDetails = new LinkedHashSet<>();
            IntStream.rangeClosed(1, 3).forEach(p -> {
                ScheduleDetail savedScheduleDetail = createScheduleDetail(savedScheduleLocation, p);
                scheduleDetails.add(savedScheduleDetail);
            });
            savedScheduleLocation.setScheduleDetails(scheduleDetails);
        });
        savedSchedule.setScheduleLocations(scheduleLocations);
        return savedSchedule;
    }

    private ScheduleLocation createScheduleLocation(Schedule savedSchedule, int k) {
        ScheduleLocation scheduleLocation = ScheduleLocation.builder()
                .schedule(savedSchedule)
                .latitude(33.0000)
                .longitude(120.0000)
                .location("위치" + k)
                .build();
        ScheduleLocation savedScheduleLocation = scheduleLocationRepository.save(scheduleLocation);
        return savedScheduleLocation;
    }


    private ScheduleDetail createScheduleDetail(ScheduleLocation savedScheduleLocation, int p) {
        ScheduleDetail scheduleDetail = ScheduleDetail.builder()
                .place("장소" + p)
                .plan("장소" + p +"에서 관광하기")
                .scheduleLocation(savedScheduleLocation)
                .startDate(LocalDateTime.of(2020, 8, 10, 13, 0, 0))
                .endDate(LocalDateTime.of(2020, 8, 11, 13, 0, 0))
                .build();
        ScheduleDetail savedScheduleDetail = scheduleDetailRepository.save(scheduleDetail);
        return savedScheduleDetail;
    }

    private AccompanyChildComment createAccompanyChildComment(Account account, Accompany accompany, AccompanyComment accompanyComment, int k) {
        AccompanyChildComment accompanyChildComment = AccompanyChildComment.builder()
                .accompany(accompany)
                .account(account)
                .comment("대댓글"+ k +" 입니다.")
                .accompanyComment(accompanyComment)
                .regDate(ZonedDateTime.now())
                .build();
        return accompanyChildCommentRepository.save(accompanyChildComment);
    }

    private Accompany createAccompany(Account account, int index) {
        Accompany accompany = Accompany.builder()
                .account(account)
                .article("동행 함께 하실 분 구합니다")
                .title("동행 게시물" + index)
                .viewCount(0)
                .regDate(ZonedDateTime.now())
                .startDate(LocalDateTime.of(2020, 8, 20, 10, 0, 0))
                .endDate(LocalDateTime.of(2020, 8, 21, 10, 0, 0))
                .latitude(22.11111)
                .longitude(102.11111)
                .location("location" + index)
                .build();
        return accompanyRepository.save(accompany);
    }

    private AccompanyComment createAccompanyComment(Account account, Accompany accompany, int index) {
        AccompanyComment comment = AccompanyComment.builder()
                .accompany(accompany)
                .account(account)
                .comment(index + "번째 댓글 입니다.")
                .regDate(ZonedDateTime.now())
                .build();
        return accompanyCommentRepository.save(comment);
    }

//    private Follow createFollow(Account followingAccount, Account followedAccount) {
//        Follow follow = Follow.builder()
//                .followingAccount(followingAccount)
//                .followedAccount(followedAccount)
//                .build();
//        return followRepository.save(follow);
//    }

    private Account createAccount(int index) {
        Account account = Account.builder()
                .username("user" + index)
                .authCode("authCode")
                .email("user" + index + "@email.com")
                .password(passwordEncoder.encode("11111111"))
                .emailAuth(true)
                .name("김이름")
                .roles(Set.of(AccountRole.USER))
                .nickname("user" + index)
                .profileImageUri(null)
                .introduce(null)
                .regDate(ZonedDateTime.now())
                .sex(Sex.MALE)
                .build();
        return accountRepository.save(account);
    }
}
