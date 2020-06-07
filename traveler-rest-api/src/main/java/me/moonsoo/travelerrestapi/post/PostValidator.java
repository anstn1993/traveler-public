package me.moonsoo.travelerrestapi.post;

import org.springframework.validation.Errors;

public class PostValidator {

    public void validate(PostDto postDto, Errors errors) {
        if((postDto.getLocation() == null || postDto.getLocation().isBlank()) && postDto.getLatitude() == null && postDto.getLongitude() == null) {
            return;
        }

        if((postDto.getLocation() != null && !postDto.getLocation().isBlank()) && postDto.getLatitude() != null && postDto.getLongitude() != null) {
            return;
        }

        errors.reject("location", "You have to specify all location data(location, latitude, longitude).");
    }

}
