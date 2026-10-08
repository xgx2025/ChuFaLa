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

/**
 * 景点接口。
 *
 * <p>提供景点新增（仅管理员）、分页列表与详情查询。
 *
 * @author 谢光湘
 */
@RestController
@RequestMapping("/attraction")
public class AttractionController {

    @Autowired
    private IAttractionService attractionService;
    @Autowired
    private AccessControl accessControl;
    /**
     * 新增景点（仅管理员）。
     *
     * @param attraction 景点实体
     * @return 操作结果
     */
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
    /**
     * 按评分和 ID 游标分页查询景点列表。
     *
     * <p>首次请求不传 cursor；后续请求沿用相同筛选条件，传入上次返回的 nextCursor。
     *
     * @param query 筛选条件、页大小、用户坐标及可选游标
     * @return 景点列表、是否有下一页及下一页游标；total 仅首页返回
     */
    @GetMapping("/list")
    public Result getAttractions(@ModelAttribute AttractionPageQueryDTO query) {
        PageResult<Attraction> pageResult = attractionService.queryAttraction(query);
        if (pageResult == null){
            return Result.fail(ResultCode.NO_DATA);
        }
        return Result.ok(pageResult);
    }

    /**
     * 查询景点详情。
     *
     * @param id 景点 ID
     * @return 景点详情
     */
    @GetMapping("/detail/{id}")
    public Result getAttractionDetail(@PathVariable Long id) {
        Attraction attraction = attractionService.getAttractionById(id);
        if (attraction == null){
            return Result.fail(ResultCode.NOT_FOUND);
        }
        return Result.ok(attraction);
    }
}
