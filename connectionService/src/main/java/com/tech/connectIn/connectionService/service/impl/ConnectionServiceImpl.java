package com.tech.connectIn.connectionService.service.impl;

import com.tech.connectIn.connectionService.auth.AuthContextHolder;
import com.tech.connectIn.connectionService.entity.Person;
import com.tech.connectIn.connectionService.exception.BadRequestException;
import com.tech.connectIn.connectionService.repository.PersonRepository;
import com.tech.connectIn.connectionService.service.ConnectionService;
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
        Long senderId = AuthContextHolder.getCurrentUserId();
        log.info("sender Id ={}",senderId);
        return personRepository.firstDegreeConnection(userId);
    }

    @Override
    public List<Person> getAllPersons(){
        log.info("Getting all persons");
        return personRepository.findAllPersons();
    }

    @Override
    public List<Person> getSentRequests(){
        Long userId = AuthContextHolder.getCurrentUserId();
        log.info("Getting sent connection requests of user with ID:{}",userId);
        return personRepository.sentRequests(userId);
    }

    @Override
    public List<Person> getReceivedRequests(){
        Long userId = AuthContextHolder.getCurrentUserId();
        log.info("Getting received connection requests of user with ID:{}",userId);
        return personRepository.receivedRequests(userId);
    }

    @Override
    public void sendConnectionRequest(Long receiverId) {
        Long senderId = AuthContextHolder.getCurrentUserId();
        log.info("sending connection request with senderId:{}, receiverId:{}",senderId,receiverId);

        if(senderId.equals(receiverId)){
            throw new BadRequestException("Both sender and receiver are the same.");
        }
        boolean alreadySentRequest = personRepository.connectionRequestExists(senderId,receiverId);

        if(alreadySentRequest){
            throw new BadRequestException("Connection Request already exists, can not send again");
        }
        boolean alreadyConnected = personRepository.alreadyConnected(senderId,receiverId);
        if(alreadyConnected){
            throw new BadRequestException("Already connected users, cannot add connection request");
        }

        log.info("successfully sent the connection request");
        personRepository.addConnectionRequest(senderId,receiverId);
    }

    @Override
    public void acceptConnectionRequest(Long senderId) {

        Long receiverId = AuthContextHolder.getCurrentUserId();
        log.info("accepting connection request with senderId:{}, receiverId:{}",senderId,receiverId);

        if(senderId.equals(receiverId)){
            throw new BadRequestException("Both sender and receiver are the same.");
        }
        boolean alreadyConnected = personRepository.alreadyConnected(senderId,receiverId);
        if(alreadyConnected){
            throw new BadRequestException("Already connected users, cannot accept connection request again.");
        }
        boolean alreadySentRequest = personRepository.connectionRequestExists(senderId,receiverId);

        if(!alreadySentRequest){
            throw new BadRequestException("No connection request exists, cannot accept without request");
        }
        personRepository.acceptConnectionRequest(senderId,receiverId);
        log.info("Successfully accepted the connection request with senderId:{}, receiverId:{}",senderId,receiverId);
    }

    @Override
    public void rejectConnectionRequest(Long senderId) {

        Long receiverId = AuthContextHolder.getCurrentUserId();
        log.info("Rejecting connection request with senderId:{}, receiverId:{}", senderId, receiverId);

        if (senderId.equals(receiverId)) {
            throw new BadRequestException("Both sender and receiver are the same.");
        }
        boolean alreadySentRequest = personRepository.connectionRequestExists(senderId,receiverId);

        if(!alreadySentRequest){
            throw new BadRequestException("No connection request exists, cannot reject it.");
        }
        personRepository.rejectConnectionRequest(senderId,receiverId);
        log.info("Successfully rejecting the connection request with senderId:{}, receiverId:{}",senderId,receiverId);
    }

    @Override
    public void removeConnection(Long userId) {

        Long currentUserId = AuthContextHolder.getCurrentUserId();
        log.info("Removing connection between currentUserId:{} and userId:{}", currentUserId, userId);

        if(currentUserId.equals(userId)){
            throw new BadRequestException("Both sender and receiver are the same.");
        }
        boolean connected = personRepository.alreadyConnected(currentUserId, userId)
                || personRepository.alreadyConnected(userId, currentUserId);
        if(!connected){
            throw new BadRequestException("The users are not connected, cannot remove connection.");
        }
        personRepository.removeConnection(currentUserId, userId);
        log.info("Successfully removed the connection between currentUserId:{} and userId:{}", currentUserId, userId);
    }


}
