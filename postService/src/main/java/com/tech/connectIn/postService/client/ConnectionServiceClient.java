package com.tech.connectIn.postService.client;

import com.tech.connectIn.postService.dto.PersonDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "connections-service",path = "/connections/core")
public interface ConnectionServiceClient {

    @GetMapping("/{userId}/first-degree")
    List<PersonDto>getFirstDegreeConnection(@PathVariable Long userId);
}
