package me.moonsoo.commonmodule.account;

import com.fasterxml.jackson.databind.JsonSerializer;

import java.io.IOException;

//response body에서 사용자 정보 중 id만 나가게 하기 위한 serializer
public class AccountSerializer extends JsonSerializer<Account> {
    @Override
    public void serialize(Account account, com.fasterxml.jackson.core.JsonGenerator jsonGenerator, com.fasterxml.jackson.databind.SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("id", account.getId());
        jsonGenerator.writeStringField("nickname", account.getNickname());
        jsonGenerator.writeStringField("profileImageUri", account.getProfileImageUri());
        jsonGenerator.writeEndObject();
    }
}
