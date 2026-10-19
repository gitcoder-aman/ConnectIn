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

    @GetMapping("/all")
    public ResponseEntity<List<Person>>getAllPersons(){
        List<Person> personList = connectionService.getAllPersons();
        return ResponseEntity.ok(personList);
    }

    @GetMapping("/requests/sent")
    public ResponseEntity<List<Person>>getSentRequests(){
        List<Person> personList = connectionService.getSentRequests();
        return ResponseEntity.ok(personList);
    }

    @GetMapping("/requests/received")
    public ResponseEntity<List<Person>>getReceivedRequests(){
        List<Person> personList = connectionService.getReceivedRequests();
        return ResponseEntity.ok(personList);
    }

    @PostMapping("/request/{userId}")
    public ResponseEntity<Void> sendConnectionRequest(@PathVariable Long userId){
        connectionService.sendConnectionRequest(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/accept/{senderId}")
    public ResponseEntity<Void> acceptConnectionRequest(@PathVariable Long senderId){
        connectionService.acceptConnectionRequest(senderId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reject/{senderId}")
    public ResponseEntity<Void> rejectConnectionRequest(@PathVariable Long senderId){
        connectionService.rejectConnectionRequest(senderId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/remove/{userId}")
    public ResponseEntity<Void> removeConnection(@PathVariable Long userId){
        connectionService.removeConnection(userId);
        return ResponseEntity.noContent().build();
    }
}
