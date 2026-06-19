package com.share.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.share.common.BusinessException;
import com.share.common.Result;
import com.share.common.ResultCode;
import com.share.entity.*;
import com.share.mapper.NoteMapper;
import com.share.service.*;
import com.share.util.FileUpload;
import com.share.vo.NoteVO;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NoteServiceImpl extends ServiceImpl<NoteMapper, Note> implements INoteService {
    private final FileUpload fileUpload;
    private final ICollectRecordService collectRecordService;
    private final ILikeRecordService likeRecordService;
    private final ICommentService commentService;
    private final IHistoryService historyService;
    private final IUserService userService;
    private final ICategoryService categoryService;
    private final INotificationService notificationService;

    public NoteServiceImpl(FileUpload fileUpload,
                           @Lazy ICollectRecordService collectRecordService,
                           @Lazy ILikeRecordService likeRecordService,
                           @Lazy ICommentService commentService,
                           @Lazy IHistoryService historyService,
                           @Lazy IUserService userService,
                           @Lazy ICategoryService categoryService,
                           @Lazy INotificationService notificationService) {
        this.fileUpload = fileUpload;
        this.collectRecordService = collectRecordService;
        this.likeRecordService = likeRecordService;
        this.commentService = commentService;
        this.historyService = historyService;
        this.userService = userService;
        this.categoryService = categoryService;
        this.notificationService = notificationService;
    }

    @Override
    public boolean incrementViewCount(Long noteId) {
        return this.update().setSql("view_count = view_count + 1").eq("id", noteId).update();
    }

    @Override
    public boolean incrementLikeCount(Long noteId) {
        return this.update().setSql("like_count = like_count + 1").eq("id", noteId).update();
    }

    @Override
    public boolean incrementCommentCount(Long noteId) {
        return this.update().setSql("comment_count = comment_count + 1").eq("id", noteId).update();
    }

    @Override
    public boolean decrementLikeCount(Long noteId) {
        return this.update().setSql("like_count = like_count - 1").eq("id", noteId).update();
    }

    @Override
    public boolean incrementCollectCount(Long noteId) {
        return this.update().setSql("collect_count = collect_count + 1").eq("id", noteId).update();
    }

    @Override
    public boolean decrementCollectCount(Long noteId) {
        return this.update().setSql("collect_count = collect_count - 1").eq("id", noteId).update();
    }

    @Override
    public Result<Note> createNote(Long userId, Note note) {
        note.setUserId(userId);
        note.setStatus("draft");
        note.setVisibility("public");
        note.setViewCount(0);
        note.setCommentCount(0);
        note.setLikeCount(0);
        note.setCollectCount(0);
        this.save(note);
        return Result.success(note);
    }

    @Override
    public Result<Page<Note>> getMyNotes(Long userId, Integer page, Integer pageSize, String status, String keyword) {
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getUserId, userId);
        if (status != null) {
            wrapper.eq(Note::getStatus, status);
        }
        if (keyword != null) {
            wrapper.and(w -> w.like(Note::getTitle, keyword).or().like(Note::getBookName, keyword));
        }
        wrapper.orderByDesc(Note::getCreatedAt);
        Page<Note> result = this.page(new Page<>(page, pageSize), wrapper);
        return Result.success(result);
    }

    @Override
    public Result<Void> offlineNote(Long id, Long userId) {
        Note note = this.getById(id);
        if (note == null) {
            return Result.error(ResultCode.BAD_REQUEST);
        }
        if (!note.getUserId().equals(userId)) {
            return Result.error(ResultCode.FORBIDDEN);
        }
        note.setStatus("offline");
        boolean b = this.saveOrUpdate(note);
        if (!b) {
            return Result.error(ResultCode.BAD_REQUEST);
        }
        return Result.success();
    }

    @Override
    public Result<Void> submitNote(Long id, Long userId) {
        Note note = this.getById(id);
        if (note == null) {
            return Result.error(ResultCode.BAD_REQUEST);
        }
        if (!note.getUserId().equals(userId)) {
            return Result.error(ResultCode.FORBIDDEN);
        }
        if ("private".equals(note.getVisibility())) {
            note.setStatus("published");
        } else {
            note.setStatus("reviewing");
        }
        boolean b = this.saveOrUpdate(note);
        if (!b) {
            return Result.error(ResultCode.BAD_REQUEST);
        }
        return Result.success();
    }

    @Override
    public Result<List<Note>> getRecommendNotes(String type) {
        List<Note> notes = this.list(new LambdaQueryWrapper<Note>()
                .eq(Note::getStatus, "published")
                .eq(Note::getVisibility, "public"));
        if (notes.isEmpty()) {
            return Result.success(List.of());
        }
        List<Note> sorted;
        if ("hot".equals(type)) {
            sorted = notes.stream()
                    .sorted((a, b) -> b.getLikeCount() - a.getLikeCount())
                    .limit(5)
                    .collect(Collectors.toList());
        } else if ("mostCommented".equals(type)) {
            sorted = notes.stream()
                    .sorted((a, b) -> b.getCommentCount() - a.getCommentCount())
                    .limit(5)
                    .collect(Collectors.toList());
        } else {
            sorted = notes.stream()
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .limit(5)
                    .collect(Collectors.toList());
        }
        return Result.success(sorted);
    }

    @Override
    public Result<?> updateNote(Long userId, Long id, Note note) {
        note.setId(id);
        note.setUserId(userId);
        String noteVisibility = note.getVisibility();
        if ("private".equals(noteVisibility)) {
            note.setStatus("published");
        } else {
            note.setStatus("reviewing");
        }
        String cover = note.getCover();
        if (cover != null && cover.startsWith("data:image/")) {
            String imageUrl = fileUpload.saveBase64Image(cover, "notes");
            note.setCover(imageUrl);
        }
        boolean b = this.saveOrUpdate(note);
        if (!b) {
            return Result.error(400, "更新失败");
        }
        return Result.success(b);
    }

    @Override
    public Result<?> addNote(Long userId, Note note) {
        note.setUserId(userId);
        String cover = note.getCover();
        if (cover != null && cover.startsWith("data:image/")) {
            String imageUrl = fileUpload.saveBase64Image(cover, "notes");
            note.setCover(imageUrl);
        }
        boolean save = this.save(note);
        if (save) {
            return Result.success(ResultCode.SUCCESS);
        }
        return Result.success(ResultCode.BAD_REQUEST);
    }

    @Override
    public Result<?> removeNote(Integer id, Long userId) {
        Note note = this.getById(id);
        if (note == null) {
            return Result.success();
        }
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException(400, "笔记用户不匹配!");
        }
        return Result.success(this.removeById(id));
    }

    @Override
    public Result<Page<Note>> listNotes(Integer page, Integer pageSize, String keyword, Integer categoryId, String sort) {
        Page<Note> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getStatus, "published")
                .eq(Note::getVisibility, "public");
        if (categoryId != null) {
            wrapper.eq(Note::getCategoryId, categoryId);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            String likePattern = "%" + keyword.trim() + "%";
            wrapper.and(w -> w.like(Note::getTitle, likePattern)
                    .or()
                    .like(Note::getBookName, likePattern)
                    .or()
                    .like(Note::getAuthor, likePattern));
        }
        if ("latest".equals(sort)) {
            wrapper.orderByDesc(Note::getCreatedAt);
        } else if ("hot".equals(sort)) {
            wrapper.orderByDesc(Note::getViewCount);
        } else if ("mostCommented".equals(sort)) {
            wrapper.orderByDesc(Note::getCommentCount);
        }
        Page<Note> result = this.page(pageParam, wrapper);
        return Result.success(result);
    }

    @Override
    public Result<NoteVO> getNoteDetail(Long id, Long userId) {
        Note note = this.getById(id);
        if (note == null || !"published".equals(note.getStatus())) {
            return Result.error(404, "笔记不存在");
        }
        if (!"public".equals(note.getVisibility()) && !note.getUserId().equals(userId)) {
            return Result.error(404, "笔记不存在或未公开");
        }
        this.incrementViewCount(id);
        NoteVO res = new NoteVO();
        BeanUtils.copyProperties(note, res);
        boolean like = likeRecordService.exists(new LambdaQueryWrapper<LikeRecord>()
                .eq(LikeRecord::getNoteId, id)
                .eq(LikeRecord::getUserId, userId));
        boolean collect = collectRecordService.exists(new LambdaQueryWrapper<CollectRecord>()
                .eq(CollectRecord::getNoteId, id)
                .eq(CollectRecord::getUserId, userId));
        User user = userService.getById(note.getUserId());
        if (user != null) {
            res.setUserAvatar(user.getAvatar());
            res.setNickname(user.getNickname());
            res.setUsername(user.getUsername());
        } else {
            return Result.error(404, "作者没了");
        }
        res.setLiked(like);
        res.setCollected(collect);
        historyService.saveHistory(userId, note.getId());
        return Result.success(res);
    }

    @Override
    public Result<Void> deleteMyNote(Long id, Long userId) {
        Note existing = this.getById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            return Result.error(403, "无权删除");
        }
        this.removeById(id);
        return Result.success();
    }

    @Override
    public Result<Void> updateVisibility(Long id, Long userId, String visibility) {
        Note note = this.getById(id);
        if (note == null || !note.getUserId().equals(userId)) {
            return Result.error(403, "无权操作");
        }
        if (!"draft".equals(note.getStatus())) {
            return Result.error(400, "仅草稿状态可修改可见性");
        }
        note.setVisibility(visibility);
        this.updateById(note);
        return Result.success();
    }

    @Override
    public Result<Page<Map<String, Object>>> adminListNotes(Integer page, Integer pageSize, String keyword, String status) {
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(Note::getStatus, "draft")
               .ne(Note::getVisibility, "private");
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w ->
                    w.like(Note::getTitle, keyword).or()
                            .like(Note::getBookName, keyword).or()
                            .like(Note::getAuthor, keyword)
            );
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Note::getStatus, status);
        }
        wrapper.orderByDesc(Note::getCreatedAt);
        Page<Note> pageResult = this.page(new Page<>(page, pageSize), wrapper);
        Page<Map<String, Object>> result = new Page<>();
        result.setCurrent(pageResult.getCurrent());
        result.setSize(pageResult.getSize());
        result.setTotal(pageResult.getTotal());
        result.setRecords(pageResult.getRecords().stream().map(note -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", note.getId());
            map.put("title", note.getTitle());
            map.put("bookName", note.getBookName());
            map.put("author", note.getAuthor());
            map.put("cover", note.getCover());
            map.put("content", note.getContent());
            map.put("viewCount", note.getViewCount());
            map.put("commentCount", note.getCommentCount());
            map.put("likeCount", note.getLikeCount());
            map.put("collectCount", note.getCollectCount());
            map.put("tags", note.getTags());
            map.put("status", note.getStatus());
            map.put("visibility", note.getVisibility());
            map.put("createdAt", note.getCreatedAt());
            map.put("updatedAt", note.getUpdatedAt());
            User user = userService.getById(note.getUserId());
            map.put("nickname", user != null ? user.getNickname() : "未知");
            Category category = categoryService.getById(note.getCategoryId());
            map.put("categoryName", category != null ? category.getName() : "未分类");
            return map;
        }).toList());
        return Result.success(result);
    }

    @Override
    public Result<Void> approveNote(Long id) {
        Note note = this.getById(id);
        if (note == null || !"reviewing".equals(note.getStatus())) {
            throw new BusinessException(400, "笔记状态不允许审核");
        }
        note.setStatus("published");
        note.setVisibility("public");
        this.updateById(note);

        // 发送个人通知给笔记作者
        Map<String, Object> notiParams = new HashMap<>();
        notiParams.put("type", "admin");
        notiParams.put("title", "笔记审核通过");
        notiParams.put("content", "您的笔记《" + note.getTitle() + "》已通过审核，现已公开发布。");
        notiParams.put("userId", note.getUserId());
        Map<String, String> detail = new HashMap<>();
        detail.put("title", "笔记审核通过通知");
        detail.put("content", "<p>恭喜！您的笔记<strong>《" + note.getTitle() + "》</strong>已通过管理员审核，现已公开发布。</p><p>其他用户现在可以查看、点赞和评论您的笔记了。</p>");
        detail.put("author", "管理员");
        notiParams.put("detail", detail);
        notificationService.addNotification(notiParams);

        return Result.success();
    }

    @Override
    public Result<Void> rejectNote(Long id) {
        Note note = this.getById(id);
        if (note == null || !"reviewing".equals(note.getStatus())) {
            throw new BusinessException(400, "笔记状态不允许驳回");
        }
        note.setStatus("rejected");
        this.updateById(note);

        // 发送个人通知给笔记作者
        Map<String, Object> notiParams = new HashMap<>();
        notiParams.put("type", "admin");
        notiParams.put("title", "笔记审核未通过");
        notiParams.put("content", "您的笔记《" + note.getTitle() + "》未通过审核，请修改后重新提交。");
        notiParams.put("userId", note.getUserId());
        Map<String, String> detail = new HashMap<>();
        detail.put("title", "笔记审核驳回通知");
        detail.put("content", "<p>很遗憾，您的笔记<strong>《" + note.getTitle() + "》</strong>未通过管理员审核。</p><p>可能的原因包括：</p><ul><li>内容不符合社区规范</li><li>笔记内容过于简略</li><li>存在版权问题</li></ul><p>请修改后重新提交审核，如有疑问请联系管理员。</p>");
        detail.put("author", "管理员");
        notiParams.put("detail", detail);
        notificationService.addNotification(notiParams);

        return Result.success();
    }

    @Override
    public Result<Void> adminOfflineNote(Long id) {
        Note note = this.getById(id);
        if (note == null || !"published".equals(note.getStatus())) {
            throw new BusinessException(400, "笔记状态不允许下架");
        }
        note.setStatus("offline");
        this.updateById(note);
        return Result.success();
    }

    @Override
    public Result<Void> banNote(Long id) {
        Note note = this.getById(id);
        if (note == null) throw new BusinessException(ResultCode.NOT_FOUND);
        note.setStatus("banned");
        this.updateById(note);
        return Result.success();
    }

    @Override
    public Result<Void> adminDeleteNote(Long id) {
        Note note = this.getById(id);
        if (note == null) throw new BusinessException(ResultCode.NOT_FOUND);
        commentService.remove(new LambdaQueryWrapper<Comment>().eq(Comment::getNoteId, id));
        likeRecordService.remove(new LambdaQueryWrapper<LikeRecord>().eq(LikeRecord::getNoteId, id));
        collectRecordService.remove(new LambdaQueryWrapper<CollectRecord>().eq(CollectRecord::getNoteId, id));
        historyService.remove(new LambdaQueryWrapper<History>().eq(History::getNoteId, id));
        this.removeById(id);
        return Result.success();
    }
}