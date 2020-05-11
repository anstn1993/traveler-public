package me.moonsoo.travelerrestapi.accompany.comment;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class AccompanyCommentSerializer extends JsonSerializer<AccompanyComment> {
    @Override
    public void serialize(AccompanyComment accompanyComment, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("id", accompanyComment.getId());
        jsonGenerator.writeEndObject();
    }
}
