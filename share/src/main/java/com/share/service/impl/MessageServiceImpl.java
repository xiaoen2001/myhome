package com.share.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.share.common.Result;
import com.share.entity.Message;
import com.share.entity.User;
import com.share.mapper.MessageMapper;
import com.share.service.IMessageService;
import com.share.service.IUserService;
import com.share.vo.MessageVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements IMessageService {

    @Autowired
    private IUserService userService;

    @Override
    public Result<Message> sendMessage(Long userId, Message message) {
        message.setSenderId(userId);
        this.save(message);
        return Result.success(message);
    }

    @Override
    public Result<List<Message>> getMessages(Long friendId, Long userId) {
        List<Message> list = this.list(new LambdaQueryWrapper<Message>()
                .and(w -> w.eq(Message::getSenderId, userId).eq(Message::getReceiverId, friendId)
                        .or()
                        .eq(Message::getSenderId, friendId).eq(Message::getReceiverId, userId))
                .orderByAsc(Message::getCreatedAt));
        return Result.success(list);
    }

    @Override
    public Result<List<MessageVO>> getRecentMessages(Long userId) {
        List<Message> allMessages = this.list(new LambdaQueryWrapper<Message>()
                .and(wrapper -> wrapper
                        .eq(Message::getSenderId, userId)
                        .or()
                        .eq(Message::getReceiverId, userId))
                .orderByDesc(Message::getCreatedAt));

        if (allMessages.isEmpty()) {
            return Result.success(new ArrayList<>());
        }
        Map<Long, Message> latestMap = new HashMap<>();
        for (Message msg : allMessages) {
            Long friendId = msg.getSenderId().equals(userId) ? msg.getReceiverId() : msg.getSenderId();
            if (!latestMap.containsKey(friendId)) {
                latestMap.put(friendId, msg);
            }
        }
        Set<Long> friendIds = latestMap.keySet();
        Map<Long, User> userMap = userService.listByIds(friendIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<MessageVO> res = new ArrayList<>();
        for (Map.Entry<Long, Message> entry : latestMap.entrySet()) {
            Long friendId = entry.getKey();
            Message msg = entry.getValue();
            MessageVO vo = new MessageVO();
            BeanUtils.copyProperties(msg, vo);
            User friend = userMap.get(friendId);
            if (friend != null) {
                vo.setNickname(friend.getNickname());
                vo.setAvatar(friend.getAvatar());
            } else {
                vo.setNickname("未知用户");
                vo.setAvatar(null);
            }
            res.add(vo);
        }
        res.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        return Result.success(res);
    }
}