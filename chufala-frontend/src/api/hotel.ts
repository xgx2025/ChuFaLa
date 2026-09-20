import request from '@/utils/request'
import qs from 'qs';
export const addHotelService =(hotel:any)=> { 
    return request.post('/hotels',hotel);
}
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

export const getHotelOrderListService = (params:any)=> { 
    return request.get('/hotelOrders',{params:params})
}

export const deleteHotelOrderService = (id:string)=> { 
    return request.delete(`/hotelOrders/${id}`)
}

export const cancelHotelOrderService = (id:string)=> { 
    return request.put(`/hotelOrders/cancel/${id}`)
}