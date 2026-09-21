package com.hope.chufala.agent.tool;

import com.hope.chufala.model.entity.Attraction;
import com.hope.chufala.model.vo.AttractionInfo;
import com.hope.chufala.model.vo.HotelInfo;
import com.hope.chufala.service.IAttractionService;
import com.hope.chufala.service.IHotelService;
import jakarta.annotation.Resource;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;

@Component
public class AttractionTools {
    @Resource
    private IAttractionService attractionService;
    @Resource
    private IHotelService hotelService;

    @Tool(description = "根据城市名称从数据库中查询该城市的知名景点信息")
    public String findAttractionByCity(@ToolParam(description = "城市名称") String city) {
        List<AttractionInfo> attractions = attractionService.queryAttractionByCity(city);
        StringBuilder sb = new StringBuilder();
        for (AttractionInfo attraction : attractions) {
            sb.append(attraction.toString()).append("\n");
        }
        System.out.println(sb);
        return sb.toString();
    }

    @Tool(description = "根据城市名称从数据库中查询酒店信息")
    public String findHotelByAttraction(@ToolParam (description = "城市名称") String city) {
        List<HotelInfo> hotels = hotelService.findHotelByCity(city);
        StringBuilder sb = new StringBuilder();
        for (HotelInfo hotel : hotels) {
            sb.append(hotel.toString()).append("\n");
        }
        System.out.println(sb);
        return sb.toString();
    }

    @Tool(description = "根据关键词查询景点基本信息")
    public String searchAttractionsByKeyword(@ToolParam(description = "关键词") String keyword) {
        List<Attraction> attractions = attractionService.searchAttractionsByKeyword(keyword);
        StringBuilder sb = new StringBuilder();
        for (Attraction attraction : attractions) {
            sb.append(attraction.toString()).append("\n");
        }
        System.out.println(sb);
        return sb.toString();
    }

    @Tool(description = "根据景点ID查询景点详细信息（含价格信息、图片的链接）")
    public String findAttractionById(@ToolParam(description = "景点ID") Long id) {
        Attraction attraction = attractionService.getAttractionById(id);
        return attraction.toString();
    }


}