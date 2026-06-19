package com.share.controller;

import com.share.common.Result;
import com.share.entity.Message;
import com.share.service.IMessageService;
import com.share.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final IMessageService messageService;

    @GetMapping
    public Result<List<Message>> getMessages(@RequestParam Long friendId, @RequestAttribute Long userId) {
        return messageService.getMessages(friendId, userId);
    }

    @PostMapping
    public Result<Message> sendMessage(@RequestAttribute Long userId, @RequestBody Message message) {
        return messageService.sendMessage(userId, message);
    }

    @GetMapping("/recent")
    public Result<List<MessageVO>> getRecentMessages(@RequestAttribute Long userId) {
        return messageService.getRecentMessages(userId);
    }
}