package com.hope.chufala.tool;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MapTools {

    private final List<McpAsyncClient> mcpAsyncClients;

    @Autowired
    public MapTools(List<McpAsyncClient> mcpAsyncClients) {
        this.mcpAsyncClients = mcpAsyncClients;
    }

    // --- 辅助方法，用于简化调用 ---
    private Mono<McpSchema.CallToolResult> callMcpTool(String toolName, Map<String, Object> arguments) {
        // 假设我们只使用第一个配置好的MCP客户端
        var mcpClient = mcpAsyncClients.get(0);
        return mcpClient.callTool(new McpSchema.CallToolRequest(toolName, arguments));
    }

    // --- 天气与定位 ---

    @Tool(description = "根据城市名称查询指定城市的天气预报")
    public Mono<String> getWeather(@ToolParam(description = "城市名称，例如：北京") String city) {
        return callMcpTool("mapsweather", Map.of("city", city))
                .map(result -> result.toString());
    }

    @Tool(description = "根据IP地址定位其所在位置")
    public Mono<String> locateByIp(@ToolParam(description = "用户的IP地址") String ip) {
        return callMcpTool("mapsiplocation", Map.of("ip", ip))
                .map(result -> result.toString());
    }

    // --- 地址与坐标转换 ---

    @Tool(description = "将详细的结构化地址转换为经纬度坐标")
    public Mono<String> geocode(@ToolParam(description = "详细地址，例如：北京市海淀区颐和园路5号") String address,
                                @ToolParam(description = "所在城市，有助于提高准确性，例如：北京") String city) {
        Map<String, Object> args = new HashMap<>();
        args.put("address", address);
        if (city != null && !city.isEmpty()) {
            args.put("city", city);
        }
        return callMcpTool("mapsgeocode", args)
                .map(result -> result.toString());
    }

    @Tool(description = "将经纬度坐标转换为详细的行政区划地址信息")
    public Mono<String> reverseGeocode(@ToolParam(description = "经纬度坐标，格式为'经度,纬度'，例如：116.397428,39.90923") String location) {
        return callMcpTool("mapsregeocode", Map.of("location", location))
                .map(result -> result.toString());
    }

    // --- POI搜索 ---

//    @Tool(description = "根据关键词在指定城市搜索相关的地点（POI）信息")
//    public Mono<String> searchPoiByKeyword(@ToolParam(description = "搜索关键词，例如：酒店、景点、美食") String keywords,
//                                           @ToolParam(description = "查询城市，例如：北京") String city) {
//        Map<String, Object> args = new HashMap<>();
//        args.put("keywords", keywords);
//        if (city != null && !city.isEmpty()) {
//            args.put("city", city);
//        }
//        return callMcpTool("mapssearchpoi", args)
//                .map(result -> result.toString());
//    }

    @Tool(description = "在指定中心点周围一定半径内搜索相关地点")
    public Mono<String> searchPoiNearby(@ToolParam(description = "搜索关键词，例如：停车场、加油站") String keywords,
                                        @ToolParam(description = "中心点经纬度，格式为'经度,纬度'") String location,
                                        @ToolParam(description = "搜索半径（单位：米），默认1000米") Integer radius) {
        Map<String, Object> args = new HashMap<>();
        args.put("keywords", keywords);
        args.put("location", location);
        if (radius != null && radius > 0) {
            args.put("radius", radius);
        }
        return callMcpTool("mapssearcharoundpoi", args)
                .map(result -> result.toString());
    }

    @Tool(description = "根据POI ID查询地点的详细信息")
    public Mono<String> getPoiDetail(@ToolParam(description = "通过搜索获取到的POI ID") String id) {
        return callMcpTool("mapssearchpoibyid", Map.of("id", id))
                .map(result -> result.toString());
    }

    // --- 路径规划 ---

    @Tool(description = "规划两点之间的驾车路线")
    public Mono<String> planDrivingRoute(@ToolParam(description = "起点经纬度，格式为'经度,纬度'") String origin,
                                         @ToolParam(description = "终点经纬度，格式为'经度,纬度'") String destination) {
        return callMcpTool("mapsdriving", Map.of("origin", origin, "destination", destination))
                .map(result -> result.toString());
    }

    @Tool(description = "规划两点之间的步行路线")
    public Mono<String> planWalkingRoute(@ToolParam(description = "起点经纬度，格式为'经度,纬度'") String origin,
                                         @ToolParam(description = "终点经纬度，格式为'经度,纬度'") String destination) {
        return callMcpTool("mapswalking", Map.of("origin", origin, "destination", destination))
                .map(result -> result.toString());
    }

    @Tool(description = "规划两点之间的骑行路线")
    public Mono<String> planBicyclingRoute(@ToolParam(description = "起点经纬度，格式为'经度,纬度'") String origin,
                                           @ToolParam(description = "终点经纬度，格式为'经度,纬度'") String destination) {
        return callMcpTool("mapsbicycling", Map.of("origin", origin, "destination", destination))
                .map(result -> result.toString());
    }

    @Tool(description = "规划两点之间的公共交通路线（公交、地铁等）")
    public Mono<String> planTransitRoute(@ToolParam(description = "起点经纬度，格式为'经度,纬度'") String origin,
                                         @ToolParam(description = "终点经纬度，格式为'经度,纬度'") String destination,
                                         @ToolParam(description = "起点城市名称") String city,
                                         @ToolParam(description = "终点城市名称，跨城时必填") String cityd) {
        Map<String, Object> args = new HashMap<>();
        args.put("origin", origin);
        args.put("destination", destination);
        args.put("city", city);
        if (cityd != null && !cityd.isEmpty()) {
            args.put("cityd", cityd);
        }
        return callMcpTool("mapstransit", args)
                .map(result -> result.toString());
    }

    // --- 实用工具 ---

    @Tool(description = "测量两个经纬度坐标之间的直线距离和驾车时间")
    public Mono<String> measureDistance(@ToolParam(description = "起点经纬度，格式为'经度,纬度'") String origin,
                                        @ToolParam(description = "终点经纬度，格式为'经度,纬度'") String destination) {
        return callMcpTool("mapsdistance", Map.of("origin", origin, "destination", destination))
                .map(result -> result.toString());
    }

    // --- 导出与唤端功能 ---

    @Tool(description = "将一份详细的行程规划导入高德地图，生成一个专属地图链接")
    public Mono<String> generatePersonalizedMap(@ToolParam(description = "行程的名称，例如：北京三日游") String tripName,
                                                @ToolParam(description = "行程的详细描述，可以包含每日的安排和途径的地点") String tripDetails) {
        // 这里需要将tripDetails格式化为高德API要求的结构，具体格式需查阅API文档
        // 假设tripDetails是一个JSON字符串或特定格式的文本
        return callMcpTool("mapsgeneratemap", Map.of("tripName", tripName, "tripDetails", tripDetails))
                .map(result -> result.toString());
    }

    @Tool(description = "根据目的地经纬度，生成一个高德导航唤端链接，用户点击后可直接启动高德地图App进行导航")
    public Mono<String> startNavigation(@ToolParam(description = "目的地的经纬度，格式为'经度,纬度'") String destination) {
        return callMcpTool("mapsnavigation", Map.of("destination", destination))
                .map(result -> result.toString());
    }

    @Tool(description = "根据起点和终点经纬度，生成一个高德打车唤端链接")
    public Mono<String> requestRide(@ToolParam(description = "起点经纬度，格式为'经度,纬度'") String origin,
                                    @ToolParam(description = "终点经纬度，格式为'经度,纬度'") String destination) {
        return callMcpTool("mapsrequestride", Map.of("origin", origin, "destination", destination))
                .map(result -> result.toString());
    }
}

