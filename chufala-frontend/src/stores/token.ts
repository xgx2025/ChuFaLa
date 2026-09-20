import { defineStore } from "pinia";
import { ref } from "vue";


export const useTokenStore = defineStore(
    'token',
    ()=>{
        const accessToken = ref('')
        const refreshToken = ref('')

        const setToken = (newAccessToken:string,newRefreshToken:string)=>{
            accessToken.value = newAccessToken;
            refreshToken.value = newRefreshToken
        }
        const removeToken = ()=>{
            accessToken.value = ''
            refreshToken.value = ''
        }
        return {
            accessToken,refreshToken,setToken,removeToken
        }
    },
    {
        persist:true
    }
)