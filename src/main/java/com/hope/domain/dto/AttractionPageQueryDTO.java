package com.hope.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttractionPageQueryDTO {
    private String keyword;
    private Integer page;
    private Integer size;
    private Integer stars;
    private String sortBy;
    private String city;
    private List<String> tags;
    private Double userLat;
    private Double userLng;

}
