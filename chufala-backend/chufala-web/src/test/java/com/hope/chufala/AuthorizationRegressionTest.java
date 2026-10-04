package com.hope.chufala;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hope.chufala.adapter.HotelAdapter;
import com.hope.chufala.adapter.VipAdapter;
import com.hope.chufala.agent.tool.OrderTools;
import com.hope.chufala.agent.tool.UserInfoTools;
import com.hope.chufala.common.exception.ForbiddenException;
import com.hope.chufala.common.exception.ResourceNotFoundException;
import com.hope.chufala.mapper.AiConversationMapper;
import com.hope.chufala.mapper.HotelOrderMapper;
import com.hope.chufala.mapper.PlanHistoryMapper;
import com.hope.chufala.model.entity.AiConversation;
import com.hope.chufala.model.entity.HotelOrder;
import com.hope.chufala.model.entity.PlanHistory;
import com.hope.chufala.model.entity.User;
import com.hope.chufala.model.entity.VipPaymentRecord;
import com.hope.chufala.security.AccessControl;
import com.hope.chufala.security.ToolUserContext;
import com.hope.chufala.service.IHotelOrderService;
import com.hope.chufala.service.IMembershipService;
import com.hope.chufala.service.IUserService;
import com.hope.chufala.service.impl.AiConversationServiceImpl;
import com.hope.chufala.service.impl.AiServiceImpl;
import com.hope.chufala.service.impl.HotelOrderServiceImpl;
import com.hope.chufala.service.impl.PlanHistoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ai.support.ToolCallbacks;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

class AuthorizationRegressionTest {
    @Test
    void onlyDatabaseAdminCanManageCatalog() {
        IUserService userService = mock(IUserService.class);
        AccessControl accessControl = new AccessControl(userService);
        User user = new User();
        user.setRole(0);
        when(userService.getUserById(7L)).thenReturn(user);

        assertThrows(ForbiddenException.class, () -> accessControl.requireAdmin(7L));
        user.setRole(2);
        assertDoesNotThrow(() -> accessControl.requireAdmin(7L));
    }

    @Test
    void cancellingAnotherUsersOrderCannotRestoreStock() {
        HotelOrderMapper mapper = mock(HotelOrderMapper.class);
        HotelOrderServiceImpl service = new HotelOrderServiceImpl();
        ReflectionTestUtils.setField(service, "hotelOrderMapper", mapper);

        assertThrows(ResourceNotFoundException.class, () -> service.cancelOrder(101L, 202L));
        verify(mapper).selectOne(org.mockito.ArgumentMatchers.<QueryWrapper<HotelOrder>>argThat(
                query -> query.getSqlSegment().contains("user_id")
                        && query.getParamNameValuePairs().containsValue(202L)));
    }

    @Test
    void deletingAnotherUsersOrderUsesOwnerCondition() {
        HotelOrderMapper mapper = mock(HotelOrderMapper.class);
        HotelOrderServiceImpl service = new HotelOrderServiceImpl();
        ReflectionTestUtils.setField(service, "hotelOrderMapper", mapper);

        assertThrows(ResourceNotFoundException.class, () -> service.deleteOrder(101L, 202L));
        verify(mapper).selectOne(org.mockito.ArgumentMatchers.<QueryWrapper<HotelOrder>>argThat(
                query -> query.getSqlSegment().contains("user_id")
                        && query.getParamNameValuePairs().containsValue(202L)));
    }

    @Test
    void paymentAdaptersRejectOrdersOwnedByAnotherUser() {
        IHotelOrderService hotelOrderService = mock(IHotelOrderService.class);
        HotelAdapter hotelAdapter = new HotelAdapter();
        ReflectionTestUtils.setField(hotelAdapter, "hotelOrderService", hotelOrderService);
        assertThrows(ResourceNotFoundException.class, () -> hotelAdapter.buildPayParam(101L, 202L));
        verify(hotelOrderService).getByOrderIdAndUserId(101L, 202L);

        IMembershipService membershipService = mock(IMembershipService.class);
        VipAdapter vipAdapter = new VipAdapter();
        ReflectionTestUtils.setField(vipAdapter, "membershipService", membershipService);
        VipPaymentRecord record = new VipPaymentRecord();
        record.setUserId(303L);
        when(membershipService.getRecordById(101L)).thenReturn(record);
        assertThrows(ResourceNotFoundException.class, () -> vipAdapter.buildPayParam(101L, 202L));
    }

    @Test
    void conversationAndPlanQueriesIncludeCurrentUser() {
        AiConversationMapper conversationMapper = mock(AiConversationMapper.class);
        AiConversationServiceImpl conversationService = new AiConversationServiceImpl();
        ReflectionTestUtils.setField(conversationService, "aiConversationMapper", conversationMapper);
        assertThrows(ResourceNotFoundException.class,
                () -> conversationService.requireConversationOwner(101L, 202L));
        verify(conversationMapper).selectOne(org.mockito.ArgumentMatchers.<QueryWrapper<AiConversation>>argThat(
                query -> query.getSqlSegment().contains("user_id")
                        && query.getParamNameValuePairs().containsValue(202L)));

        PlanHistoryMapper planMapper = mock(PlanHistoryMapper.class);
        PlanHistoryServiceImpl planService = new PlanHistoryServiceImpl();
        ReflectionTestUtils.setField(planService, "planHistoryMapper", planMapper);
        assertThrows(ResourceNotFoundException.class, () -> planService.queryPlanResult(101L, 202L));
        verify(planMapper).selectOne(org.mockito.ArgumentMatchers.<QueryWrapper<PlanHistory>>argThat(
                query -> query.getSqlSegment().contains("user_id")
                        && query.getParamNameValuePairs().containsValue(202L)));
    }

    @Test
    void agentToolsUseServerProvidedIdentity() {
        assertEquals(202L, ToolUserContext.getUserId(new ToolContext(Map.of("userId", 202L))));
        assertThrows(ForbiddenException.class,
                () -> ToolUserContext.getUserId(new ToolContext(Map.of())));
        assertEquals(4, ToolCallbacks.from(new OrderTools(), new UserInfoTools()).length);
    }

    @Test
    void anotherUsersUploadedFileIsRejectedBeforeDatabaseLookup() {
        AiServiceImpl aiService = new AiServiceImpl();
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> values = mock(ValueOperations.class);
        ReflectionTestUtils.setField(aiService, "stringRedisTemplate", redisTemplate);
        when(redisTemplate.opsForValue()).thenReturn(values);
        when(values.get("chat:upload:owner:101")).thenReturn("303");

        assertThrows(ResourceNotFoundException.class,
                () -> aiService.getFilesByIds(List.of("101"), 202L));
    }
}
