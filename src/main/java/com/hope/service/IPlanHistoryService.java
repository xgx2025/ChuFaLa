package com.hope.service;

import com.hope.domain.entity.PlanHistory;
import org.springframework.stereotype.Service;

import java.util.List;


public interface IPlanHistoryService {

    List<PlanHistory> queryHistory(Long userId);
}
