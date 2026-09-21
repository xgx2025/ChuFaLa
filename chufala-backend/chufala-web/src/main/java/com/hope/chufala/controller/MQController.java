//package com.hope.controller;
//
//import com.hope.service.MessageProducer;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/mq")
//public class MQController {
//    @Autowired
//    private MessageProducer messageProducer;
//    @RequestMapping("/send")
//    public String send(){
//        messageProducer.sendMessage("boot.exchange","boot.routing.key","xgx：hello world");
//        return "success";
//    }
//}
