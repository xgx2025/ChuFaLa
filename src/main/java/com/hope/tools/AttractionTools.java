package com.hope.tools;

import com.hope.domain.entity.Attraction;
import com.hope.domain.vo.AttractionInfo;
import com.hope.domain.vo.HotelInfo;
import com.hope.service.IAttractionService;
import com.hope.service.IHotelService;
import jakarta.annotation.Resource;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AttractionTools {
    @Resource
    private IAttractionService attractionService;
    @Resource
    private IHotelService hotelService;

    @Tool(description = "根据城市名称从数据库中查询该城市的知名景点信息")
    public String findAttractionByCity(String city) {
        List<AttractionInfo> attractions = attractionService.queryAttractionByCity(city);
        StringBuilder sb = new StringBuilder();
        for (AttractionInfo attraction : attractions) {
            sb.append(attraction.toString()).append("\n");
        }
        System.out.println(sb);
        return sb.toString();
    }

    @Tool(description = "根据城市名称从数据库中查询酒店信息")
    public String findHotelByAttraction(String city) {
        List<HotelInfo> hotels = hotelService.findHotelByAttraction(city);
        StringBuilder sb = new StringBuilder();
        for (HotelInfo hotel : hotels) {
            sb.append(hotel.toString()).append("\n");
        }
        System.out.println(sb);
        return sb.toString();
    }


}