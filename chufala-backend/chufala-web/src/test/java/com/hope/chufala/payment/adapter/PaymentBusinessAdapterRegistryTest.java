package com.hope.chufala.payment.adapter;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PaymentBusinessAdapterRegistryTest {

    @Test
    void routesByDeclaredBusinessTypeRegardlessOfClassOrBeanName() {
        PaymentBusinessAdapter hotel = adapter("HOTEL");
        PaymentBusinessAdapter membership = adapter("VIP");
        PaymentBusinessAdapterRegistry registry =
                new PaymentBusinessAdapterRegistry(List.of(hotel, membership));

        assertSame(hotel, registry.getAdapter("hotel"));
        assertSame(membership, registry.getAdapter("VIP"));
        assertThrows(IllegalArgumentException.class, () -> registry.getAdapter("TICKET"));
    }

    @Test
    void rejectsDuplicateOrBlankBusinessTypes() {
        assertThrows(IllegalStateException.class, () ->
                new PaymentBusinessAdapterRegistry(List.of(adapter("HOTEL"), adapter("hotel"))));
        assertThrows(IllegalArgumentException.class, () ->
                new PaymentBusinessAdapterRegistry(List.of(adapter(" "))));
    }

    private static PaymentBusinessAdapter adapter(String businessType) {
        PaymentBusinessAdapter adapter = mock(PaymentBusinessAdapter.class);
        when(adapter.supportedBizType()).thenReturn(businessType);
        return adapter;
    }
}
