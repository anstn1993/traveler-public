package me.moonsoo.travelerrestapi.follow;

import me.moonsoo.commonmodule.account.Account;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
