package com.share.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.share.entity.Message;
import com.share.service.IMessageService;
import com.share.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final JwtUtil jwtUtil;
    private final IMessageService messageService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final NotificationWebSocketHandler notificationWebSocketHandler;
    // 存储用户ID与WebSocketSession的映射，用于点对点发送
    private static final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public ChatWebSocketHandler(JwtUtil jwtUtil, IMessageService messageService, NotificationWebSocketHandler notificationWebSocketHandler) {
        this.jwtUtil = jwtUtil;
        this.messageService = messageService;
        this.notificationWebSocketHandler = notificationWebSocketHandler;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            sessions.put(userId, session);
            log.info("聊天 WebSocket 用户 {} 已连接", userId);
        } else {
            session.close(CloseStatus.NOT_ACCEPTABLE);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long senderId = getUserIdFromSession(session);
        if (senderId == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        // 解析前端发送的 JSON：{ receiverId: xxx, type: "text", content: "xxx" }
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Long receiverId = Long.valueOf(payload.get("receiverId").toString());
        String type = (String) payload.get("type");
        String content = (String) payload.get("content");

        // 保存消息到数据库
        Message msg = new Message();
        msg.setSenderId(senderId);
        msg.setReceiverId(receiverId);
        msg.setType(type);
        msg.setContent(content);
        msg.setCreatedAt(LocalDateTime.now());
        messageService.save(msg);

        // 构造要返回的响应数据（包含服务器生成的 id 和 createdAt）
        Map<String, Object> response = Map.of(
                "id", msg.getId(),
                "senderId", senderId,
                "receiverId", receiverId,
                "type", type,
                "content", content,
                "createdAt", msg.getCreatedAt().toString()
        );
        String responseJson = objectMapper.writeValueAsString(response);

        // 1. 发送给接收方（如果在线）
        WebSocketSession receiverSession = sessions.get(receiverId);
        if (receiverSession != null && receiverSession.isOpen()) {
            receiverSession.sendMessage(new TextMessage(responseJson));
        }else {
            notificationWebSocketHandler.sendToUser(receiverId, "信息通知","有好友发送信息了!");
        }

        // 2. 发送给发送方（确认回显）
        WebSocketSession senderSession = sessions.get(senderId);
        if (senderSession != null && senderSession.isOpen()) {
            senderSession.sendMessage(new TextMessage(responseJson));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            sessions.remove(userId);
            log.info("聊天 WebSocket 用户 {} 断开", userId);
        }
    }

    private Long getUserIdFromSession(WebSocketSession session) {
        String query = session.getUri().getQuery();
        if (query != null && query.contains("token=")) {
            String token = query.split("token=")[1];
            if (jwtUtil.validateToken(token)) {
                return jwtUtil.getUserIdFromToken(token);
            }
        }
        return null;
    }
}