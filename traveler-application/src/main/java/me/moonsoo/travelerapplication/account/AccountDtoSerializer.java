package me.moonsoo.travelerapplication.account;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
//{"username":"anstn1993","password":"11111111","email":"anstn1993@gmail.com","name":"user","nickname":"user","sex":"MALE"}
@JsonComponent
public class AccountDtoSerializer extends JsonSerializer<AccountDto> {
    @Override
    public void serialize(AccountDto accountDto, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("username", accountDto.getUsername());
        jsonGenerator.writeStringField("password", accountDto.getPassword());
        jsonGenerator.writeStringField("email", accountDto.getEmail());
        jsonGenerator.writeStringField("name", accountDto.getName());
        jsonGenerator.writeStringField("nickname", accountDto.getNickname());
        jsonGenerator.writeStringField("sex", accountDto.getSex().name());
        jsonGenerator.writeEndObject();
    }
}
