package me.moonsoo.travelerrestapi.schedule;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.PagedModel;

import java.io.IOException;

@JsonComponent
public class ScheduleWithoutLocationsSerializer extends JsonSerializer<PagedModel<ScheduleWithoutLocationsModel>> {

    @Override
    public void serialize(PagedModel<ScheduleWithoutLocationsModel> scheduleWithoutLocationsModels, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        if (!scheduleWithoutLocationsModels.getContent().isEmpty()) {
            jsonGenerator.writeObjectFieldStart("_embedded");
            jsonGenerator.writeArrayFieldStart("scheduleList");
            scheduleWithoutLocationsModels.getContent().forEach(s -> {
                try {
                    jsonGenerator.writeObject(s);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            jsonGenerator.writeEndArray();
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeObjectFieldStart("_links");
        scheduleWithoutLocationsModels.getLinks().forEach(l -> {
            try {
                jsonGenerator.writeObjectFieldStart(l.getRel().value());
                jsonGenerator.writeStringField("href", l.getHref());
                jsonGenerator.writeEndObject();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        jsonGenerator.writeEndObject();
        jsonGenerator.writeObjectFieldStart("page");
        jsonGenerator.writeNumberField("size", scheduleWithoutLocationsModels.getMetadata().getSize());
        jsonGenerator.writeNumberField("totalElements", scheduleWithoutLocationsModels.getMetadata().getTotalElements());
        jsonGenerator.writeNumberField("totalPages", scheduleWithoutLocationsModels.getMetadata().getTotalPages());
        jsonGenerator.writeNumberField("number", scheduleWithoutLocationsModels.getMetadata().getNumber());
        jsonGenerator.writeEndObject();
        jsonGenerator.writeEndObject();
    }
}
