package me.moonsoo.travelerrestapi.accompany;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

public class AccompanySerializer extends JsonSerializer<Accompany> {
    @Override
    public void serialize(Accompany accompany, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("id", accompany.getId());
        jsonGenerator.writeEndObject();
    }
}
