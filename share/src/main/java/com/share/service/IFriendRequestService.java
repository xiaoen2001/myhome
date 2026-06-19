package com.share.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.share.common.Result;
import com.share.entity.FriendRequest;

import java.util.List;

public interface IFriendRequestService extends IService<FriendRequest> {
    Result<List<FriendRequest>> getFriendRequests(Long userId);
    Result<Void> sendFriendRequest(Long userId, Long toUserId);
    Result<Void> handleFriendRequest(Long id, String action, Long currentUserId);
}