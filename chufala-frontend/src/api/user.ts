import request from '@/utils/request'
import type { LoginForm, RegisterForm } from '@/types/user.interface'

//注册
export const userRegisterService =(registerForm:RegisterForm)=> { 
    return request.post('/auth/register', registerForm)
}

//登录
export const userLoginService =(loginForm:LoginForm)=> { 
    return request.post('/auth/login', loginForm)
}

export const getVerifyCodeService =(email:string)=> { 
    return request.get('/auth/sendVerificationCode', {params:{email}})
}

export const getUserInfoService =()=> { 
    return request.get('/user/info')
}

export const updateUserInfoService =(userInfo:any)=> { 
    return request.put('/user/update', userInfo)
}

export const updateUserAvatarService =(avatar:any)=> { 
    return request.put('/user/updateAvatar', avatar)
}