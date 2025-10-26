package com.hope.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hope.domain.dto.AttractionPageQueryDTO;
import com.hope.domain.entity.Attraction;
import com.hope.domain.vo.AttractionInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AttractionMapper extends BaseMapper<Attraction> {
    List<Attraction> selectAttractionPage( Integer offset, Integer size,String keyword, Integer stars, String city, List<String> tags);
    List<AttractionInfo> selectAttractionByCity(String city);
    List<String> findAttractionImage(Long attractionId);
}
