package com.share.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.share.common.BusinessException;
import com.share.common.Result;
import com.share.common.ResultCode;
import com.share.entity.Category;
import com.share.entity.Note;
import com.share.mapper.CategoryMapper;
import com.share.service.ICategoryService;
import com.share.service.INoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {

    @Lazy
    @Autowired
    private INoteService noteService;

    @Override
    public Result<Void> deleteCategory(Integer id) {
        boolean used = noteService.count(new LambdaQueryWrapper<Note>().eq(Note::getCategoryId, id)) > 0;
        if (used) throw new BusinessException(ResultCode.CATEGORY_IN_USE);
        this.removeById(id);
        return Result.success();
    }
}