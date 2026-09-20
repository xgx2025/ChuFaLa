import request from '@/utils/request'

// 创建会员订单
export const createMembershipOrderService = () => {
    return request.post('/membership')
}

// 创建支付
export const createPayService = (bizType: string, orderId: number) => {
    return request.get('/alipay/pay', {
        params: {
            bizType,
            orderId
        },
        // 明确指定响应类型为 text，以便拦截器正确处理
        responseType: 'text'
    })
}
