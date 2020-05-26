package me.moonsoo.travelerrestapi.schedule;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;

public class ScheduleSerializer extends JsonSerializer<Schedule> {
    @Override
    public void serialize(Schedule schedule, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("id", schedule.getId());
        jsonGenerator.writeEndObject();
    }
}
