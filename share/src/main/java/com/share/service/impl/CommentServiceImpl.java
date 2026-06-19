package com.share.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.share.common.BusinessException;
import com.share.common.Result;
import com.share.common.ResultCode;
import com.share.config.NotificationWebSocketHandler;
import com.share.entity.Comment;
import com.share.entity.Note;
import com.share.entity.User;
import com.share.mapper.CommentMapper;
import com.share.service.ICommentService;
import com.share.service.INoteService;
import com.share.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

    @Autowired
    @Lazy
    private INoteService noteService;
    @Autowired
    private IUserService userService;
    @Autowired
    private NotificationWebSocketHandler notificationWebSocketHandler;

    @Override
    public Result<Comment> addComment(Long userId, Comment comment) {
        User user = userService.getById(userId);
        comment.setUserId(userId);
        comment.setUsername(user.getUsername());
        comment.setUserAvatar(user.getAvatar());
        comment.setUserNickname(user.getNickname());
        this.save(comment);
        noteService.incrementCommentCount(comment.getNoteId());

        Note note = noteService.getById(comment.getNoteId());
        if (note != null && note.getUserId() != null && !note.getUserId().equals(userId)) {
            notificationWebSocketHandler.sendToUser(note.getUserId(), "系统", "有人评论了您的笔记！");
        }
        return Result.success(comment);
    }

    @Override
    public Result<Page<Map<String, Object>>> adminListComments(Integer page, Integer pageSize, String keyword, String username, String noteTitle) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Comment::getContent, keyword);
        }
        if (username != null && !username.isEmpty()) {
            wrapper.like(Comment::getUsername, username);
        }
        if (noteTitle != null && !noteTitle.isEmpty()) {
            LambdaQueryWrapper<Note> noteWrapper = new LambdaQueryWrapper<>();
            noteWrapper.like(Note::getTitle, noteTitle);
            List<Long> noteIds = noteService.list(noteWrapper).stream().map(Note::getId).toList();
            if (!noteIds.isEmpty()) {
                wrapper.in(Comment::getNoteId, noteIds);
            } else {
                wrapper.eq(Comment::getNoteId, -1L);
            }
        }
        wrapper.orderByDesc(Comment::getCreatedAt);
        Page<Comment> pageResult = this.page(new Page<>(page, pageSize), wrapper);
        Page<Map<String, Object>> result = new Page<>();
        result.setCurrent(pageResult.getCurrent());
        result.setSize(pageResult.getSize());
        result.setTotal(pageResult.getTotal());
        result.setRecords(pageResult.getRecords().stream().map(comment -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", comment.getId());
            map.put("content", comment.getContent());
            map.put("userId", comment.getUserId());
            map.put("username", comment.getUsername());
            map.put("userNickname", comment.getUserNickname());
            map.put("userAvatar", comment.getUserAvatar());
            map.put("createdAt", comment.getCreatedAt());
            Note note = noteService.getById(comment.getNoteId());
            map.put("noteTitle", note != null ? note.getTitle() : "未知笔记");
            return map;
        }).toList());
        return Result.success(result);
    }

    @Override
    public Result<Void> adminDeleteComment(Long id) {
        Comment comment = this.getById(id);
        if (comment == null) throw new BusinessException(ResultCode.NOT_FOUND);
        Note note = noteService.getById(comment.getNoteId());
        if (note == null) throw new BusinessException(ResultCode.NOT_FOUND);
        note.setCommentCount(note.getCommentCount() - 1);
        noteService.updateById(note);
        this.removeById(id);

        // 发送个人通知给评论作者
        if (comment.getUserId() != null) {
            notificationWebSocketHandler.sendToUser(comment.getUserId(), "系统", "您的评论已被管理员删除：" + comment.getContent());
        }

        return Result.success();
    }
}