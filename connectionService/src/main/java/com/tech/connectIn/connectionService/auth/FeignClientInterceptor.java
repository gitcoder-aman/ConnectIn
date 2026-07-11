package com.tech.connectIn.connectionService.auth;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FeignClientInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        Long userId = AuthContextHolder.getCurrentUserId();

        System.out.println("Feign sending userId = " + userId);
        log.info("Feign sending userId = {}",userId);
        if(userId != null){
            requestTemplate.header("X-User-Id",userId.toString());
        }
    }
}
