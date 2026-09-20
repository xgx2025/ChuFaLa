import type { UserInfo } from "@/types/user.interface";
import { defineStore } from "pinia";
import { ref } from "vue";


export const useUserInfoStore = defineStore(
    'userInfo',
    ()=>{
        const info = ref<UserInfo>({
            id:'',
            username:'',
            email:'',
            phone:'',
            gender:'',
            avatar:'',
            bio:'',
            status:0,
            vip:0,
            createTime:''
        })
        const setInfo = (newInfo:any)=>{
            info.value = newInfo 
        }
        const removeInfo = ()=>{
            info.value = {
                id:'',
                username:'',
                email:'',
                phone:'',
                gender:'',
                avatar:'',
                bio:'',
                status:0,
                vip:0,
                createTime:''
            }
        }

        return {info,setInfo,removeInfo}
    },
    {
        persist:true
    }
)