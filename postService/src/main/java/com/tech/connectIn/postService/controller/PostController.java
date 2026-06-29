package com.tech.connectIn.postService.controller;

import com.tech.connectIn.postService.auth.AuthContextHolder;
import com.tech.connectIn.postService.dto.PostCreateRequestDto;
import com.tech.connectIn.postService.dto.PostDto;
import com.tech.connectIn.postService.service.PostService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/core")
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostDto>createPost(@RequestBody PostCreateRequestDto postCreateRequestDto){
        PostDto postDto = postService.createPost(postCreateRequestDto,1L);
        return new ResponseEntity<>(postDto, HttpStatus.CREATED);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostDto>getPost(@PathVariable Long postId){

        Long userId = AuthContextHolder.getCurrentUserId();

        PostDto postDto = postService.getPostById(postId);
        return ResponseEntity.ok(postDto);
    }

    @GetMapping("/users/{userId}/all-posts")
    public ResponseEntity<List<PostDto>>getAllPosts(@PathVariable Long userId){
        List<PostDto> allPostsByUser = postService.getAllPostsByUser(userId);
        return ResponseEntity.ok(allPostsByUser);
    }

}
