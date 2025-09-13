package com.hope.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelFormDTO {
    private String name;
    private Integer type;
    private String city;
    private String address;
    private Double price;
    private String phone;
    private String coverImage;
    private String description;
    private Double longitude;
    private Double latitude;
    private Integer starRating;
}
