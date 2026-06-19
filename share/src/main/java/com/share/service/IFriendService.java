package com.share.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.share.common.Result;
import com.share.entity.Friend;

public interface IFriendService extends IService<Friend> {
    boolean areFriends(Long userId, Long friendId);

    Result<Void> deleteFriend(Long userId, Long friendId);

    Result<Boolean> checkFriendStatus(Long userId, Long targetUserId);
}