package com.tech.connectIn.connectionService.service;

import com.tech.connectIn.connectionService.entity.Person;

import java.util.List;


public interface ConnectionService {

    List<Person> getFirstDegreeConnectionsOfUser(Long userId);

}
