package com.share.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.share.common.Result;
import com.share.entity.Note;
import com.share.service.ICollectRecordService;
import com.share.service.IHistoryService;
import com.share.service.ILikeRecordService;
import com.share.service.INoteService;
import com.share.vo.HistoryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class MyNoteController {

    private final INoteService noteService;
    private final ICollectRecordService collectRecordService;
    private final ILikeRecordService likeRecordService;
    private final IHistoryService historyService;

    @GetMapping("/collects")
    public Result<Page<Note>> collects(@RequestAttribute Long userId,
                                       @RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "10") Integer pageSize,
                                       @RequestParam(required = false) String keyword) {
        return collectRecordService.getMyCollects(userId, page, pageSize, keyword);
    }

    @DeleteMapping("/collects/{id}")
    public Result<?> deleteCollectRecord(@PathVariable Long id, @RequestAttribute Long userId) {
        return collectRecordService.deleteCollectRecord(id, userId);
    }

    @GetMapping("/notes")
    public Result<Page<Note>> getMyNotes(
            @RequestAttribute Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return noteService.getMyNotes(userId, page, pageSize, status, keyword);
    }

    @PostMapping("/notes")
    public Result<Note> createNote(@RequestAttribute Long userId, @RequestBody Note note) {
        return noteService.createNote(userId, note);
    }

    @GetMapping("/likes")
    public Result<Page<Note>> getMyLikes(@RequestAttribute Long userId,
                                         @RequestParam(defaultValue = "1") Integer page,
                                         @RequestParam(defaultValue = "10") Integer pageSize,
                                         @RequestParam(required = false) String keyword) {
        return likeRecordService.getMyLikes(userId, page, pageSize, keyword);
    }

    @GetMapping("/history")
    public Result<Page<HistoryVO>> getMyHistory(@RequestAttribute Long userId,
                                                @RequestParam(defaultValue = "1") Integer page,
                                                @RequestParam(defaultValue = "10") Integer pageSize,
                                                @RequestParam(required = false) String keyword) {
        return historyService.getMyHistory(userId, page, pageSize, keyword);
    }

    @DeleteMapping("/history/{id}")
    public Result<?> deleteHistory(@PathVariable Long id, @RequestAttribute Long userId) {
        return historyService.deleteHistory(id, userId);
    }

    @DeleteMapping("/history")
    public Result<?> deleteHistorys(@RequestAttribute Long userId) {
        return historyService.deleteHistorys(userId);
    }

    @PutMapping("/notes/{id}")
    public Result<Void> updateNote(@RequestAttribute Long userId, @PathVariable Long id, @RequestBody Note note) {
        Note existing = noteService.getById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            return Result.error(403, "无权修改");
        }
        existing.setTitle(note.getTitle());
        existing.setBookName(note.getBookName());
        existing.setAuthor(note.getAuthor());
        existing.setCover(note.getCover());
        existing.setCategoryId(note.getCategoryId());
        existing.setTags(note.getTags());
        existing.setContent(note.getContent());
        existing.setVisibility(note.getVisibility());
        noteService.updateById(existing);
        return Result.success();
    }

    @DeleteMapping("/notes/{id}")
    public Result<Void> deleteNote(@RequestAttribute Long userId, @PathVariable Long id) {
        return noteService.deleteMyNote(id, userId);
    }

    @PutMapping("/notes/{id}/visibility")
    public Result<Void> updateVisibility(@RequestAttribute Long userId, @PathVariable Long id, @RequestBody Map<String, String> body) {
        return noteService.updateVisibility(id, userId, body.get("visibility"));
    }

    @PostMapping("/notes/{id}/submit")
    public Result<Void> submitForReview(@RequestAttribute Long userId, @PathVariable Long id) {
        return noteService.submitNote(id, userId);
    }

    @PostMapping("/notes/{id}/offline")
    public Result<Void> offlineNote(@RequestAttribute Long userId, @PathVariable Long id) {
        return noteService.offlineNote(id, userId);
    }
}