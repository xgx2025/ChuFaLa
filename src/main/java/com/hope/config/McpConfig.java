package com.hope.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpClientTransport;
import org.springframework.ai.mcp.client.autoconfigure.NamedClientMcpTransport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

@Configuration
public class McpConfig {

    @Bean
    public List<NamedClientMcpTransport> mcpClientTransport() {
        String apiKey = System.getenv("AMAP_API_KEY");
        McpClientTransport transport = HttpClientSseClientTransport
                .builder("https://mcp.amap.com")
                .sseEndpoint("/sse?key="+apiKey)
                .objectMapper(new ObjectMapper())
                .build();

        return Collections.singletonList(new NamedClientMcpTransport("amap", transport));
    }
}
