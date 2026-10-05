package com.hope.chufala.controller.dev;

import com.hope.chufala.common.util.SmartList;
import com.hope.chufala.common.util.ThreadLocalUtils;
import com.hope.chufala.security.AccessControl;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import com.hope.chufala.model.vo.TouristAttractionVO;
import com.hope.chufala.perf.ListBenchmark;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 列表性能对比页（开发工具，仅管理员可访问）。
 *
 * <p>对比 ArrayList / LinkedList / SmartList 在给定数据量下的写入耗时，
 * 结果渲染到 performance 模板页。
 *
 * @author 谢光湘
 */
@Controller
@RequiredArgsConstructor
public class PerformanceController {
    private final AccessControl accessControl;

    // 默认测试数据量为100000
    /**
     * 执行列表性能基准测试并渲染结果页。
     *
     * @param model    视图模型
     * @param dataSize 测试数据量，默认 100000
     * @return 视图名 performance
     */
    @GetMapping("/performance")
    public String showPerformance(Model model, @RequestParam(defaultValue = "100000") int dataSize) {
        Claims claims = ThreadLocalUtils.get();
        accessControl.requireAdmin(claims.get("userId", Long.class));

        // 执行性能测试
        List<TouristAttractionVO> arrayList = new ArrayList<>();
        List<TouristAttractionVO> linkedList = new LinkedList<>();
        List<TouristAttractionVO> smartList = new SmartList<>();

        // 传入动态数据大小
        long arrayListTime = ListBenchmark.testPerformance(arrayList, dataSize);
        long linkedListTime = ListBenchmark.testPerformance(linkedList, dataSize);
        long smartListTime = ListBenchmark.testPerformance(smartList, dataSize);

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
