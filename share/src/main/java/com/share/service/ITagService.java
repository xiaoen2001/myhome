package com.share.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.share.common.Result;
import com.share.entity.Tag;

public interface ITagService extends IService<Tag> {
    Result<Tag> addTag(Tag tag);
    Result<Void> deleteTag(Integer id);
}