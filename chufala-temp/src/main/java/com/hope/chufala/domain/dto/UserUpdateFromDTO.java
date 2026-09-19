package com.hope.chufala.domain.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateFromDTO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String username;
    private String gender;
    private String bio;
    private String avatar;
    private LocalDate birthday;
}
