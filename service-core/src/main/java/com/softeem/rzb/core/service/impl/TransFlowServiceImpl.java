package com.softeem.rzb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.softeem.rzb.core.mapper.TransFlowMapper;
import com.softeem.rzb.core.mapper.UserInfoMapper;
import com.softeem.rzb.core.pojo.bo.TransFlowBO;
import com.softeem.rzb.core.pojo.entity.TransFlow;
import com.softeem.rzb.core.pojo.entity.UserInfo;
import com.softeem.rzb.core.service.TransFlowService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 交易流水表 服务实现类
 * </p>
 *
 * @author min
 * @since 2024-06-27
 */
@Service
public class TransFlowServiceImpl extends ServiceImpl<TransFlowMapper, TransFlow> implements TransFlowService {

    @Resource
    private UserInfoMapper userInfoMapper;

    @Override
    public List<TransFlow> selectByUserId(Long userId) {

        QueryWrapper<TransFlow> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .eq("user_id", userId)
                .orderByDesc("id");
        return baseMapper.selectList(queryWrapper);
    }

    /**
     * 判断交易流水是否已经保存
     *
     * @param agentBillNo
     * @return
     */
    @Override
    public boolean isSaveTransFlow(String agentBillNo) {
        int count = baseMapper.selectCount(
                new QueryWrapper<TransFlow>()
                        .eq("trans_no", agentBillNo));
        return count > 0;// 存在返回true
    }

    /**
     * 保存交易流水
     *
     * @param transFlowBO
     */
    @Override
    public void saveTransFlow(TransFlowBO transFlowBO) {

        //获取用户基本信息 user_info
        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper.eq("bind_code", transFlowBO.getBindCode());
        UserInfo userInfo = userInfoMapper.selectOne(userInfoQueryWrapper);

        //存储交易流水数据
        TransFlow transFlow = new TransFlow();
        transFlow.setUserId(userInfo.getId());//用户id
        transFlow.setUserName(userInfo.getName());//用户姓名
        transFlow.setTransNo(transFlowBO.getAgentBillNo());//流水号
        transFlow.setTransType(transFlowBO.getTransTypeEnum().getTransType());//交易类型
        transFlow.setTransTypeName(transFlowBO.getTransTypeEnum().getTransTypeName());//交易类型名称
        transFlow.setTransAmount(transFlowBO.getAmount());//交易金额
        transFlow.setMemo(transFlowBO.getMemo());//备注
        baseMapper.insert(transFlow); //插入交易流水表
    }
}
