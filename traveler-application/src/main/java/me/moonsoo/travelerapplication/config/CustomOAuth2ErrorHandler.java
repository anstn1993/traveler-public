package me.moonsoo.travelerapplication.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

@Slf4j
public class CustomOAuth2ErrorHandler implements ResponseErrorHandler {


    @Override
    public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
        if(clientHttpResponse.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR) {
            log.info(clientHttpResponse.getStatusText());
        }

        return false;
    }

    @Override
    public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {
        if(clientHttpResponse.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR) {
            log.info(clientHttpResponse.getStatusText());
        }
    }
}
