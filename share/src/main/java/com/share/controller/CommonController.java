package com.share.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.share.common.Result;
import com.share.entity.Category;
import com.share.entity.Tag;
import com.share.service.ICategoryService;
import com.share.service.ITagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommonController {

    private final ICategoryService categoryService;
    private final ITagService tagService;

    @GetMapping("/categories")
    public Result<List<Category>> getCategories() {
        List<Category> list = categoryService.list(new LambdaQueryWrapper<Category>().eq(Category::getEnabled, true).orderByAsc(Category::getSort));
        return Result.success(list);
    }

    @GetMapping("/tags")
    public Result<List<Tag>> getTags() {
        List<Tag> list = tagService.list(new LambdaQueryWrapper<Tag>().eq(Tag::getEnabled, true).orderByAsc(Tag::getSortOrder));
        return Result.success(list);
    }
}