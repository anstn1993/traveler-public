package me.moonsoo.travelerrestapi.schedule;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;

import java.util.Collection;

/*
*이 클래스는 ScheduleController의 목록 조회 핸들러에서 사용된다. ScheduleWithoutLocationsSerializer의 JsonSerializer 제네릭 타입을 PagedModel로 설정했더니 다른 모든 엔티티들의 목록 조회 핸들러에서도
* ScheduleWithoutLocationsSerializer를 사용해서 serializing을 해버리는 문제가 생겨서 따로 상속 받는 클래스를 설정해주고 @JsonComponent의 type을 이 클래스로 설정해서 이 클래스를 사용할 경우에만
* ScheduleWithoutLocationsSerializer를 사용하게 하기 위해서 만들었다.
*/
public class ScheduleWithoutLocationsPagedModel extends PagedModel<ScheduleWithoutLocationsModel> {
    public ScheduleWithoutLocationsPagedModel(Collection<ScheduleWithoutLocationsModel> content, PageMetadata metadata, Iterable<Link> links) {
        super(content, metadata, links);
    }
}
