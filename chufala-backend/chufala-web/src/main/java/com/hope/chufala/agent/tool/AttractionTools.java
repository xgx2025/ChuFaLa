package com.hope.chufala.agent.tool;

import com.hope.chufala.model.entity.Attraction;
import com.hope.chufala.model.vo.AttractionInfoVO;
import com.hope.chufala.model.vo.HotelInfoVO;
import com.hope.chufala.service.IAttractionService;
import com.hope.chufala.service.IHotelService;
import jakarta.annotation.Resource;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 景点与酒店信息查询工具，供智能助手在对话中按需调用。
 *
 * <p>返回值统一为拼接后的多行文本，直接作为工具结果回填给大模型。
 *
 * @author 谢光湘
 */
@Component
public class AttractionTools {
    @Resource
    private IAttractionService attractionService;
    @Resource
    private IHotelService hotelService;

    /**
     * 按城市查询知名景点。
     *
     * @param city 城市名称
     * @return 景点信息文本（每行一条）
     */
    @Tool(description = "根据城市名称从数据库中查询该城市的知名景点信息")
    public String findAttractionByCity(@ToolParam(description = "城市名称") String city) {
        List<AttractionInfoVO> attractions = attractionService.queryAttractionByCity(city);
        StringBuilder sb = new StringBuilder();
        for (AttractionInfoVO attraction : attractions) {
            sb.append(attraction.toString()).append("\n");
        }
        System.out.println(sb);
        return sb.toString();
    }

    /**
     * 按城市查询酒店信息。
     *
     * @param city 城市名称
     * @return 酒店信息文本（每行一条）
     */
    @Tool(description = "根据城市名称从数据库中查询酒店信息")
    public String findHotelByAttraction(@ToolParam (description = "城市名称") String city) {
        List<HotelInfoVO> hotels = hotelService.findHotelByCity(city);
        StringBuilder sb = new StringBuilder();
        for (HotelInfoVO hotel : hotels) {
            sb.append(hotel.toString()).append("\n");
        }
        System.out.println(sb);
        return sb.toString();
    }

    /**
     * 按关键词检索景点。
     *
     * @param keyword 关键词
     * @return 景点信息文本（每行一条）
     */
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

    /**
     * 按 ID 查询景点详细信息（含价格与图片链接）。
     *
     * @param id 景点 ID
     * @return 景点信息文本
     */
    @Tool(description = "根据景点ID查询景点详细信息（含价格信息、图片的链接）")
    public String findAttractionById(@ToolParam(description = "景点ID") Long id) {
        Attraction attraction = attractionService.getAttractionById(id);
        return attraction.toString();
    }


}
