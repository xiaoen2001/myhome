package com.share.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.share.common.Result;
import com.share.entity.Carousel;
import com.share.service.ICarouselService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PublicController {

    private final ICarouselService carouselService;

    @GetMapping("/carousels")
    public Result<List<Carousel>> getCarousels() {
        List<Carousel> list = carouselService.list(new LambdaQueryWrapper<Carousel>().eq(Carousel::getEnabled, true).orderByAsc(Carousel::getSortOrder));
        return Result.success(list);
    }

}