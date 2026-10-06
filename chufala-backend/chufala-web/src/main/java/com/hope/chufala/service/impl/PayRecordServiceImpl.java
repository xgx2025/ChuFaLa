package com.hope.chufala.service.impl;

import com.hope.chufala.model.entity.PayRecord;
import com.hope.chufala.mapper.PayRecordMapper;
import com.hope.chufala.service.IPayRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 支付流水服务实现。
 *
 * <p>依靠 pay_record.order_id 唯一索引保证一单一记录；状态更新按主键做局部更新。
 *
 * @author 谢光湘
 */
@Service
public class PayRecordServiceImpl implements IPayRecordService {

    @Autowired
    private PayRecordMapper payRecordMapper;

    @Override
    // 冲突后的回查需看到另一请求刚提交的记录，避免复用外层事务的旧读视图。
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void ensurePayRecord(String bizType, Long orderId, Long userId, BigDecimal money) {
        PayRecord record = payRecordMapper.selectByOrderId(orderId);
        if (record == null) {
            record = new PayRecord();
            record.setUserId(userId);
            record.setBizType(bizType);
            record.setOrderId(orderId);
            record.setMoney(money);
            record.setStatus("WAIT_PAY");
            try {
                payRecordMapper.insert(record);
                return;
            } catch (DuplicateKeyException e) {
                // 另一请求先插入同一订单；唯一键保证这里只会读到那一条记录。
                record = payRecordMapper.selectByOrderId(orderId);
                if (record == null) {
                    throw e;
                }
            }
        }

        if (!"WAIT_PAY".equals(record.getStatus())
                || !userId.equals(record.getUserId()) || !bizType.equals(record.getBizType())
                || record.getMoney() == null || record.getMoney().compareTo(money) != 0) {
            throw new IllegalArgumentException("订单支付状态或金额异常");
        }
    }

    /**
     * 更新支付流水状态。
     *
     * @param payRecord 含目标状态与主键的流水记录
     * @return 是否更新成功
     */
    @Override
    public boolean updateStatus(PayRecord payRecord) {
        return payRecordMapper.updateById(payRecord)>0;
    }
}
