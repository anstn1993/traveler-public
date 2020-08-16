package me.moonsoo.travelerrestapi.follow;

import me.moonsoo.commonmodule.account.Account;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class FollowService {

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private ModelMapper modelMapper;

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

    public Page<Account> findAllFollowingAccounts(Account followedAccount, Pageable pageable) {
        Page<Follow> follows = followRepository.findByFollowedAccount(followedAccount, pageable);
        Page<Account> followingAccounts = follows.map(follow -> follow.getFollowingAccount());
        return followingAccounts;
    }

    public void delete(Follow follow) {
        followRepository.delete(follow);
    }

    public Map<String, Integer> getFollowResourceCount(Account targetAccount) {
        Map<String, Integer> followResourceCount = new HashMap<>();
        Integer followingCount = followRepository.countFollowByFollowingAccount(targetAccount);
        Integer followerCount = followRepository.countFollowByFollowedAccount(targetAccount);
        followResourceCount.put("followingCount", followingCount);
        followResourceCount.put("followerCount", followerCount);
        return followResourceCount;
    }
}
