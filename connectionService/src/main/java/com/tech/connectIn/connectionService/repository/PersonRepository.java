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

    @Query("""
            MATCH (p:Person)
            RETURN p
            """)
    List<Person> findAllPersons();

    @Query("""
            MATCH (p:Person {userId: $userId})-[:REQUESTED_TO]->(receiver:Person)
            RETURN receiver
            """)
    List<Person> sentRequests(@Param("userId") Long userId);

    @Query("""
            MATCH (sender:Person)-[:REQUESTED_TO]->(p:Person {userId: $userId})
            RETURN sender
            """)
    List<Person> receivedRequests(@Param("userId") Long userId);

    @Query("""
            MATCH (p1:Person) - [r:REQUESTED_TO]->(p2:Person) WHERE p1.userId = $senderId AND p2.userId = $receiverId
            RETURN count(r) > 0
            """)
    boolean connectionRequestExists(Long senderId, Long receiverId);

    @Query("""
            MATCH (p1:Person) - [r:CONNECTED_TO]->(p2:Person) WHERE p1.userId = $senderId AND p2.userId = $receiverId
            RETURN count(r) > 0
            """)
    boolean alreadyConnected(Long senderId, Long receiverId);

    @Query("""
            MATCH (p1:Person),(p2:Person) WHERE p1.userId = $senderId AND p2.userId = $receiverId
            CREATE (p1)-[:REQUESTED_TO]->(p2)
            """)
    void addConnectionRequest(Long senderId, Long receiverId);

    @Query("""
            MATCH (p1:Person) - [r:REQUESTED_TO]-> (p2:Person) WHERE p1.userId = $senderId AND p2.userId = $receiverId
            DELETE r CREATE (p1)-[:CONNECTED_TO]->(p2)
            """)
    void acceptConnectionRequest(Long senderId, Long receiverId);

    @Query("""
            MATCH (p1:Person) - [r:REQUESTED_TO]-> (p2:Person) WHERE p1.userId = $senderId AND p2.userId = $receiverId
            DELETE r
            """)
    void rejectConnectionRequest(Long senderId, Long receiverId);

    @Query("""
            MATCH (p1:Person)-[r:CONNECTED_TO]-(p2:Person)
            WHERE p1.userId = $senderId AND p2.userId = $receiverId
            DELETE r
            """)
    void removeConnection(Long senderId, Long receiverId);


}
