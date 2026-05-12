package com.znn.easytravel.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 高德地图逆地理编码响应实体类
 * 对应你提供的 JSON 数据结构
 */
@Data
@NoArgsConstructor
public class RegeoResponse {

    private String status;
    private String info;
    private String infocode;

    @JsonProperty("regeocode")
    private Regeocode regeocode;

    /**
     * 逆地理编码信息
     */
    @Data
    @NoArgsConstructor
    public static class Regeocode {

        @JsonProperty("formatted_address")
        private String formattedAddress;

        @JsonProperty("addressComponent")
        private AddressComponent addressComponent;

        private List<Poi> pois;
        private List<Road> roads;
        private List<Roadinter> roadinters;
        private List<Aoi> aois;

        /**
         * 地址组件
         */
        @Data
        @NoArgsConstructor
        public static class AddressComponent {
            private String country;
            private String province;
            private String city;
            private String citycode;
            private String district;
            private String adcode;
            private String township;
            private String towncode;

            @JsonProperty("neighborhood")
            private NameType neighborhood;

            @JsonProperty("building")
            private NameType building;

            @JsonProperty("streetNumber")
            private StreetNumber streetNumber;

            @JsonProperty("businessAreas")
            private List<List<Object>> businessAreas;

            /**
             * 通用名称-类型结构（用于 neighborhood 和 building）
             */
            @Data
            @NoArgsConstructor
            public static class NameType {
                private List<String> name;
                private List<String> type;
            }

            /**
             * 门牌信息
             */
            @Data
            @NoArgsConstructor
            public static class StreetNumber {
                private String street;
                private String number;
                private String location;
                private String direction;
                private String distance;
            }
        }

        /**
         * 兴趣点 (POI)
         */
        @Data
        @NoArgsConstructor
        public static class Poi {
            private String id;
            private String name;
            private String type;
            private String tel;
            private String direction;
            private String distance;
            private String location;
            private String address;

            @JsonProperty("poiweight")
            private String poiWeight;

            @JsonProperty("businessarea")
            private List<Object> businessArea;
        }

        /**
         * 道路信息
         */
        @Data
        @NoArgsConstructor
        public static class Road {
            private String id;
            private String name;
            private String direction;
            private String distance;
            private String location;
        }

        /**
         * 道路交叉口
         */
        @Data
        @NoArgsConstructor
        public static class Roadinter {
            private String direction;
            private String distance;
            private String location;

            @JsonProperty("first_id")
            private String firstId;

            @JsonProperty("first_name")
            private String firstName;

            @JsonProperty("second_id")
            private String secondId;

            @JsonProperty("second_name")
            private String secondName;
        }

        /**
         * 区域兴趣点 (AOI)
         */
        @Data
        @NoArgsConstructor
        public static class Aoi {
            private String id;
            private String name;
            private String adcode;
            private String location;
            private String area;
            private String distance;
            private String type;
        }
    }
}
