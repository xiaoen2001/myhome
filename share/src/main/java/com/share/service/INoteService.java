package com.share.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.share.common.Result;
import com.share.entity.Note;
import com.share.vo.NoteVO;

import java.util.List;
import java.util.Map;

public interface INoteService extends IService<Note> {
    boolean incrementViewCount(Long noteId);
    boolean incrementLikeCount(Long noteId);
    boolean incrementCommentCount(Long noteId);
    boolean decrementLikeCount(Long noteId);
    boolean incrementCollectCount(Long noteId);
    boolean decrementCollectCount(Long noteId);

    Result<?> removeNote(Integer id, Long userId);

    Result<Page<Note>> listNotes(Integer page, Integer pageSize, String keyword, Integer categoryId, String sort);

    Result<?> addNote(Long userId, Note note);

    Result<?> updateNote(Long userId, Long id, Note note);

    Result<Void> updateVisibility(Long id, Long userId, String visibility);

    Result<List<Note>> getRecommendNotes(String type);

    Result<Void> submitNote(Long id, Long userId);

    Result<Void> offlineNote(Long id, Long userId);

    Result<Page<Note>> getMyNotes(Long userId, Integer page, Integer pageSize, String status, String keyword);

    Result<Note> createNote(Long userId, Note note);

    Result<NoteVO> getNoteDetail(Long id, Long userId);

    Result<Void> deleteMyNote(Long id, Long userId);

    Result<Page<Map<String, Object>>> adminListNotes(Integer page, Integer pageSize, String keyword, String status);

    Result<Void> approveNote(Long id);

    Result<Void> rejectNote(Long id);

    Result<Void> adminOfflineNote(Long id);

    Result<Void> banNote(Long id);

    Result<Void> adminDeleteNote(Long id);
}