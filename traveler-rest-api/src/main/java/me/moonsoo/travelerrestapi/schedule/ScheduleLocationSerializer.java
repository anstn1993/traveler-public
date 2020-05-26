package me.moonsoo.travelerrestapi.schedule;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class ScheduleLocationSerializer extends JsonSerializer<ScheduleLocation> {
    @Override
    public void serialize(ScheduleLocation scheduleLocation, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("id", scheduleLocation.getId());
        jsonGenerator.writeEndObject();
    }
}
