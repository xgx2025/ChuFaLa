package com.hope.chufala.controller;



import com.hope.chufala.common.constant.ResultCode;
import com.hope.chufala.model.dto.AttractionPageQueryDTO;
import com.hope.chufala.model.entity.Attraction;
import com.hope.chufala.common.model.vo.PageResult;
import com.hope.chufala.common.model.vo.Result;
import com.hope.chufala.service.IAttractionService;
import com.hope.chufala.security.AccessControl;
import com.hope.chufala.common.util.ThreadLocalUtils;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/attraction")
public class AttractionController {

    @Autowired
    private IAttractionService attractionService;
    @Autowired
    private AccessControl accessControl;
    @PostMapping("/add")
    public Result add(@RequestBody Attraction attraction) {
      Claims claims = ThreadLocalUtils.get();
      accessControl.requireAdmin(claims.get("userId", Long.class));
      boolean flag = attractionService.addAttraction(attraction);
      if (!flag){
          return Result.fail(ResultCode.NO_DATA);
      }
      return Result.ok(null);
    }
    @GetMapping("/list")
    public Result getAttractions(@ModelAttribute AttractionPageQueryDTO query) {
        PageResult<Attraction> pageResult = attractionService.queryAttraction(query);
        if (pageResult == null){
            return Result.fail(ResultCode.NO_DATA);
        }
        return Result.ok(pageResult);
    }

    @GetMapping("/detail/{id}")
    public Result getAttractionDetail(@PathVariable Long id) {
        Attraction attraction = attractionService.getAttractionById(id);
        if (attraction == null){
            return Result.fail(ResultCode.NOT_FOUND);
        }
        return Result.ok(attraction);
    }
}
