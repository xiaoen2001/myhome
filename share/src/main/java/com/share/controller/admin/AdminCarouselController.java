package com.share.controller.admin;

import com.share.common.BusinessException;
import com.share.common.Result;
import com.share.common.ResultCode;
import com.share.entity.Carousel;
import com.share.service.ICarouselService;
import com.share.service.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/api/admin/carousels")
@RequiredArgsConstructor
public class AdminCarouselController {

    private final ICarouselService carouselService;
    private final IUserService userService;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${server.base-url:http://localhost:8080}")
    private String baseUrl;

    private void checkAdmin(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) throw new BusinessException(ResultCode.UNAUTHORIZED);
        String role = userService.getById(userId).getRole();
        if (!"admin".equals(role)) throw new BusinessException(ResultCode.FORBIDDEN);
    }

    @GetMapping
    public Result<List<Carousel>> list(HttpServletRequest request) {
        checkAdmin(request);
        return Result.success(carouselService.list());
    }

    @DeleteMapping("/new")
    public Result<Void> deleteTempImage(@RequestParam("url") String url, HttpServletRequest request) {
        checkAdmin(request);
        if (!StringUtils.hasText(url)) {
            return Result.error(ResultCode.BAD_REQUEST);
        }
        String relativePath = url.replace(baseUrl + "/uploads/", "");
        File file = new File(uploadDir, relativePath);
        if (file.exists()) {
            boolean deleted = file.delete();
            if (!deleted) {
                return Result.error(500, "文件删除失败");
            }
        }
        return Result.success();
    }

    @PostMapping
    public Result<Carousel> add(@RequestBody Carousel carousel, HttpServletRequest request) {
        checkAdmin(request);
        carouselService.save(carousel);
        return Result.success(carousel);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Carousel carousel, HttpServletRequest request) {
        checkAdmin(request);
        return carouselService.updateCarousel(id, carousel);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        checkAdmin(request);
        carouselService.removeById(id);
        return Result.success();
    }

    @PostMapping("/upload")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        checkAdmin(request);
        return carouselService.uploadImage(file);
    }
}