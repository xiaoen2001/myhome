package com.share.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.share.common.Result;
import com.share.entity.Friend;
import com.share.mapper.FriendMapper;
import com.share.service.IFriendService;
import org.springframework.stereotype.Service;

@Service
public class FriendServiceImpl extends ServiceImpl<FriendMapper, Friend> implements IFriendService {
    @Override
    public boolean areFriends(Long userId, Long friendId) {
        return this.count(new LambdaQueryWrapper<Friend>().eq(Friend::getUserId, userId).eq(Friend::getFriendId, friendId)) > 0;
    }

    @Override
    public Result<Boolean> checkFriendStatus(Long userId, Long targetUserId) {
        boolean isFriend = this.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Friend>().eq(Friend::getUserId, userId).eq(Friend::getFriendId, targetUserId)) > 0;
        return Result.success(isFriend);
    }

    @Override
    public Result<Void> deleteFriend(Long userId, Long friendId) {
        this.remove(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Friend>().eq(Friend::getUserId, userId).eq(Friend::getFriendId, friendId));
        // 同时删除对方的好友关系
        this.remove(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Friend>().eq(Friend::getUserId, friendId).eq(Friend::getFriendId, userId));
        return Result.success();
    }
}