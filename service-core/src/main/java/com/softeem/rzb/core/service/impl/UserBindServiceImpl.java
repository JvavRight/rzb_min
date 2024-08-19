package com.softeem.rzb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.softeem.rzb.common.exception.Assert;
import com.softeem.rzb.common.result.ResponseEnum;
import com.softeem.rzb.core.enums.UserBindEnum;
import com.softeem.rzb.core.hfb.FormHelper;
import com.softeem.rzb.core.hfb.HfbConst;
import com.softeem.rzb.core.hfb.RequestHelper;
import com.softeem.rzb.core.mapper.UserBindMapper;
import com.softeem.rzb.core.mapper.UserInfoMapper;
import com.softeem.rzb.core.pojo.entity.UserBind;
import com.softeem.rzb.core.pojo.entity.UserInfo;
import com.softeem.rzb.core.pojo.vo.UserBindVO;
import com.softeem.rzb.core.service.UserBindService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 用户绑定表 服务实现类
 * </p>
 *
 * @author min
 * @since 2024-06-27
 */
@Service
public class UserBindServiceImpl extends ServiceImpl<UserBindMapper, UserBind> implements UserBindService {

    @Resource
    private UserInfoMapper userInfoMapper;

    /**
     * 获取绑定账户(借款人或投资人)的绑定协议号
     *
     * @param userId
     * @return
     */
    @Override
    public String getBindCodeByUserId(Long userId) {
        QueryWrapper<UserBind> userBindQueryWrapper = new QueryWrapper<>();
        userBindQueryWrapper.eq("user_id", userId);
        UserBind userBind = baseMapper.selectOne(userBindQueryWrapper);
        return userBind.getBindCode();
    }

    /**
     * 用户绑定
     *
     * @param paramMap
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void notify(Map<String, Object> paramMap) {

        String bindCode = (String) paramMap.get("bindCode");
        //会员id
        String agentUserId = (String) paramMap.get("agentUserId");

        //根据user_id查询user_bind记录
        UserBind userBind = baseMapper.selectOne(
                new QueryWrapper<UserBind>().eq("user_id", agentUserId));
        userBind.setBindCode(bindCode);
        userBind.setStatus(UserBindEnum.BIND_OK.getStatus());
        //更新用户绑定表
        baseMapper.updateById(userBind);

        //更新用户表
        UserInfo userInfo = userInfoMapper.selectById(agentUserId);
        userInfo.setBindCode(bindCode);
        userInfo.setName(userBind.getName());
        userInfo.setIdCard(userBind.getIdCard());
        userInfo.setBindStatus(UserBindEnum.BIND_OK.getStatus());
        userInfoMapper.updateById(userInfo);
    }

    /**
     * 提交绑定
     *
     * @param userBindVO 用户绑定对象
     * @param userId     用户id
     * @return
     */
    @Override
    public String commitBindUser(UserBindVO userBindVO, Long userId) {

        //查询身份证号码是否在其它账户绑定
        UserBind userBind = baseMapper.selectOne(
                new QueryWrapper<UserBind>()
                        .eq("id_card", userBindVO.getIdCard())
                        .ne("user_id", userId));
        //USER_BIND_IDCARD_EXIST_ERROR(-301, "身份证号码已绑定")
        Assert.isNull(userBind, ResponseEnum.USER_BIND_IDCARD_EXIST_ERROR);

        //查询用户绑定信息
        userBind = baseMapper.selectOne(new QueryWrapper<UserBind>().eq("user_id", userId));

        //判断是否有绑定记录
        if (userBind == null) {
            //如果未创建绑定记录，则创建一条记录
            userBind = new UserBind();
            BeanUtils.copyProperties(userBindVO, userBind);
            userBind.setUserId(userId);
            userBind.setStatus(UserBindEnum.NO_BIND.getStatus());
            baseMapper.insert(userBind);
        } else {
            //曾经跳转到托管平台，但是未操作完成，此时将用户最新填写的数据同步到userBind对象
            BeanUtils.copyProperties(userBindVO, userBind);
            baseMapper.updateById(userBind);// 修改用户的绑定信息
        }

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("agentId", HfbConst.AGENT_ID);
        paramMap.put("agentUserId", userId);
        paramMap.put("idCard", userBindVO.getIdCard());
        paramMap.put("personalName", userBindVO.getName());
        paramMap.put("bankType", userBindVO.getBankType());
        paramMap.put("bankNo", userBindVO.getBankNo());
        paramMap.put("mobile", userBindVO.getMobile());
        paramMap.put("returnUrl", HfbConst.USERBIND_RETURN_URL);
        paramMap.put("notifyUrl", HfbConst.USERBIND_NOTIFY_URL);
        paramMap.put("timestamp", RequestHelper.getTimestamp());
        paramMap.put("sign", RequestHelper.getSign(paramMap));

        //构建充值自动提交表单
        String formStr = FormHelper.buildForm(HfbConst.USERBIND_URL, paramMap);
        System.out.println("formStr = " + formStr);
        return formStr;

    }

}
