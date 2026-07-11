package com.tech.connectIn.connectionService.service.impl;

import com.tech.connectIn.connectionService.entity.Person;
import com.tech.connectIn.connectionService.repository.PersonRepository;
import com.tech.connectIn.connectionService.service.PersonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;

    @Override
    public void createPerson(Long userId, String name) {
        Person person = Person.builder()
                .name(name)
                .userId(userId)
                .build();
        personRepository.save(person);
    }
}
