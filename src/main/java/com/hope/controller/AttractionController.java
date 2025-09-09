package com.hope.controller;


import com.hope.constant.ResultCode;
import com.hope.domain.entity.Attraction;
import com.hope.domain.vo.Result;
import com.hope.service.IAttractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/attraction")
public class AttractionController {

    @Autowired
    private IAttractionService attractionService;
    @PostMapping("/add")
    public Result add(@RequestBody Attraction attraction) {
      boolean flag = attractionService.addAttraction(attraction);
      if (!flag){
          return Result.fail(ResultCode.UNKNOWN_ERROR);
      }
      return Result.ok(null);
    }
}
