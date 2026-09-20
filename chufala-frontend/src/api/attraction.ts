import request from '@/utils/request'
import qs from 'qs';
export const getAttractionList =(attractionPageQueryDTO:any)=> { 
    return request.get('/attraction/list',{params:attractionPageQueryDTO,paramsSerializer: (params) => {
    // qs.stringify 处理参数，arrayFormat: 'repeat' 表示数组用多个相同参数名传递
    return qs.stringify(params, { arrayFormat: 'repeat' });
  }})
}


export const getAttractionDetailService =(id:string)=> { 
    return request.get(`/attraction/detail/${id}`)
}