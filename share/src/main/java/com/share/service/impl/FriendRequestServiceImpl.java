package com.share.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.share.common.Result;
import com.share.entity.Friend;
import com.share.entity.FriendRequest;
import com.share.entity.User;
import com.share.mapper.FriendRequestMapper;
import com.share.service.IFriendRequestService;
import com.share.service.IFriendService;
import com.share.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FriendRequestServiceImpl extends ServiceImpl<FriendRequestMapper, FriendRequest> implements IFriendRequestService {

    @Autowired
    @Lazy
    private IFriendService friendService;
    @Autowired
    private IUserService userService;

    @Override
    public Result<List<FriendRequest>> getFriendRequests(Long userId) {
        List<FriendRequest> list = this.list(new LambdaQueryWrapper<FriendRequest>()
                .eq(FriendRequest::getToUserId, userId)
                .eq(FriendRequest::getStatus, "pending"));
        return Result.success(list);
    }

    @Override
    public Result<Void> sendFriendRequest(Long userId, Long toUserId) {
        FriendRequest existing = this.getOne(new LambdaQueryWrapper<FriendRequest>()
                .eq(FriendRequest::getFromUserId, userId)
                .eq(FriendRequest::getToUserId, toUserId));
        if (existing != null) {
            return Result.error(400, "已发送过申请");
        }
        User user = userService.getById(userId);
        FriendRequest request = new FriendRequest();
        if (user != null) {
            request.setFromUserAvatar(user.getAvatar());
            request.setFromUserNickname(user.getNickname());
        } else {
            request.setFromUserAvatar(null);
            request.setFromUserNickname("未知用户");
        }
        request.setFromUserId(userId);
        request.setToUserId(toUserId);
        request.setStatus("pending");
        this.save(request);
        return Result.success();
    }

    @Override
    public Result<Void> handleFriendRequest(Long id, String action, Long currentUserId) {
        if ((!"agree".equals(action) && !"reject".equals(action))) {
            return Result.error(400, "无效的操作");
        }
        FriendRequest friendRequest = this.getById(id);
        if (friendRequest == null) {
            return Result.error(404, "申请不存在");
        }
        if (!friendRequest.getToUserId().equals(currentUserId)) {
            return Result.error(403, "无权操作");
        }
        if ("agree".equals(action)) {
            boolean alreadyFriend1 = friendService.lambdaQuery()
                    .eq(Friend::getUserId, currentUserId)
                    .eq(Friend::getFriendId, friendRequest.getFromUserId()).exists();
            if (!alreadyFriend1) {
                Friend friend1 = new Friend();
                friend1.setUserId(currentUserId);
                friend1.setFriendId(friendRequest.getFromUserId());
                friend1.setFriendNickname(friendRequest.getFromUserNickname());
                friend1.setFriendAvatar(friendRequest.getFromUserAvatar());
                friendService.save(friend1);
            }
            boolean alreadyFriend2 = friendService.lambdaQuery()
                    .eq(Friend::getUserId, friendRequest.getFromUserId())
                    .eq(Friend::getFriendId, currentUserId).exists();
            if (!alreadyFriend2) {
                Friend friend2 = new Friend();
                friend2.setUserId(friendRequest.getFromUserId());
                friend2.setFriendId(currentUserId);
                User currentUser = userService.getById(currentUserId);
                friend2.setFriendNickname(currentUser.getNickname());
                friend2.setFriendAvatar(currentUser.getAvatar());
                friendService.save(friend2);
            }
            friendRequest.setStatus("accepted");
        } else {
            friendRequest.setStatus("rejected");
        }
        this.updateById(friendRequest);
        return Result.success();
    }
}