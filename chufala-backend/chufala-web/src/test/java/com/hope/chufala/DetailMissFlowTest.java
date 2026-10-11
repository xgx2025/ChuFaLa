package com.hope.chufala;

import com.hope.chufala.infra.DetailMissCache;
import com.hope.chufala.mapper.AttractionMapper;
import com.hope.chufala.mapper.HotelMapper;
import com.hope.chufala.service.impl.AttractionServiceImpl;
import com.hope.chufala.service.impl.HotelServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DetailMissFlowTest {
    @Test
    void missingAttractionReturnsNullAndRemembersId() {
        AttractionServiceImpl service = new AttractionServiceImpl();
        AttractionMapper mapper = mock(AttractionMapper.class);
        DetailMissCache cache = mock(DetailMissCache.class);
        ReflectionTestUtils.setField(service, "attractionMapper", mapper);
        ReflectionTestUtils.setField(service, "detailMissCache", cache);

        assertNull(service.getAttractionById(42L));
        verify(mapper).selectById(42L);
        verify(cache).rememberMissing("attraction", 42L);
        when(cache.isMissing("attraction", 42L)).thenReturn(true);
        assertNull(service.getAttractionById(42L));
        verifyNoMoreInteractions(mapper);
    }

    @Test
    void missingHotelReturnsNullAndRemembersId() {
        HotelServiceImpl service = new HotelServiceImpl();
        HotelMapper mapper = mock(HotelMapper.class);
        DetailMissCache cache = mock(DetailMissCache.class);
        ReflectionTestUtils.setField(service, "hotelMapper", mapper);
        ReflectionTestUtils.setField(service, "detailMissCache", cache);

        assertNull(service.getHotelDetail(42L));
        verify(mapper).selectById(42L);
        verify(cache).rememberMissing("hotel", 42L);
        when(cache.isMissing("hotel", 42L)).thenReturn(true);
        assertNull(service.getHotelDetail(42L));
        verifyNoMoreInteractions(mapper);
    }
}
