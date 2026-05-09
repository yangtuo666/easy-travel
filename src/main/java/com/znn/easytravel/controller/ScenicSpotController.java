package com.znn.easytravel.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.znn.easytravel.response.ScenicSpotVO;
import com.znn.easytravel.service.ScenicSpotService;

import java.util.List;

@RestController
@RequestMapping("/api/scenic-spots")
@RequiredArgsConstructor
public class ScenicSpotController {

    private final ScenicSpotService scenicSpotService;

    @GetMapping("/top-rated")
    public List<ScenicSpotVO> getTopRatedScenicSpots(@RequestParam String cityName) {
        return scenicSpotService.getTopRatedScenicSpots(cityName);
    }
}
