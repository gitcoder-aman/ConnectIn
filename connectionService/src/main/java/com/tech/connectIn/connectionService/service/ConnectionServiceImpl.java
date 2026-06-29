package com.tech.connectIn.connectionService.service;

import com.tech.connectIn.connectionService.entity.Person;
import com.tech.connectIn.connectionService.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConnectionServiceImpl implements ConnectionService {

    private final PersonRepository personRepository;

    @Override
    public List<Person> getFirstDegreeConnectionsOfUser(Long userId){
        log.info("Getting first degree connections of user with ID:{}",userId);

        return personRepository.firstDegreeConnection(userId);
    }
}
