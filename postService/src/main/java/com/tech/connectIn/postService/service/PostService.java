package com.tech.connectIn.postService.service;

import com.tech.connectIn.postService.dto.PostCreateRequestDto;
import com.tech.connectIn.postService.dto.PostDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PostService {
    PostDto createPost(PostCreateRequestDto postCreateRequestDto, MultipartFile file);

    PostDto getPostById(Long postId);

    List<PostDto> getAllPostsByUser(Long userId);
}
