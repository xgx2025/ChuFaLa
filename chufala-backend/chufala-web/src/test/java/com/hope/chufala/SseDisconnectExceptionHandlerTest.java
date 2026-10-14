package com.hope.chufala;

import com.hope.chufala.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SseDisconnectExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void disconnectedSseDoesNotProduceJsonErrorBody() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setContentType("text/event-stream;charset=UTF-8");

        assertNull(handler.handleOtherException(new IOException("连接已中止"), response));
        assertNull(handler.handleOtherException(
                new AsyncRequestNotUsableException("ServletOutputStream failed to flush"), response));
        assertNull(handler.handleRuntimeException(
                new IllegalStateException("流已经关闭", new IOException("连接已中止")), response));
    }

    @Test
    void ordinaryRequestsKeepJsonErrorHandling() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setContentType("application/json");

        assertNotNull(handler.handleOtherException(new IOException("读取失败"), response));
        assertNotNull(handler.handleRuntimeException(new IllegalStateException("状态错误"), response));
    }

    @Test
    void mvcDoesNotTryToSerializeJsonAfterSseWriteFailure() throws Exception {
        MockMvcBuilders.standaloneSetup(new BrokenStreamController())
                .setControllerAdvice(handler)
                .build()
                .perform(get("/test-sse"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @RestController
    private static class BrokenStreamController {
        @GetMapping(value = "/test-sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
        public void stream(HttpServletResponse response) throws IOException {
            response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
            throw new IOException("连接已中止");
        }
    }
}
