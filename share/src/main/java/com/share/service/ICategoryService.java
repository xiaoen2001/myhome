package com.share.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.share.common.Result;
import com.share.entity.Category;

public interface ICategoryService extends IService<Category> {
    Result<Void> deleteCategory(Integer id);
}