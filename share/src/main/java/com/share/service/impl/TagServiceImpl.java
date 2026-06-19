package com.share.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.share.common.BusinessException;
import com.share.common.Result;
import com.share.common.ResultCode;
import com.share.entity.Tag;
import com.share.mapper.TagMapper;
import com.share.service.INoteService;
import com.share.service.ITagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements ITagService {

    @Autowired
    @Lazy
    private INoteService noteService;

    @Override
    public Result<Tag> addTag(Tag tag) {
        boolean exists = this.exists(new LambdaQueryWrapper<Tag>()
                .eq(Tag::getName, tag.getName())
                .eq(Tag::getSortOrder, tag.getSortOrder()));
        if (exists) {
            return Result.error(ResultCode.PLAN_ALREADY_COMPLETED);
        }
        this.save(tag);
        return Result.success(tag);
    }

    @Override
    public Result<Void> deleteTag(Integer id) {
        boolean used = noteService.list().stream()
                .anyMatch(note -> note.getTags() != null && note.getTags().stream().anyMatch(t -> t.equals(this.getById(id).getName())));
        if (used) throw new BusinessException(400, "该标签已被笔记使用，无法删除");
        this.removeById(id);
        return Result.success();
    }
}