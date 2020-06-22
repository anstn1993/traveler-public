package me.moonsoo.travelerrestapi.account;

import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.commonmodule.account.AccountRepository;
import me.moonsoo.commonmodule.account.AccountRole;
import me.moonsoo.travelerrestapi.accompany.AccompanyRepository;
import me.moonsoo.travelerrestapi.accompany.childcomment.AccompanyChildCommentRepository;
import me.moonsoo.travelerrestapi.accompany.comment.AccompanyCommentRepository;
import me.moonsoo.travelerrestapi.email.EmailService;
import me.moonsoo.travelerrestapi.follow.FollowRepository;
import me.moonsoo.travelerrestapi.post.FileUploader;
import me.moonsoo.travelerrestapi.post.PostRepository;
import me.moonsoo.travelerrestapi.properties.S3Properties;
import me.moonsoo.travelerrestapi.schedule.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class AccountService {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AccompanyRepository accompanyRepository;

    @Autowired
    AccompanyCommentRepository accompanyCommentRepository;

    @Autowired
    AccompanyChildCommentRepository accompanyChildCommentRepository;

    @Autowired
    ScheduleRepository scheduleRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    FollowRepository followRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    S3Properties s3Properties;

    @Autowired
    FileUploader fileUploader;


    //사용자 추가
    public Account save(Account account, List<MultipartFile> imageFile) throws IOException, IllegalArgumentException {
        account.setPassword(passwordEncoder.encode(account.getPassword()));//비밀번호 암호화
        account.setEmailAuth(false);//이메일 인증 여부
        account.setAuthCode(passwordEncoder.encode(account.getEmail()));//이메일 인증 코드
        account.setRegDate(LocalDateTime.now());
        account.setProfileImageUri(null);
        account.setRoles(Set.of(AccountRole.USER));
        Account savedAccount = accountRepository.save(account);
        if (!imageFile.isEmpty()) {//프로필 이미지가 존재하는 경우 s3서버에 저장
            try {
                List<String> uploadedProfileImageUri = fileUploader.upload(imageFile, savedAccount, s3Properties.getProfileImageDirectory());
                savedAccount.setProfileImageUri(uploadedProfileImageUri.get(0));
                return savedAccount;
            } catch (IOException e) {
                e.printStackTrace();
                accountRepository.delete(savedAccount);
                throw new IOException(e.getMessage());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                accountRepository.delete(savedAccount);
                throw new IllegalArgumentException(e.getMessage());
            }
        }
        return savedAccount;
    }

    //이메일 인증 상태로 update
    public void updateEmailAuth(Account account) {
        account.setEmailAuth(true);
        accountRepository.save(account);
    }

    //페이징, 검색어 조건에 따른 사용자 목록 return
    public Page<Account> findAccounts(Pageable pageable, String filter, String search) {
        if (filter == null || filter.isBlank() || search == null || search.isBlank()) {
            return accountRepository.findAllByEmailAuthIsTrue(pageable);
        } else if (filter.equals("name")) {
            return accountRepository.findAllByEmailAuthIsTrueAndNameContains(pageable, search);
        } else {//filter.equals("nickname")
            return accountRepository.findAllByEmailAuthIsTrueAndNicknameContains(pageable, search);
        }
    }

    //사용자 리소스 수정
    public Account update(Account targetAccount, List<MultipartFile> imageFile) throws IOException, IllegalArgumentException {
        try {
            //비밀번호 암호화
            targetAccount.setPassword(passwordEncoder.encode(targetAccount.getPassword()));

            if (targetAccount.getProfileImageUri() != null) {//기존 프로필 사진이 존재하는 경우
                //s3서버에서 프로필 이미지 제거
                fileUploader.deleteProfileImage(targetAccount.getProfileImageUri());
                targetAccount.setProfileImageUri(null);
            }

            if (!imageFile.isEmpty()) {//요청 part에 이미지 파일이 넘어온 경우
                //s3서버에 프로필 이미지 추가
                List<String> uploadedImageUriList = fileUploader.upload(imageFile, targetAccount, s3Properties.getProfileImageDirectory());
                targetAccount.setProfileImageUri(uploadedImageUriList.get(0));
            }

            return accountRepository.save(targetAccount);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    //사용자 삭제
    @Transactional
    public void delete(Account account) {
        if (account.getProfileImageUri() != null) {
            fileUploader.deleteProfileImage(account.getProfileImageUri());//s3서버에서 프로필 이미지 제거
        }

        //사용자가 생성한 모든 리소스 제거
        accompanyChildCommentRepository.deleteByAccount(account);
        accompanyCommentRepository.deleteByAccount(account);
        accompanyRepository.deleteByAccount(account);
        scheduleRepository.deleteByAccount(account);
        postRepository.deleteByAccount(account);
        followRepository.deleteByFollowingAccount(account);
        followRepository.deleteByFollowedAccount(account);
        accountRepository.delete(account);
    }
}
