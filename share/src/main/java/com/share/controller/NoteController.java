package com.share.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.share.common.Result;
import com.share.entity.Note;
import com.share.service.ICollectRecordService;
import com.share.service.ILikeRecordService;
import com.share.service.INoteService;
import com.share.vo.NoteVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final INoteService noteService;
    private final ILikeRecordService likeRecordService;
    private final ICollectRecordService collectRecordService;

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id, @RequestAttribute Long userId) {
        return noteService.removeNote(id, userId);
    }

    @GetMapping
    public Result<Page<Note>> listNotes(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(defaultValue = "latest") String sort) {
        return noteService.listNotes(page, pageSize, keyword, categoryId, sort);
    }

    @PostMapping
    public Result<?> addNote(@RequestAttribute Long userId, @RequestBody Note note) {
        return noteService.addNote(userId, note);
    }

    @PutMapping("/{id}")
    public Result<?> updateNote(@RequestAttribute Long userId, @PathVariable Long id, @RequestBody Note note) {
        return noteService.updateNote(userId, id, note);
    }

    @GetMapping("/recommend")
    public Result<List<Note>> getRecommendNotes(@RequestParam String type) {
        return noteService.getRecommendNotes(type);
    }

    @GetMapping("/{id}")
    public Result<NoteVO> getNoteDetail(@PathVariable Long id, @RequestAttribute Long userId) {
        return noteService.getNoteDetail(id, userId);
    }

    @PostMapping("/{id}/like")
    public Result<Void> likeNote(@PathVariable Long id, @RequestAttribute Long userId) {
        likeRecordService.like(userId, id);
        return Result.success();
    }

    @DeleteMapping("/{id}/like")
    public Result<Void> unlikeNote(@PathVariable Long id, @RequestAttribute Long userId) {
        likeRecordService.unlike(userId, id);
        return Result.success();
    }

    @PostMapping("/{id}/collect")
    public Result<Void> collectNote(@PathVariable Long id, @RequestAttribute Long userId) {
        collectRecordService.collect(userId, id);
        return Result.success();
    }

    @DeleteMapping("/{id}/collect")
    public Result<Void> uncollectNote(@PathVariable Long id, @RequestAttribute Long userId) {
        collectRecordService.uncollect(userId, id);
        return Result.success();
    }

    @PostMapping("/{id}/submit")
    public Result<Void> submitNote(@PathVariable Long id, @RequestAttribute Long userId) {
        return noteService.submitNote(id, userId);
    }

    @PostMapping("/{id}/offline")
    public Result<Void> offlineNote(@PathVariable Long id, @RequestAttribute Long userId) {
        return noteService.offlineNote(id, userId);
    }
}