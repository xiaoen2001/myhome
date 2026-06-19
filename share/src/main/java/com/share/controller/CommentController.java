package com.share.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.share.common.Result;
import com.share.entity.Comment;
import com.share.service.ICommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final ICommentService commentService;

    @GetMapping
    public Result<List<Comment>> getComments(@RequestParam Long noteId) {
        List<Comment> list = commentService.list(new LambdaQueryWrapper<Comment>().eq(Comment::getNoteId, noteId).orderByDesc(Comment::getCreatedAt));
        return Result.success(list);
    }

    @PostMapping
    public Result<Comment> addComment(@RequestAttribute Long userId, @RequestBody Comment comment) {
        return commentService.addComment(userId, comment);
    }
}