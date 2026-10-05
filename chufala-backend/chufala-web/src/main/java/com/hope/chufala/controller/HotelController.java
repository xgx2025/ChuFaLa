package com.hope.chufala.controller;


import com.hope.chufala.common.constant.ResultCode;
import com.hope.chufala.model.dto.HotelPageQueryDTO;
import com.hope.chufala.model.entity.Hotel;
import com.hope.chufala.common.model.vo.PageResult;
import com.hope.chufala.common.model.vo.Result;
import com.hope.chufala.service.IHotelService;
import com.hope.chufala.security.AccessControl;
import com.hope.chufala.common.util.ThreadLocalUtils;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 酒店接口。
 *
 * <p>提供酒店新增（仅管理员）、详情查询与列表查询。
 *
 * @author 谢光湘
 */
@RestController
@RequestMapping("/hotels")
public class HotelController {

    @Autowired
    private IHotelService hotelService;
    @Autowired
    private AccessControl accessControl;

    /**
     * 新增酒店（仅管理员）。
     *
     * @param hotel 酒店实体
     * @return 操作结果
     */
    @PostMapping
    public Result add(@RequestBody Hotel hotel) {
        Claims claims = ThreadLocalUtils.get();
        accessControl.requireAdmin(claims.get("userId", Long.class));
        boolean flag = hotelService.addHotel(hotel);
        if (!flag){
            return Result.fail(ResultCode.UNKNOWN_ERROR);
        }
        return Result.ok(null);
    }

    /**
     * 查询酒店详情。
     *
     * @param id 酒店 ID
     * @return 酒店详情
     */
    @GetMapping("/detail/{id}")
    public Result getHotelDetail(@PathVariable Long id) {
        Hotel hotel = hotelService.getHotelDetail(id);
        if (hotel == null){
            return Result.fail(ResultCode.NOT_FOUND);
        }
        return Result.ok(hotel);
    }

    /**
     * 普通分页：查询全部酒店
     *
     * @param page 页码，从 1 开始
     * @param size 每页大小
     * @return 分页结果
     */
    @GetMapping
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

    /**
     * 普通分页：按评分排名查询
     *
     * @param query 查询条件
     * @return 分页结果
     */
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
