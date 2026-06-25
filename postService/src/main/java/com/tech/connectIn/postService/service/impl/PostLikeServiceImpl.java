package com.tech.connectIn.postService.service.impl;

import com.tech.connectIn.postService.entity.Post;
import com.tech.connectIn.postService.entity.PostLike;
import com.tech.connectIn.postService.exception.BadRequestException;
import com.tech.connectIn.postService.exception.ResourceNotFoundException;
import com.tech.connectIn.postService.repository.PostLikeRepository;
import com.tech.connectIn.postService.repository.PostRepository;
import com.tech.connectIn.postService.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostLikeServiceImpl implements PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final ModelMapper modelMapper;

    @Transactional
    @Override
    public void likePost(Long postId) {
        Long userId = 1L;
        log.info("User with ID:{} liking the post with ID: {}",userId,postId);
        postRepository.findById(postId).orElseThrow(()->new ResourceNotFoundException("Post not found with ID:"+postId));

        boolean hasAlreadyLiked = postLikeRepository.existsByUserIdAndPostId(userId,postId);
        if(hasAlreadyLiked){
            throw new BadRequestException("You can not like the Post again");
        }
        PostLike postLike = new PostLike();
        postLike.setUserId(userId);
        postLike.setPostId(postId);
        postLikeRepository.save(postLike);

        //TODO:send notification to the owner of the post
    }

    @Transactional
    @Override
    public void unlikePost(Long postId) {
        Long userId = 1L;
        log.info("User with ID:{} unliking the post with ID: {}",userId,postId);
        postRepository.findById(postId).orElseThrow(()->new ResourceNotFoundException("Post not found with ID:"+postId));

        boolean hasAlreadyLiked = postLikeRepository.existsByUserIdAndPostId(userId,postId);
        if(!hasAlreadyLiked){
            throw new BadRequestException("You can not unlike the Post that you have not liked.");
        }

        postLikeRepository.deleteByUserIdAndPostId(userId,postId);
    }
}
