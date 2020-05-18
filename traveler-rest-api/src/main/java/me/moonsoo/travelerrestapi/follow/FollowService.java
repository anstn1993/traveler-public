package me.moonsoo.travelerrestapi.follow;

import me.moonsoo.commonmodule.account.Account;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FollowService {

    @Autowired
    FollowRepository followRepository;

    @Autowired
    ModelMapper modelMapper;

    public Follow save(FollowDto followDto, Account account) {
        Follow follow = modelMapper.map(followDto, Follow.class);
        follow.setFollowingAccount(account);
        return followRepository.save(follow);
    }

    public Optional<Follow> getFollow(Account followingAccount, Account followedAccount) {
        return followRepository.findByFollowingAccountAndAndFollowedAccount(followingAccount, followedAccount);
    }

    public Page<Account> findAllFollowedAccounts(Account followingAccount, Pageable pageable) {
        Page<Follow> follows = followRepository.findByFollowingAccount(followingAccount, pageable);
        Page<Account> followedAccounts = follows.map(follow -> follow.getFollowedAccount());
        return followedAccounts;
    }
}
