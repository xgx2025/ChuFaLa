package com.hope.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    // 错误处理的请求路径（SpringBoot默认映射到/error）
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        // 获取状态码
        Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");

        // 根据状态码返回不同页面
        if (statusCode == 404) {
            // 向页面传递自定义数据
            model.addAttribute("path", request.getAttribute("jakarta.servlet.error.request_uri"));
            return "error/404"; // 对应templates/error/404.html
        }

        // 其他错误默认页面
        return "error/default";
    }
}

