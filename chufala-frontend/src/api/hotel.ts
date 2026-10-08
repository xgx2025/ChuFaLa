import request from '@/utils/request'
import qs from 'qs';
export const addHotelService =(hotel:any)=> { 
    return request.post('/hotels',hotel);
}
/** 酒店列表游标分页：首次不传 cursor，后续传回 nextCursor。 */
export const getHotelListService =(hotelPageQueryDTO:any)=> { 
    return request.get('/hotels/list',{params:hotelPageQueryDTO,paramsSerializer: (params) => {
    // qs.stringify 处理参数，arrayFormat: 'repeat' 表示数组用多个相同参数名传递
    return qs.stringify(params, { arrayFormat: 'repeat' });
  }})
}

export const getHotelDetailService =(id:string)=> { 
    return request.get(`/hotels/detail/${id}`)
}

export const getRoomInfoService =(id:string)=> { 
    return request.get(`/rooms/${id}`)
}

export const getRoomAvailabilityService = (id:string, checkIn:string, checkOut:string) => {
    return request.get(`/rooms/${id}/availability`, { params: { checkIn, checkOut } })
}

export const submitOrderService =(order:any)=> { 
    return request.post('/order/submit',order)
}

export const getRoomTotalPriceService = (params:any)=> { 
    console.log(params)
    return request.get('/rooms/totalPrice',{params:params})
}
export const bookRoomService = (params:any)=> { 
    return request.post('/hotelOrders',params)
}

// 支付。同 createPayService：拦截器对 text 响应直接返回字符串，
// 用 get<string, string> 把真实返回类型标出来。
export const payOrderService = (params:any): Promise<string> => { 
    return request.get<string, string>('/alipay/pay',{params:params,responseType: 'text' })
}

/** 当前用户订单游标分页；筛选状态变化时需从首页重新请求。 */
export const getHotelOrderListService = (params:any)=> { 
    return request.get('/hotelOrders',{params:params})
}
/** 按业务订单号查询当前用户未删除的订单。 */
export const getHotelOrderDetailService = (orderId:string) => {
    return request.get(`/hotelOrders/${orderId}`)
}

export const deleteHotelOrderService = (id:string)=> { 
    return request.delete(`/hotelOrders/${id}`)
}

export const cancelHotelOrderService = (id:string)=> { 
    return request.put(`/hotelOrders/cancel/${id}`)
}
