package com.znn.easytravel.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.znn.easytravel.dto.GaoDeResponse;
import com.znn.easytravel.dto.LocationInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import com.znn.easytravel.response.ScenicSpotVO;
import com.znn.easytravel.service.ScenicSpotService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ScenicSpotServiceImpl implements ScenicSpotService {

    private static final String GAODE_API_URL = "https://restapi.amap.com/v5/place/text";

    private static final String GAODE_AROUND_API_URL = "https://restapi.amap.com/v5/place/around";
    @Value("${gao.de.key}")
    private  String API_KEY;
    private static final String TYPES = "110200|110201|110202|110203";
    private static final String TYPES2 = "100104|100105|100000";
    private static final boolean CITY_LIMIT = true;
    private static final String SHOW_FIELDS = "business";
    private static final int MAX_PAGE_NUM = 100; // 防止无限循环，设置最大页数限制

    private final RestTemplate restTemplate;

    public ScenicSpotServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public List<ScenicSpotVO> getTopRatedScenicSpots(String cityName) {
        List<GaoDeResponse.Poi> allQualifiedPois = new ArrayList<>();
        int pageNum = 1;
        int maxRetries = 3;

        while (pageNum <= MAX_PAGE_NUM) {
            log.info("正在获取第 {} 页数据，城市: {}", pageNum, cityName);

            String url = buildUrl(cityName, pageNum);
            boolean success = false;

            for (int retry = 0; retry < maxRetries && !success; retry++) {
                try {
                    if (retry > 0) {
                        log.info("第 {} 页第 {} 次重试", pageNum, retry);
                        Thread.sleep(500 * retry);
                    }

                    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                    String responseBody = response.getBody();

                    if (responseBody == null) {
                        log.warn("第 {} 页返回数据为空", pageNum);
                        break;
                    }

                    JSONObject jsonObject = JSON.parseObject(responseBody);
                    String status = jsonObject.getString("status");
                    String infocode = jsonObject.getString("infocode");

                    if (!"1".equals(status) || !"10000".equals(infocode)) {
                        log.error("API调用失败，status: {}, infocode: {}", status, infocode);
                        continue;
                    }

                    JSONArray poisArray = jsonObject.getJSONArray("pois");
                    if (poisArray == null || poisArray.isEmpty()) {
                        log.info("第 {} 页没有更多数据，结束循环", pageNum);
                        return getTopFiveScenicSpots(allQualifiedPois);
                    }

                    List<GaoDeResponse.Poi> currentPagePois = parsePois(poisArray);

                    List<GaoDeResponse.Poi> qualifiedPois = filterByRating(currentPagePois, 4);
                    allQualifiedPois.addAll(qualifiedPois);

                    log.info("第 {} 页获取到 {} 条数据，其中 {} 条符合条件",
                            pageNum, currentPagePois.size(), qualifiedPois.size());

                    success = true;

                } catch (Exception e) {
                    log.error("获取第 {} 页数据时发生异常（第 {} 次尝试）", pageNum, retry + 1, e);
                    if (retry == maxRetries - 1) {
                        log.error("第 {} 页达到最大重试次数，跳过该页", pageNum);
                    }
                }
            }

            if (!success) {
                log.warn("第 {} 页多次重试后仍然失败，跳过该页继续下一页", pageNum);
            }

            pageNum++;
        }

        log.info("总共获取到 {} 条符合条件的景区数据", allQualifiedPois.size());

        return getTopFiveScenicSpots(allQualifiedPois);
    }

    private String buildUrl(String cityName, int pageNum) {
        return String.format("%s?key=%s&types=%s&region=%s&city_limit=%s&show_fields=%s&page_num=%d",
                GAODE_API_URL,
                API_KEY,
                TYPES,
                cityName,
                CITY_LIMIT,
                SHOW_FIELDS,
                pageNum);
    }

    private String buildAroundUrl(String localtion, int pageNum) {
        return String.format("%s?key=%s&location=%s&types=%s&radius=%s&show_fields=%s&page_num=%d",
                GAODE_AROUND_API_URL,
                API_KEY,
                localtion,
                TYPES2,
                30000,
                SHOW_FIELDS,
                pageNum);
    }

    private List<GaoDeResponse.Poi> parsePois(JSONArray poisArray) {
        List<GaoDeResponse.Poi> pois = new ArrayList<>();
        for (int i = 0; i < poisArray.size(); i++) {
            JSONObject poiJson = poisArray.getJSONObject(i);
            GaoDeResponse.Poi poi = new GaoDeResponse.Poi();

            poi.setAddress(poiJson.getString("address"));
            poi.setName(poiJson.getString("name"));
            poi.setLocation(poiJson.getString("location"));
            poi.setId(poiJson.getString("id"));

            JSONObject businessJson = poiJson.getJSONObject("business");
            if (businessJson != null) {
                GaoDeResponse.Business business = new GaoDeResponse.Business();
                business.setRating(businessJson.getString("rating"));
                poi.setBusiness(business);
            }

            pois.add(poi);
        }
        return pois;
    }

    private List<GaoDeResponse.Poi> filterByRating(List<GaoDeResponse.Poi> pois, double minRating) {
        return pois.stream()
                .filter(poi -> {
                    if (poi.getBusiness() == null || poi.getBusiness().getRating() == null) {
                        return false;
                    }
                    try {
                        double rating = Double.parseDouble(poi.getBusiness().getRating());
                        return rating >= minRating;
                    } catch (NumberFormatException e) {
                        log.warn("解析评分失败: {}", poi.getBusiness().getRating());
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    private List<ScenicSpotVO> getTopFiveScenicSpots(List<GaoDeResponse.Poi> pois) {
        return pois.stream()
                .sorted(Comparator.comparingDouble((GaoDeResponse.Poi poi) ->
                        Double.parseDouble(poi.getBusiness().getRating())).reversed())
                .limit(10)
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    private ScenicSpotVO convertToVO(GaoDeResponse.Poi poi) {
        return ScenicSpotVO.builder()
                .address(poi.getAddress())
                .name(poi.getName())
                .location(poi.getLocation())
                .rating(poi.getBusiness().getRating())
                .build();
    }


    /**
     * 计算中心点附近30公里酒店
     * @param points
     * @return
     */
    @Override
    public List<ScenicSpotVO> arithmeticAverage(List<LocationInfoDTO> points) {
        if (CollectionUtils.isEmpty(points)) {
            throw new IllegalArgumentException("点列表不能为空");
        }

        double totalLat = 0, totalLng = 0;
        for (LocationInfoDTO point : points) {
            totalLat += point.getLatitude();
            totalLng += point.getLongitude();
        }

        double centerLatitude = Math.round((totalLat / points.size()) * 1_000_000.0) / 1_000_000.0;
        double centerLongitude = Math.round((totalLng / points.size()) * 1_000_000.0) / 1_000_000.0;
        String location = centerLatitude + "," + centerLongitude;
        List<GaoDeResponse.Poi> allQualifiedPois = new ArrayList<>();
        int pageNum = 1;
        int maxRetries = 3;

        while (pageNum <= MAX_PAGE_NUM) {
            log.info("正在获取第 {} 页数据", pageNum);

            String url = buildAroundUrl(location, pageNum);
            boolean success = false;

            for (int retry = 0; retry < maxRetries && !success; retry++) {
                try {
                    if (retry > 0) {
                        log.info("第 {} 页第 {} 次重试", pageNum, retry);
                        Thread.sleep(500 * retry);
                    }

                    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                    String responseBody = response.getBody();

                    if (responseBody == null) {
                        log.warn("第 {} 页返回数据为空", pageNum);
                        break;
                    }

                    JSONObject jsonObject = JSON.parseObject(responseBody);
                    String status = jsonObject.getString("status");
                    String infocode = jsonObject.getString("infocode");

                    if (!"1".equals(status) || !"10000".equals(infocode)) {
                        log.error("API调用失败，status: {}, infocode: {}", status, infocode);
                        continue;
                    }

                    JSONArray poisArray = jsonObject.getJSONArray("pois");
                    if (poisArray == null || poisArray.isEmpty()) {
                        log.info("第 {} 页没有更多数据，结束循环", pageNum);
                        return getTopFiveScenicSpots(allQualifiedPois);
                    }

                    List<GaoDeResponse.Poi> currentPagePois = parsePois(poisArray);

                    List<GaoDeResponse.Poi> qualifiedPois = filterByRating(currentPagePois, 4);
                    allQualifiedPois.addAll(qualifiedPois);

                    log.info("第 {} 页获取到 {} 条数据，其中 {} 条符合条件",
                            pageNum, currentPagePois.size(), qualifiedPois.size());

                    success = true;

                } catch (Exception e) {
                    log.error("获取第 {} 页数据时发生异常（第 {} 次尝试）", pageNum, retry + 1, e);
                    if (retry == maxRetries - 1) {
                        log.error("第 {} 页达到最大重试次数，跳过该页", pageNum);
                    }
                }
            }

            if (!success) {
                log.warn("第 {} 页多次重试后仍然失败，跳过该页继续下一页", pageNum);
            }

            pageNum++;
        }

        log.info("总共获取到 {} 条符合条件的景区数据", allQualifiedPois.size());

        return getTopFiveScenicSpots(allQualifiedPois);
    }

}
