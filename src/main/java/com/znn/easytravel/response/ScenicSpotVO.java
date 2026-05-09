package com.znn.easytravel.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScenicSpotVO {
    private String address;
    private String name;
    private String location;
    private String rating;
}
