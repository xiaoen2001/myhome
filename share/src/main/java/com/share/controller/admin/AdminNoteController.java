package com.share.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.share.common.BusinessException;
import com.share.common.Result;
import com.share.common.ResultCode;
import com.share.entity.Note;
import com.share.service.INoteService;
import com.share.service.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/notes")
@RequiredArgsConstructor
public class AdminNoteController {

    private final INoteService noteService;
    private final IUserService userService;

    private void checkAdmin(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) throw new BusinessException(ResultCode.UNAUTHORIZED);
        String role = userService.getById(userId).getRole();
        if (!"admin".equals(role)) throw new BusinessException(ResultCode.FORBIDDEN);
    }

    @GetMapping
    public Result<Page<Map<String, Object>>> listNotes(@RequestParam(defaultValue = "1") Integer page,
                                                       @RequestParam(defaultValue = "10") Integer pageSize,
                                                       @RequestParam(required = false) String keyword,
                                                       @RequestParam(required = false) String status,
                                                       HttpServletRequest request) {
        checkAdmin(request);
        return noteService.adminListNotes(page, pageSize, keyword, status);
    }

    @GetMapping("/{id}")
    public Result<Note> getNoteDetail(@PathVariable Long id, HttpServletRequest request) {
        checkAdmin(request);
        Note note = noteService.getById(id);
        if (note == null) throw new BusinessException(ResultCode.NOT_FOUND);
        return Result.success(note);
    }

    @PutMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id, HttpServletRequest request) {
        checkAdmin(request);
        return noteService.approveNote(id);
    }

    @PutMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @RequestBody Map<String, String> body, HttpServletRequest request) {
        checkAdmin(request);
        return noteService.rejectNote(id);
    }

    @PutMapping("/{id}/offline")
    public Result<Void> offline(@PathVariable Long id, HttpServletRequest request) {
        checkAdmin(request);
        return noteService.adminOfflineNote(id);
    }

    @PutMapping("/{id}/ban")
    public Result<Void> ban(@PathVariable Long id, HttpServletRequest request) {
        checkAdmin(request);
        return noteService.banNote(id);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteNote(@PathVariable Long id, HttpServletRequest request) {
        checkAdmin(request);
        return noteService.adminDeleteNote(id);
    }
}