package com.share.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.share.common.Result;
import com.share.entity.Comment;

import java.util.Map;

public interface ICommentService extends IService<Comment> {
    Result<Comment> addComment(Long userId, Comment comment);
    Result<Page<Map<String, Object>>> adminListComments(Integer page, Integer pageSize, String keyword, String username, String noteTitle);
    Result<Void> adminDeleteComment(Long id);
}