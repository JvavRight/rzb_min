package com.softeem.rzb.core.controller.api;


import com.softeem.rzb.core.pojo.entity.IntegralGrade;
import com.softeem.rzb.core.service.IntegralGradeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 积分等级表 前端控制器
 * </p>
 *
 * @author min
 * @since 2024-06-27
 */
@Api(tags = "网站积分等级接口")
@RestController
@RequestMapping("/api/core/integralGrade")
public class IntegralGradeController {

    @Autowired
    private IntegralGradeService integralGradeService;

    @ApiOperation("测试接口")
    @GetMapping("/findAll")
    public List<IntegralGrade> findAll() {
        return integralGradeService.list();
    }

}

