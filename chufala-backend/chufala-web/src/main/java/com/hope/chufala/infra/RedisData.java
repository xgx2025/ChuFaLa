package com.hope.chufala.infra;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class RedisData {
    private LocalDate expireTime;
    private Object data;

}
