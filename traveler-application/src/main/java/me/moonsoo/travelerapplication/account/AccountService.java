package me.moonsoo.travelerapplication.account;

import me.moonsoo.commonmodule.account.Account;
import me.moonsoo.commonmodule.account.AccountAdapter;
import me.moonsoo.commonmodule.account.AccountRepository;
import me.moonsoo.commonmodule.account.EmailAuthException;
import me.moonsoo.travelerapplication.properties.S3Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Null;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class AccountService implements UserDetailsService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private FileUploader fileUploader;

    @Autowired
    private S3Properties s3Properties;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Account> accountOpt = accountRepository.findByUsername(username);
        Account account = accountOpt.orElseThrow(() -> new UsernameNotFoundException(username));
        if(!account.isEmailAuth()) {
            throw new EmailAuthException("You need to authenticate your email.");
        }
        return new AccountAdapter(account);
    }

    public Optional<Account> findByNameAndEmail(String name, String email) {
        return accountRepository.findByNameAndEmail(name, email);
    }

    public Optional<Account> findByUsernameAndEmail(String username, String email) {
        return accountRepository.findByUsernameAndEmail(username, email);
    }

    //비밀번호 수정
    public void updatePassword(String username, String password) {
        Optional<Account> accountOpt = accountRepository.findByUsername(username);
        Account account = accountOpt.get();
        account.setPassword(passwordEncoder.encode(password));
        accountRepository.save(account);
    }

    //사용자 프로필 수정
    public Account update(Account targetAccount, MultipartFile imageFile) throws IOException, IllegalArgumentException {
        try {
            if (targetAccount.getProfileImageUri() != null) {//기존 프로필 사진이 존재하는 경우
                //s3서버에서 프로필 이미지 제거
                fileUploader.deleteProfileImage(targetAccount.getProfileImageUri());
                targetAccount.setProfileImageUri(null);
            }

            if (imageFile != null) {//요청 part에 이미지 파일이 넘어온 경우
                //s3서버에 프로필 이미지 추가
                String uploadedImageUri = fileUploader.upload(imageFile, targetAccount, s3Properties.getProfileImageDirectory());
                targetAccount.setProfileImageUri(uploadedImageUri);
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

    public Optional<Account> findByUsername(String username) {
        return accountRepository.findByUsername(username);
    }

    public Optional<Account> findByEmail(String email) {
        return accountRepository.findByEmail(email);
    }
    public Optional<Account> findByNickname(String nickname) {
        return accountRepository.findByNickname(nickname);
    }

    public Optional<Account> findById(Integer userId) {
        return accountRepository.findById(userId);
    }
}
