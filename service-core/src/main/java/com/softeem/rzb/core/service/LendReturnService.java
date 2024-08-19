package com.softeem.rzb.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.softeem.rzb.core.pojo.entity.LendReturn;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 还款记录表 服务类
 * </p>
 *
 * @author min
 * @since 2024-06-27
 */
public interface LendReturnService extends IService<LendReturn> {

    List<LendReturn> selectByLendId(Long lendId);

    String commitReturn(Long lendReturnId, Long userId);


    void notify(Map<String, Object> paramMap);

}
