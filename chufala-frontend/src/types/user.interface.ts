export interface LoginForm {
  phone: string
  password: string
}

export interface RegisterForm {
  username: string
  email: string
  password: string
  verifyCode: number
}

export interface UserInfo {
    id:string,
    username:string,
    email:string,
    phone:string,
    gender:string,
    avatar:string,
    bio:string,
    status:number,
    vip:number,
    createTime:string
}