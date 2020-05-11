package me.moonsoo.travelerrestapi.accompany.comment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AccompanyCommentFormatter implements Converter<String, AccompanyComment> {

    @Autowired
    AccompanyCommentRepository accompanyCommentRepository;

    @Override
    public AccompanyComment convert(String commentIdStr) {
        Integer commentId = Integer.parseInt(commentIdStr);
        Optional<AccompanyComment> commentOtp = accompanyCommentRepository.findById(commentId);
        if(commentOtp.isEmpty()) {
            return null;
        }

        return commentOtp.get();
    }
}
