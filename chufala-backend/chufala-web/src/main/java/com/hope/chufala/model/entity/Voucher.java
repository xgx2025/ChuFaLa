package com.hope.chufala.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Voucher {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
}
