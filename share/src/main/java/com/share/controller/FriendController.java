package com.share.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.share.common.Result;
import com.share.entity.Friend;
import com.share.entity.FriendRequest;
import com.share.entity.User;
import com.share.service.IFriendRequestService;
import com.share.service.IFriendService;
import com.share.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FriendController {

    private final IFriendService friendService;
    private final IFriendRequestService friendRequestService;
    private final IUserService userService;

    @GetMapping("/friends")
    public Result<List<Friend>> getFriends(@RequestAttribute Long userId) {
        List<Friend> list = friendService.list(new LambdaQueryWrapper<Friend>().eq(Friend::getUserId, userId));
        return Result.success(list);
    }

    @DeleteMapping("/friends/{friendId}")
    public Result<Void> deleteFriend(@RequestAttribute Long userId, @PathVariable Long friendId) {
        return friendService.deleteFriend(userId, friendId);
    }

    @GetMapping("/friend-requests")
    public Result<List<FriendRequest>> getFriendRequests(@RequestAttribute Long userId) {
        return friendRequestService.getFriendRequests(userId);
    }

    @PostMapping("/friend-requests")
    public Result<Void> sendFriendRequest(@RequestAttribute Long userId, @RequestBody FriendRequest request) {
        return friendRequestService.sendFriendRequest(userId, request.getToUserId());
    }

    @PutMapping("/friend-requests/{id}")
    public Result<Void> handleFriendRequest(@PathVariable Long id,
                                            @RequestBody Map<String, String> body,
                                            @RequestAttribute Long userId) {
        return friendRequestService.handleFriendRequest(id, body.get("action"), userId);
    }

    @GetMapping("/users/search")
    public Result<List<User>> searchUsers(@RequestParam String keyword) {
        return userService.searchUsers(keyword);
    }

    @GetMapping("/users/recommend")
    public Result<List<User>> recommendUsers(@RequestAttribute Long userId) {
        return userService.recommendFriends(userId);
    }

    @GetMapping("/users/{targetUserId}/friend-status")
    public Result<Boolean> checkFriendStatus(@RequestAttribute Long userId, @PathVariable Long targetUserId) {
        return friendService.checkFriendStatus(userId, targetUserId);
    }
}