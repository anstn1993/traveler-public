package me.moonsoo.travelerrestapi.errors;

import me.moonsoo.travelerrestapi.index.IndexController;
import org.codehaus.jackson.annotate.JsonUnwrapped;
import org.springframework.hateoas.EntityModel;
import org.springframework.validation.Errors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class ErrorsModel extends EntityModel<Errors> {
    public ErrorsModel(Errors errors) {
        super(errors);
        add(linkTo(methodOn(IndexController.class).index()).withRel("index"));
    }
}
