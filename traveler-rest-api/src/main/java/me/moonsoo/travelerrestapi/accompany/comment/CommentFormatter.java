package me.moonsoo.travelerrestapi.accompany.comment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CommentFormatter implements Converter<String, Comment> {

    @Autowired
    CommentRepository commentRepository;

    @Override
    public Comment convert(String commentIdStr) {
        Integer commentId = Integer.parseInt(commentIdStr);
        Optional<Comment> commentOtp = commentRepository.findById(commentId);
        if(commentOtp.isEmpty()) {
            return null;
        }

        return commentOtp.get();
    }
}
