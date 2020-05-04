package me.moonsoo.travelerrestapi.accompany;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

public class AccompanyModel extends EntityModel<Accompany> {
    public AccompanyModel(Accompany accompany, Link... links) {
        super(accompany, links);
        add(linkTo(AccompanyController.class).slash(accompany.getId()).withSelfRel());
    }
}
