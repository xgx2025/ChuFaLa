import request from '@/utils/request'
import { useTokenStore } from '@/stores/token'

export const getTripPlanService =(message:any)=> { 
    return request.post('/agent/plan',message);

}
export const getPlanHistoryService = ()=>{
    return request.get('/agent/plan/history');
}
export const getHistoricalItineraryService = (id:any)=>{
    return request.get(`/agent/plan/${id}`);
}

export const sendChatStream = async (data: { message: string; conversationId?: string | null; model?: string; planId?: string | number | null; fileIds?: string[] }) => {
  const tokenStore = useTokenStore()
  return fetch('/api/agent/chat', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${tokenStore.accessToken}`
    },
    body: JSON.stringify(data)
  })
}

export const getChatHistoryService = () => {
    return request.get('/agent/chat/history');
}

export const getChatMessagesService = (id: any) => {
    return request.get(`/agent/chat/${id}`);
}

export const deleteConversationService = (id: any) => {
    return request.delete(`/agent/chat/${id}`);
}

export const uploadChatImageService = (formData: FormData) => {
    return request.post('/agent/chat-image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
}