package com.znn.easytravel.service;

import com.znn.easytravel.dto.LocationInfoDTO;
import com.znn.easytravel.response.ScenicSpotVO;
import java.util.List;

public interface ScenicSpotService {

    List<ScenicSpotVO> getTopRatedScenicSpots(String cityName);

    List<ScenicSpotVO> arithmeticAverage(List<LocationInfoDTO> points);
}
