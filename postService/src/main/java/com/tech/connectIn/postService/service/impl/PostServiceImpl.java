package com.tech.connectIn.postService.service.impl;

import com.tech.connectIn.postService.auth.AuthContextHolder;
import com.tech.connectIn.postService.client.ConnectionServiceClient;
import com.tech.connectIn.postService.client.UploaderServiceClient;
import com.tech.connectIn.postService.dto.PersonDto;
import com.tech.connectIn.postService.dto.PostCreateRequestDto;
import com.tech.connectIn.postService.dto.PostDto;
import com.tech.connectIn.postService.entity.Post;
import com.tech.connectIn.postService.event.PostCreated;
import com.tech.connectIn.postService.exception.ResourceNotFoundException;
import com.tech.connectIn.postService.repository.PostRepository;
import com.tech.connectIn.postService.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final ModelMapper modelMapper;
    private final ConnectionServiceClient connectionServiceClient;
    private final KafkaTemplate<Long, PostCreated> postCreatedKafkaTemplate;
    private final UploaderServiceClient uploaderServiceClient;

    @Override
    public PostDto createPost(PostCreateRequestDto postCreateRequestDto, MultipartFile file) {

        Long userId = AuthContextHolder.getCurrentUserId();
        log.info("Creating post for user with id:{}", userId);

        String imageUrl = uploaderServiceClient.uploadFile(file);

        log.info("image Url :{}",imageUrl);

        Post post = modelMapper.map(postCreateRequestDto, Post.class);
        post.setUserId(userId);
        post.setImageUrl(imageUrl);
        post = postRepository.save(post);

        List<PersonDto> personDtoList = connectionServiceClient.getFirstDegreeConnection(userId);

        for(PersonDto person : personDtoList){  // send notification to each connection

            PostCreated postCreated = PostCreated.builder()
                    .postId(post.getId())
                    .content(post.getContent())
                    .userId(person.getUserId())
                    .ownerUserId(userId)
                    .build();
            log.info("post_created_kafka{}",postCreated);
            postCreatedKafkaTemplate.send("post_created_topic",postCreated);
        }


        return modelMapper.map(post, PostDto.class);
    }

    @Override
    public PostDto getPostById(Long postId) {
        log.info("Getting post by id:{}", postId);

        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post not found with ID: " + postId));
        return modelMapper.map(post, PostDto.class);
    }

    @Override
    public List<PostDto> getAllPostsByUser(Long userId) {
        log.info("Getting all the posts of a user with ID:{} ",userId);
        List<Post> postList = postRepository.findByUserId(userId);
        return  postList.stream()
                .map((post)->modelMapper.map(post, PostDto.class))
                .collect(Collectors.toList());
    }
}
