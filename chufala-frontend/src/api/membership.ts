import request from '@/utils/request'

// 创建会员订单
export const createMembershipOrderService = () => {
    return request.post('/membership')
}

// 创建支付
// 返回类型显式写成 Promise<string>：utils/request.ts 的响应拦截器对
// responseType === 'text' 的请求直接返回 result.data（HTML 字符串），
// 而 axios 的类型推断看不到拦截器对返回值的改写，默认会推断成 AxiosResponse。
// 用 get<string, string> 的第二泛型（R）声明真实返回类型，避免调用方报 TS2322。
export const createPayService = (bizType: string, orderId: number): Promise<string> => {
    return request.get<string, string>('/alipay/pay', {
        params: {
            bizType,
            orderId
        },
        // 明确指定响应类型为 text，以便拦截器正确处理
        responseType: 'text'
    })
}
