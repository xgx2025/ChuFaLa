package com.hope.factory;

import com.hope.service.adapter.BizAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

// 工厂类：根据业务类型获取对应的适配器
@Component
public class BizAdapterFactory {


    @Autowired
    private Map<String, BizAdapter> adapterMap; // Spring自动注入所有BizAdapter实现，key为bean名称

    // 获取适配器（bizType：HOTEL -> hotelAdapter，TICKET -> ticketAdapter）
    public BizAdapter getAdapter(String bizType) {
        String beanName = bizType.toLowerCase() + "Adapter";
        BizAdapter adapter = adapterMap.get(beanName);
        if (adapter == null) {
            throw new RuntimeException("未找到业务适配器：" + bizType);
        }
        return adapter;
    }
}