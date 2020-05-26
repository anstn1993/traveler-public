package me.moonsoo.travelerrestapi.schedule;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

public class ScheduleModel extends EntityModel<Schedule> {
    public ScheduleModel(Schedule schedule, Link... links) {
        super(schedule, links);
        add(linkTo(ScheduleController.class).slash(schedule.getId()).withSelfRel());//self링크 추가
    }
}
