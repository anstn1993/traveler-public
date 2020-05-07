package me.moonsoo.travelerrestapi.accompany;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Optional;

//path variable로 동행 게시물 id를 Accompany객체로 변환해서 핸들러 argument로 반환
@Component
public class AccompanyFormatter implements Converter<String, Accompany> {

    @Autowired
    AccompanyRepository accompanyRepository;

    @Override
    public Accompany convert(String accompanyIdStr) {
        Integer accompanyId = Integer.parseInt(accompanyIdStr);
        Optional<Accompany> accompanyOtp = accompanyRepository.findById(accompanyId);
        if(accompanyOtp.isEmpty()) {
            return null;
        }
        return accompanyOtp.get();
    }
}
