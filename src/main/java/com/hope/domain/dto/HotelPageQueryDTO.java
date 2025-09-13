package com.hope.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelPageQueryDTO {
    private Integer page;
    private Integer size;
    private Integer stars;
    private String city;
    private Double maxPrice;
    private Double minPrice;
    private List<String> facilities;
    private Double userLat;
    private Double userLng;
}
