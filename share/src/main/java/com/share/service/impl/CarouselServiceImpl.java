package com.share.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.share.common.BusinessException;
import com.share.common.Result;
import com.share.entity.Carousel;
import com.share.mapper.CarouselMapper;
import com.share.service.ICarouselService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class CarouselServiceImpl extends ServiceImpl<CarouselMapper, Carousel> implements ICarouselService {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${server.base-url:http://localhost:8080}")
    private String baseUrl;

    @Override
    public Result<Void> updateCarousel(Long id, Carousel carousel) {
        Carousel oldCarousel = this.getById(id);
        String imageUrl = oldCarousel.getImageUrl();
        // 只删除本地上传的图片文件（跳过外部URL和data URI）
        if (imageUrl != null && !imageUrl.isEmpty()
                && imageUrl.startsWith(baseUrl)
                && !imageUrl.startsWith("data:")) {
            String relativePath = imageUrl.replace(baseUrl + "/uploads/", "");
            File oldFile = new File(uploadDir, relativePath);
            if (oldFile.exists()) {
                oldFile.delete();
            }
        }
        carousel.setId(id);
        this.updateById(carousel);
        return Result.success();
    }

    @Override
    public Result<String> uploadImage(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String suffix = null;
        if (originalFilename != null) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFileName = UUID.randomUUID() + suffix;
        String subDir = "carousels";
        File dir = new File(uploadDir, subDir);
        if (!dir.exists()) dir.mkdirs();
        File dest = new File(dir, newFileName);
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw new BusinessException(500, "文件保存失败");
        }
        String imageUrl = baseUrl + "/uploads/carousels/" + newFileName;
        return Result.success(imageUrl);
    }
}