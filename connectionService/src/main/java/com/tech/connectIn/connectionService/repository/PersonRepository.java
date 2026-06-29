package com.tech.connectIn.connectionService.repository;

import com.tech.connectIn.connectionService.entity.Person;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PersonRepository extends Neo4jRepository<Person, Long> {

    Optional<Person> findByUserId(Long userId);

    @Query("""
            MATCH (p:Person {userId: $userId})-[:CONNECTED_TO]->(friend:Person)
            RETURN friend
            """)
    List<Person> firstDegreeConnection(@Param("userId") Long userId);
}
