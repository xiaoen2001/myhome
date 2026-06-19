package com.share.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.share.entity.Notification;
import com.share.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private final JwtUtil jwtUtil;
    private static final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper(); // 用于 JSON 转换

    public NotificationWebSocketHandler(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String query = session.getUri().getQuery();
        Long userId = null;
        if (query != null && query.contains("token=")) {
            String token = query.split("token=")[1];
            if (jwtUtil.validateToken(token)) {
                userId = jwtUtil.getUserIdFromToken(token);
            }
        }
        if (userId != null) {
            sessions.put(userId, session);
            log.info("用户 {} 已连接 WebSocket", userId);
        } else {
            session.close(CloseStatus.NOT_ACCEPTABLE);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.values().remove(session);
        log.info("WebSocket 连接关闭");
    }

    /**
     * 发送给指定用户（个人通知）
     * @param userId 接收方用户ID
     * @param title 通知标题
     * @param content 通知内容
     */
    public void sendToUser(Long userId, String title, String content) {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                Map<String, Object> payload = new HashMap<>();
                payload.put("title", title);
                payload.put("content", content);
                String json = objectMapper.writeValueAsString(payload);
                session.sendMessage(new TextMessage(json));
                log.info("发送通知给用户 {}: {} - {}", userId, title, content);
            } catch (Exception e) {
                log.error("发送通知给用户 {} 失败", userId, e);
            }
        } else {
            log.debug("用户 {} 不在线，通知暂存（可后续扩展离线通知表）", userId);
        }
    }

    public void broadcast(Notification notification) {
        for (WebSocketSession session : sessions.values()) {
            try {
                Map<String, Object> payload = new HashMap<>();
                payload.put("title", notification.getTitle());
                payload.put("content", notification.getContent());
                payload.put("type", notification.getType());
                payload.put("detailId", notification.getDetailId());
                String json = objectMapper.writeValueAsString(payload);
                session.sendMessage(new TextMessage(json));
            } catch (Exception e) {
                log.error("广播失败", e);
            }
        }
    }
}