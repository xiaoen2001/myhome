package com.share.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.share.common.BusinessException;
import com.share.common.Result;
import com.share.common.ResultCode;
import com.share.entity.Tag;
import com.share.service.ITagService;
import com.share.service.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/tags")
@RequiredArgsConstructor
public class AdminTagController {

    private final ITagService tagService;
    private final IUserService userService;

    private void checkAdmin(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) throw new BusinessException(ResultCode.UNAUTHORIZED);
        String role = userService.getById(userId).getRole();
        if (!"admin".equals(role)) throw new BusinessException(ResultCode.FORBIDDEN);
    }

    @GetMapping
    public Result<Page<Tag>> list(@RequestParam(defaultValue = "1") Integer page,
                                  @RequestParam(defaultValue = "10") Integer pageSize,
                                  HttpServletRequest request) {
        checkAdmin(request);
        Page<Tag> pageResult = tagService.page(new Page<>(page, pageSize));
        return Result.success(pageResult);
    }

    @PostMapping
    public Result<Tag> add(@RequestBody Tag tag, HttpServletRequest request) {
        checkAdmin(request);
        return tagService.addTag(tag);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Integer id, @RequestBody Tag tag, HttpServletRequest request) {
        checkAdmin(request);
        tag.setId(id);
        tagService.updateById(tag);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id, HttpServletRequest request) {
        checkAdmin(request);
        return tagService.deleteTag(id);
    }
}