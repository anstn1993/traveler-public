package me.moonsoo.travelerrestapi.index;


import me.moonsoo.travelerrestapi.accompany.AccompanyController;
import me.moonsoo.travelerrestapi.properties.AppProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
public class IndexController {

    @Autowired
    private AppProperties appProperties;

    @GetMapping("/api")
    public RepresentationModel index() {
        RepresentationModel representationModel =new RepresentationModel();
        representationModel.add(new Link(appProperties.getBaseUrl() + appProperties.getProfileUri()).withRel("profile"));
        return representationModel;
    }
}
