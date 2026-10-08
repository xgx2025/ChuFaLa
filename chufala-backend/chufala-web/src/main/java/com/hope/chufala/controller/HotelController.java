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
     * 按筛选条件和排序方式游标分页查询酒店。
     *
     * <p>首次请求不传 cursor；后续请求沿用相同筛选和排序条件，传入上次返回的 nextCursor。
     *
     * @param query 筛选条件、排序方式、页大小、用户坐标及可选游标
     * @return 酒店列表、是否有下一页及下一页游标
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
