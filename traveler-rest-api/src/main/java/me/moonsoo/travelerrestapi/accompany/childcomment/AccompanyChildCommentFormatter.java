package me.moonsoo.travelerrestapi.accompany.childcomment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AccompanyChildCommentFormatter implements Converter<String, AccompanyChildComment> {

    @Autowired
    AccompanyChildCommentRepository accompanyChildCommentRepository;

    @Override
    public AccompanyChildComment convert(String childCommentIdStr) {
        Integer childCommentId = Integer.parseInt(childCommentIdStr);
        Optional<AccompanyChildComment> childCommentOpt = accompanyChildCommentRepository.findById(childCommentId);
        if(childCommentOpt.isEmpty()) {
            return null;
        }
        return childCommentOpt.get();
    }
}
