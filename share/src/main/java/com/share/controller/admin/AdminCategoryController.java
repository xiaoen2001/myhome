package com.share.controller.admin;

import com.share.common.BusinessException;
import com.share.common.Result;
import com.share.common.ResultCode;
import com.share.entity.Category;
import com.share.service.ICategoryService;
import com.share.service.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final ICategoryService categoryService;
    private final IUserService userService;

    private void checkAdmin(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) throw new BusinessException(ResultCode.UNAUTHORIZED);
        String role = userService.getById(userId).getRole();
        if (!"admin".equals(role)) throw new BusinessException(ResultCode.FORBIDDEN);
    }

    @GetMapping
    public Result<List<Category>> list(HttpServletRequest request) {
        checkAdmin(request);
        return Result.success(categoryService.list());
    }

    @PostMapping
    public Result<Category> add(@RequestBody Category category, HttpServletRequest request) {
        checkAdmin(request);
        categoryService.save(category);
        return Result.success(category);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Integer id, @RequestBody Category category, HttpServletRequest request) {
        checkAdmin(request);
        category.setId(id);
        categoryService.updateById(category);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id, HttpServletRequest request) {
        checkAdmin(request);
        return categoryService.deleteCategory(id);
    }
}