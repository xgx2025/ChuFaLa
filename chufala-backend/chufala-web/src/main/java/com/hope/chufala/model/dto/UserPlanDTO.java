package com.hope.chufala.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPlanDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String destination;
    private Integer people;
    private LocalDate startDate;
    private Integer dayNum;
    private List<String> preferences;
    private Double budget;
}
