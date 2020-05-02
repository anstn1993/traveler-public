package me.moonsoo.travelerrestapi.accompany;

import lombok.extern.slf4j.Slf4j;
import me.moonsoo.commonmodule.account.AccountAdapter;
import me.moonsoo.commonmodule.oauth.OAuthAccessToken;
import me.moonsoo.commonmodule.oauth.OAuthAccessTokenRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.net.URI;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping(value = "/api/accompanies")
@Slf4j
public class AccompanyController {

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    AccompanyRepository accompanyRepository;

    @Autowired
    OAuthAccessTokenRepository accessTokenRepository;

    @PostMapping
    public ResponseEntity createAccompany(@RequestBody @Valid AccompanyDto accompanyDto,
                                          Errors errors,
                                          @AuthenticationPrincipal AccountAdapter accountAdapter) {
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }
        log.info(accountAdapter.getUsername());
        Accompany accompany = modelMapper.map(accompanyDto, Accompany.class);
        accompany.setAccount(accountAdapter.getAccount());
        accompany.setRegDate(LocalDateTime.now());
        Accompany savedAccompany = accompanyRepository.save(accompany);
        URI uri = linkTo(AccompanyController.class).slash(savedAccompany.getId()).toUri();
        return ResponseEntity.created(uri).body(savedAccompany);
    }
}
