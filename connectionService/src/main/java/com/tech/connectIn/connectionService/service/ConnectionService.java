package com.tech.connectIn.connectionService.service;

import com.tech.connectIn.connectionService.entity.Person;

import java.util.List;


public interface ConnectionService {

    List<Person> getFirstDegreeConnectionsOfUser(Long userId);

    List<Person> getAllPersons();

    List<Person> getSentRequests();

    List<Person> getReceivedRequests();

    void sendConnectionRequest(Long receiverId);

    void acceptConnectionRequest(Long senderId);

    void rejectConnectionRequest(Long senderId);

    void removeConnection(Long userId);

}
