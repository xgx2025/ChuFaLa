package com.hope.controller;

import com.hope.domain.vo.TouristAttraction;
import com.hope.service.ListPerformanceTest;
import com.hope.utils.SmartList;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Controller
public class PerformanceController {

    // 默认测试数据量为100000
    @GetMapping("/performance")
    public String showPerformance(Model model, @RequestParam(defaultValue = "100000") int dataSize) {

        // 执行性能测试
        List<TouristAttraction> arrayList = new ArrayList<>();
        List<TouristAttraction> linkedList = new LinkedList<>();
        List<TouristAttraction> smartList = new SmartList<>();

        // 传入动态数据大小
        long arrayListTime = ListPerformanceTest.testPerformance(arrayList, dataSize);
        long linkedListTime = ListPerformanceTest.testPerformance(linkedList, dataSize);
        long smartListTime = ListPerformanceTest.testPerformance(smartList, dataSize);

        // 确保时间值至少为1（避免总和为0）
        arrayListTime = Math.max(arrayListTime, 1);
        linkedListTime = Math.max(linkedListTime, 1);
        smartListTime = Math.max(smartListTime, 1);


        // 传递数据到视图
        model.addAttribute("arrayListTime", arrayListTime);
        model.addAttribute("linkedListTime", linkedListTime);
        model.addAttribute("smartListTime", smartListTime);
        model.addAttribute("dataSize", dataSize); // 回显数据大小

        return "performance";
    }
}

