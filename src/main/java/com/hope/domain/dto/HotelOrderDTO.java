package com.hope.domain.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelOrderDTO {
    private String rawData;
    private String signature;
    private String guestName;
    private String guestPhone;
    private String guestEmail;
    private String arrivalTime;
    private String specialRequest;
}
