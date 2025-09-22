package com.hope.controller;

import com.hope.constant.ResultCode;
import com.hope.domain.dto.HotelPageQueryDTO;
import com.hope.domain.entity.Hotel;
import com.hope.domain.vo.PageResult;
import com.hope.domain.vo.Result;
import com.hope.service.IHotelOrderService;
import com.hope.service.IHotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hotel")
public class HotelController {

    @Autowired
    private IHotelService hotelService;

    @RequestMapping("/add")
    public Result add(@RequestBody Hotel hotel) {
        boolean flag = hotelService.addHotel(hotel);
        if (!flag){
            return Result.fail(ResultCode.UNKNOWN_ERROR);
        }
        return Result.ok(null);
    }

    @GetMapping("/detail/{id}")
    public Result getHotelDetail(@PathVariable Long id) {
        Hotel hotel = hotelService.getHotelDetail(id);
        if (hotel == null){
            return Result.fail(ResultCode.NOT_FOUND);
        }
        return Result.ok(hotel);
    }
    // 普通分页：查询全部酒店
    @GetMapping("/all")
    public Result getAllHotels(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        PageResult<Hotel> pageResult = hotelService.queryAllHotels(page, size);
        if (pageResult == null){
            return Result.fail(ResultCode.UNKNOWN_ERROR);
        }
        return Result.ok(pageResult);
    }

    // 普通分页：按评分排名查询
    @GetMapping("/list")
    public Result getHotelsByScoreRank(@ModelAttribute HotelPageQueryDTO query) {
        PageResult<Hotel> pageResult = hotelService.queryHotelsByScoreRank(query);
        if (pageResult == null){
            return Result.fail(ResultCode.UNKNOWN_ERROR);
        }
        return Result.ok(pageResult);
    }



//    @GetMapping("/submit-order")
//    public Result submitOrder(){
//
//    }




    // 提交评论
//    @PostMapping("/comment")
//    public String submitComment(@RequestBody HotelReview review) {
//        hotelService.submitComment(review);
//        return "评论提交成功";
//    }
}
