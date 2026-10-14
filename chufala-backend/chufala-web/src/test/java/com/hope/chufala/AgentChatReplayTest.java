package com.hope.chufala;

import com.hope.chufala.common.exception.ResourceNotFoundException;
import com.hope.chufala.common.util.ThreadLocalUtils;
import com.hope.chufala.controller.AgentController;
import com.hope.chufala.infra.ChatStreamManager;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class AgentChatReplayTest {
    @Test
    void ownerCanResumeWithoutResubmittingQuestion() {
        AgentController controller = new AgentController();
        ChatStreamManager streams = mock(ChatStreamManager.class);
        SseEmitter emitter = mock(SseEmitter.class);
        ReflectionTestUtils.setField(controller, "chatStreamManager", streams);
        when(streams.belongsTo("42", 7L)).thenReturn(true);
        when(streams.subscribe("42", "3")).thenReturn(emitter);
        Claims claims = Jwts.claims();
        claims.put("userId", 7L);
        ThreadLocalUtils.set(claims);
        try {
            assertSame(emitter, controller.subscribeChat("42", "3"));
            verify(streams).subscribe("42", "3");
        } finally {
            ThreadLocalUtils.remove();
        }
    }

    @Test
    void anotherUserCannotReplayChat() {
        AgentController controller = new AgentController();
        ChatStreamManager streams = mock(ChatStreamManager.class);
        ReflectionTestUtils.setField(controller, "chatStreamManager", streams);
        Claims claims = Jwts.claims();
        claims.put("userId", 8L);
        ThreadLocalUtils.set(claims);
        try {
            assertThrows(ResourceNotFoundException.class,
                    () -> controller.subscribeChat("42", "3"));
            verify(streams, never()).subscribe(anyString(), any());
        } finally {
            ThreadLocalUtils.remove();
        }
    }
}
