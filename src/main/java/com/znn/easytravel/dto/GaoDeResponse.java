package com.znn.easytravel.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import java.util.List;

@Data
public class GaoDeResponse {
    private String count;
    private String infocode;
    private List<Poi> pois;
    private String status;
    private String info;

    @Data
    public static class Poi {
        private String parent;
        private String address;
        private Business business;
        private String distance;
        private String pcode;
        private String adcode;
        private String pname;
        private String cityname;
        private String type;
        private String typecode;
        private String adname;
        private String citycode;
        private String name;
        private String location;
        private String id;
    }

    @Data
    public static class Business {
        private String opentime_today;
        private String keytag;
        private String rating;
        private String tel;
        private String rectag;
        private String opentime_week;
    }
}
