package me.moonsoo.travelerrestapi.follow;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import me.moonsoo.commonmodule.account.Account;

import java.io.IOException;

public class FollowAccountSerializer extends JsonSerializer<Account> {

    @Override
    public void serialize(Account account, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("id", account.getId());
        jsonGenerator.writeStringField("email", account.getEmail());
        jsonGenerator.writeStringField("name", account.getName());
        jsonGenerator.writeStringField("nickname", account.getNickname());
        jsonGenerator.writeStringField("profileImagePath", account.getProfileImagePath());
        jsonGenerator.writeStringField("sex", account.getSex().name());
        jsonGenerator.writeEndObject();
    }
}