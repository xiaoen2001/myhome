package com.share.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.share.common.Result;
import com.share.entity.Carousel;
import org.springframework.web.multipart.MultipartFile;

public interface ICarouselService extends IService<Carousel> {
    Result<Void> updateCarousel(Long id, Carousel carousel);
    Result<String> uploadImage(MultipartFile file);
}