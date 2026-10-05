package com.hope.chufala.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpClientTransport;
import org.springframework.ai.mcp.client.autoconfigure.NamedClientMcpTransport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

/**
 * MCP 客户端传输配置。
 *
 * <p>以 SSE 方式接入高德地图 MCP Server，注入 NamedClientMcpTransport 供 Spring AI
 * 建立 MCP 客户端；MapTools 中的地图工具即通过该客户端调用。
 *
 * @author 谢光湘
 */
@Configuration
public class McpConfig {

    /** 高德地图 MCP Key，统一由 application.yml 的 amap.api-key 提供 */
    @Value("${amap.api-key}")
    private String amapApiKey;

    /**
     * 声明高德 MCP 传输（命名为 amap）。
     *
     * @return MCP 传输列表
     */
    @Bean
    public List<NamedClientMcpTransport> mcpClientTransport() {
        McpClientTransport transport = HttpClientSseClientTransport
                .builder("https://mcp.amap.com")
                .sseEndpoint("/sse?key=" + amapApiKey)
                .objectMapper(new ObjectMapper())
                .build();

        return Collections.singletonList(new NamedClientMcpTransport("amap", transport));
    }
}
