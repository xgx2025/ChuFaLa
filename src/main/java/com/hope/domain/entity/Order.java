package com.hope.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 接口次数订单表
 * @TableName order
 */
@TableName(value ="`order`")
@Data
public class Order implements Serializable {
    /**
     * 主键Id
     */
    private Long id;
    /**
     * 订单Id
     */
    private Long orderId;

    /**
     * 用户Id
     */
    private Long userId;

    /**
     * 接口Id
     */
    private String subject;

    /**
     * 支付金额
     */
    private Double money;

    /**
     * 支付方式
     */
    private String paymentMethod;

    /**
     * 0 - 未支付 1 - 已支付
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 支付时间
     */
    private Date payTime;

    /**
     * 是否删除
     */
    private Integer isDelete;

}
