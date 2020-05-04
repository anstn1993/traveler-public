package me.moonsoo.travelerrestapi.index;


import me.moonsoo.travelerrestapi.accompany.AccompanyController;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
public class IndexController {
    @GetMapping("/api")
    public RepresentationModel index() {
        RepresentationModel representationModel =new RepresentationModel();
        representationModel.add(linkTo(AccompanyController.class).withRel("accompanies"));
        return representationModel;
    }
}
