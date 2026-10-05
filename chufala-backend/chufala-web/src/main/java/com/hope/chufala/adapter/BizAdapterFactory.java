package com.hope.chufala.adapter;

import com.hope.chufala.adapter.BizAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 业务适配器工厂。
 *
 * <p>借助 Spring 注入 {@code Map<String, BizAdapter>}（key 为 Bean 名），
 * 按 {@code bizType.toLowerCase() + "Adapter"} 的约定取用具体适配器。
 *
 * @author 谢光湘
 */
// 工厂类：根据业务类型获取对应的适配器
@Component
public class BizAdapterFactory {


    @Autowired
    private Map<String, BizAdapter> adapterMap; // Spring自动注入所有BizAdapter实现，key为bean名称

    // 获取适配器（bizType：HOTEL -> hotelAdapter，TICKET -> ticketAdapter）
    /**
     * 按业务类型获取适配器。
     *
     * @param bizType 业务类型（HOTEL / TICKET / VIP）
     * @return 对应适配器
     */
    public BizAdapter getAdapter(String bizType) {
        String beanName = bizType.toLowerCase() + "Adapter";
        BizAdapter adapter = adapterMap.get(beanName);
        if (adapter == null) {
            throw new RuntimeException("未找到业务适配器：" + bizType);
        }
        return adapter;
    }
}
