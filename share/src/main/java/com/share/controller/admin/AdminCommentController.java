package com.share.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.share.common.BusinessException;
import com.share.common.Result;
import com.share.common.ResultCode;
import com.share.service.ICommentService;
import com.share.service.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {

    private final ICommentService commentService;
    private final IUserService userService;

    private void checkAdmin(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) throw new BusinessException(ResultCode.UNAUTHORIZED);
        String role = userService.getById(userId).getRole();
        if (!"admin".equals(role)) throw new BusinessException(ResultCode.FORBIDDEN);
    }

    @GetMapping
    public Result<Page<Map<String, Object>>> listComments(@RequestParam(defaultValue = "1") Integer page,
                                                          @RequestParam(defaultValue = "10") Integer pageSize,
                                                          @RequestParam(required = false) String keyword,
                                                          @RequestParam(required = false) String username,
                                                          @RequestParam(required = false) String noteTitle,
                                                          HttpServletRequest request) {
        checkAdmin(request);
        return commentService.adminListComments(page, pageSize, keyword, username, noteTitle);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(@PathVariable Long id, HttpServletRequest request) {
        checkAdmin(request);
        return commentService.adminDeleteComment(id);
    }
}