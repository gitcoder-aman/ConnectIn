package com.tech.connectIn.connectionService.controller;

import com.tech.connectIn.connectionService.entity.Person;
import com.tech.connectIn.connectionService.service.ConnectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/core")
@RequiredArgsConstructor
@Slf4j
public class ConnectionController {

    private final ConnectionService connectionService;

    @GetMapping("/{userId}/first-degree")
    public ResponseEntity<List<Person>>getFirstDegreeConnection(@PathVariable Long userId, @RequestHeader("X-User-Id") Long userIdFromHeader){
        log.info("User id is {}",userIdFromHeader);

        List<Person> personList = connectionService.getFirstDegreeConnectionsOfUser(userId);
        return ResponseEntity.ok(personList);
    }
}
