package com.hope.chufala.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.hope.chufala.model.vo.TravelItineraryVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(autoResultMap = true)
public class PlanHistory {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private Long userId;
    private String destination;
    private Integer people;
    private int dayNum;
    private LocalDate startDate;
    @TableField(exist = false)
    private List<String> preferences;
    private Double budget;
    private String planContent;
    @TableField(value = "plan_result",typeHandler = JacksonTypeHandler.class)
    private TravelItineraryVO planResult;
    private LocalDateTime createTime;
}
