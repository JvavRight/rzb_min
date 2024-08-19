package com.softeem.rzb.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.softeem.rzb.core.pojo.bo.TransFlowBO;
import com.softeem.rzb.core.pojo.entity.TransFlow;

import java.util.List;

/**
 * <p>
 * 交易流水表 服务类
 * </p>
 *
 * @author min
 * @since 2024-06-27
 */
public interface TransFlowService extends IService<TransFlow> {

    boolean isSaveTransFlow(String agentBillNo);

    void saveTransFlow(TransFlowBO transFlowBO);

    List<TransFlow> selectByUserId(Long userId);
}
