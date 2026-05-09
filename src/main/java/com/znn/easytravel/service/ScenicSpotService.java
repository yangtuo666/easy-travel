package com.znn.easytravel.service;

import com.znn.easytravel.response.ScenicSpotVO;
import java.util.List;

public interface ScenicSpotService {

    List<ScenicSpotVO> getTopRatedScenicSpots(String cityName);
}
