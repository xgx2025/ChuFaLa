package com.hope.chufala.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.A;

@Data
@NoArgsConstructor
public class Point {
    private Double longitude;
    private Double latitude;
}
