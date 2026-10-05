package com.hope.chufala;

import com.hope.chufala.controller.PayController;
import com.hope.chufala.service.IAlipayService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class PayControllerReturnTest {
    @Test
    void browserReturnOnlyRedirectsAndDoesNotChangePaymentState() {
        PayController controller = new PayController();
        IAlipayService alipayService = mock(IAlipayService.class);
        ReflectionTestUtils.setField(controller, "alipayService", alipayService);
        ReflectionTestUtils.setField(controller, "frontendReturnUrl", "http://localhost:8881/payment/return");

        ResponseEntity<Void> response = controller.payReturn();

        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertEquals(URI.create("http://localhost:8881/payment/return"), response.getHeaders().getLocation());
        verifyNoInteractions(alipayService);
    }
}
