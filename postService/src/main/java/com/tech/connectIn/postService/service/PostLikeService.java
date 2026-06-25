package com.tech.connectIn.postService.service;

public interface PostLikeService {

    void likePost(Long postId);

    void unlikePost(Long postId);
}
