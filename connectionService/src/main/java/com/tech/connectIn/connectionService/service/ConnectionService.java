package com.tech.connectIn.connectionService.service;

import com.tech.connectIn.connectionService.entity.Person;

import java.util.List;


public interface ConnectionService {

    List<Person> getFirstDegreeConnectionsOfUser(Long userId);

    void sendConnectionRequest(Long receiverId);

    void acceptConnectionRequest(Long senderId);

    void rejectConnectionRequest(Long senderId);

}
