package me.moonsoo.travelerrestapi.schedule;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.core.TypeReferences;

import java.io.IOException;

/*
 *이 serializer는 ScheduleWithoutLocations의 목록 조회 핸들러에서 PagedModel을 serializing하기 위해서 사용된다.
 */

/*
*json serializer의 타입을 PagedModel로 설정했더니 다른 엔티티들의 목록 조회 시에도 paged model을 serializing하는 과정에서 이 serializer가 사용되어서 에러가 발생했다.
*그 문제를 해결하기 위해서 따로 PagedModel<ScheduleWithoutLocationsModel>을 상속받는 ScheduleWithoutLocationsPagedModel을 따로 만들어주고 @JsonComponent의 attribute중 type을 설정해줘서
* 그 타입을 serializing할 때만 사용되게끔 설정해줬다. ScheduleController의 목록 조회 핸들러 코드도 참조해볼 것.
*/
@JsonComponent(type = ScheduleWithoutLocationsPagedModel.class)
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
