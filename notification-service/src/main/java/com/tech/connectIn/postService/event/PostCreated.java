package com.tech.connectIn.postService.event;

import lombok.Data;

@Data
public class PostCreated {

    private Long ownerUserId;
    private Long postId;
    private Long userId;
    private String content;
}
