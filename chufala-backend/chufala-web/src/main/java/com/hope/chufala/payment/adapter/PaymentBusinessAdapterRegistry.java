package com.hope.chufala.payment.adapter;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** 按业务类型路由支付适配器，不依赖 Spring Bean 名。 */
@Component
public class PaymentBusinessAdapterRegistry {

    private final Map<String, PaymentBusinessAdapter> adapters;

    public PaymentBusinessAdapterRegistry(List<PaymentBusinessAdapter> adapters) {
        Map<String, PaymentBusinessAdapter> byType = new HashMap<>();
        for (PaymentBusinessAdapter adapter : adapters) {
            String type = normalize(adapter.supportedBizType());
            if (byType.putIfAbsent(type, adapter) != null) {
                throw new IllegalStateException("重复的支付业务适配器：" + type);
            }
        }
        this.adapters = Map.copyOf(byType);
    }

    public PaymentBusinessAdapter getAdapter(String bizType) {
        String type = normalize(bizType);
        PaymentBusinessAdapter adapter = adapters.get(type);
        if (adapter == null) {
            throw new IllegalArgumentException("未找到业务适配器：" + type);
        }
        return adapter;
    }

    private static String normalize(String bizType) {
        if (bizType == null || bizType.isBlank()) {
            throw new IllegalArgumentException("业务类型不能为空");
        }
        return bizType.trim().toUpperCase(Locale.ROOT);
    }
}
