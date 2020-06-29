package me.moonsoo.travelerrestapi.post.comment;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class PostCommentSerializer extends JsonSerializer<PostComment> {
    @Override
    public void serialize(PostComment postComment, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("id", postComment.getId());
        jsonGenerator.writeEndObject();
    }
}
