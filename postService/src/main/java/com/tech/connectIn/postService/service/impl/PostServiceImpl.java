package com.tech.connectIn.postService.service.impl;

import com.tech.connectIn.postService.dto.PostCreateRequestDto;
import com.tech.connectIn.postService.dto.PostDto;
import com.tech.connectIn.postService.entity.Post;
import com.tech.connectIn.postService.exception.ResourceNotFoundException;
import com.tech.connectIn.postService.repository.PostRepository;
import com.tech.connectIn.postService.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final ModelMapper modelMapper;

    @Override
    public PostDto createPost(PostCreateRequestDto postCreateRequestDto, Long userId) {

        log.info("Creating post for user with id:{}", userId);
        Post post = modelMapper.map(postCreateRequestDto, Post.class);
        post.setUserId(userId);
        post = postRepository.save(post);
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
