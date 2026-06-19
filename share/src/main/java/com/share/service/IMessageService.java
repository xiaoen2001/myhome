package com.share.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.share.common.Result;
import com.share.entity.Message;
import com.share.vo.MessageVO;

import java.util.List;

public interface IMessageService extends IService<Message> {
    Result<List<Message>> getMessages(Long friendId, Long userId);
    Result<Message> sendMessage(Long userId, Message message);
    Result<List<MessageVO>> getRecentMessages(Long userId);
}