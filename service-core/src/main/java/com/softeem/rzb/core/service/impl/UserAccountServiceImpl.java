package com.softeem.rzb.core.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.softeem.rzb.base.dto.SmsDTO;
import com.softeem.rzb.common.exception.Assert;
import com.softeem.rzb.common.result.ResponseEnum;
import com.softeem.rzb.core.enums.TransTypeEnum;
import com.softeem.rzb.core.hfb.FormHelper;
import com.softeem.rzb.core.hfb.HfbConst;
import com.softeem.rzb.core.hfb.RequestHelper;
import com.softeem.rzb.core.mapper.UserAccountMapper;
import com.softeem.rzb.core.mapper.UserInfoMapper;
import com.softeem.rzb.core.pojo.bo.TransFlowBO;
import com.softeem.rzb.core.pojo.entity.UserAccount;
import com.softeem.rzb.core.pojo.entity.UserInfo;
import com.softeem.rzb.core.service.TransFlowService;
import com.softeem.rzb.core.service.UserAccountService;
import com.softeem.rzb.core.service.UserBindService;
import com.softeem.rzb.core.service.UserInfoService;
import com.softeem.rzb.core.util.LendNoUtils;
import com.softeem.rzb.rabbitutil.constant.MQConst;
import com.softeem.rzb.rabbitutil.service.MQService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 用户账户 服务实现类
 * </p>
 *
 * @author min
 * @since 2024-06-27
 */
@Slf4j
@Service
public class UserAccountServiceImpl extends ServiceImpl<UserAccountMapper, UserAccount> implements UserAccountService {

    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private TransFlowService transFlowService;

    @Resource
    private UserBindService userBindService;

    @Resource
    private UserAccountService userAccountService;

    @Resource
    private UserInfoService userInfoService;
    @Resource
    private MQService mqService;

    /**
     * 用户提现异步回调
     *
     * @param paramMap
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void notifyWithdraw(Map<String, Object> paramMap) {

        log.info("提现成功");
        // 判断幂等性：根据交易流水号判断, 交易流流水号唯一, 如果存在则说明已经提现成功了
        String agentBillNo = (String) paramMap.get("agentBillNo");
        boolean result = transFlowService.isSaveTransFlow(agentBillNo);
        if (result) {
            log.warn("幂等性返回");
            return;
        }

        String bindCode = (String) paramMap.get("bindCode");
        String fetchAmt = (String) paramMap.get("fetchAmt");

        //根据用户账户修改账户金额
        baseMapper.updateAccount(bindCode, new BigDecimal("-" + fetchAmt), new BigDecimal(0));

        //增加交易流水
        TransFlowBO transFlowBO = new TransFlowBO(
                agentBillNo,
                bindCode,
                new BigDecimal(fetchAmt),
                TransTypeEnum.WITHDRAW,
                "提现");
        transFlowService.saveTransFlow(transFlowBO);
    }

    /**
     * 用户提现
     *
     * @param fetchAmt
     * @param userId
     * @return
     */
    @Override
    public String commitWithdraw(BigDecimal fetchAmt, Long userId) {

        //账户可用余额充足：当前用户的余额 >= 当前用户的提现金额
        BigDecimal amount = userAccountService.getAccount(userId);//获取当前用户的账户余额
        //OT_SUFFICIENT_FUNDS_ERROR(307, "余额不足，请充值")
        Assert.isTrue(amount.doubleValue() >= fetchAmt.doubleValue(),
                ResponseEnum.NOT_SUFFICIENT_FUNDS_ERROR);

        String bindCode = userBindService.getBindCodeByUserId(userId);//根据userId获取绑定协议号

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("agentId", HfbConst.AGENT_ID);
        paramMap.put("agentBillNo", LendNoUtils.getWithdrawNo());//提现业务随机流水号
        paramMap.put("bindCode", bindCode);//绑定协议号
        paramMap.put("fetchAmt", fetchAmt);//提现金额
        paramMap.put("feeAmt", new BigDecimal(0));
        paramMap.put("notifyUrl", HfbConst.WITHDRAW_NOTIFY_URL);
        paramMap.put("returnUrl", HfbConst.WITHDRAW_RETURN_URL);
        paramMap.put("timestamp", RequestHelper.getTimestamp());
        String sign = RequestHelper.getSign(paramMap);//生成验签参数
        paramMap.put("sign", sign);

        //构建自动提交表单
        return FormHelper.buildForm(HfbConst.WITHDRAW_URL, paramMap);
    }

    /**
     * 根据userId获取账户余额
     *
     * @param userId
     * @return
     */
    @Override
    public BigDecimal getAccount(Long userId) {
        //根据userId查找用户账户
        QueryWrapper<UserAccount> userAccountQueryWrapper = new QueryWrapper<>();
        userAccountQueryWrapper.eq("user_id", userId);
        UserAccount userAccount = baseMapper.selectOne(userAccountQueryWrapper);

        BigDecimal amount = userAccount.getAmount();
        return amount;
    }

    /**
     * 用户充值异步回调
     *
     * @param paramMap
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String notify(Map<String, Object> paramMap) {

        log.info("充值成功：" + JSONObject.toJSONString(paramMap));

        //判断交易流水是否存在
        String agentBillNo = (String) paramMap.get("agentBillNo"); //商户充值订单号
        boolean isSave = transFlowService.isSaveTransFlow(agentBillNo);
        if (isSave) {
            log.warn("幂等性返回");
            return "success";
        }

        String bindCode = (String) paramMap.get("bindCode"); //充值人绑定协议号
        String chargeAmt = (String) paramMap.get("chargeAmt"); //充值金额
        //优化 更新充值金额
        baseMapper.updateAccount(bindCode, new BigDecimal(chargeAmt), new BigDecimal(0));

        //增加交易流水
        //TODO
        TransFlowBO transFlowBO = new TransFlowBO(
                agentBillNo,
                bindCode,
                new BigDecimal(chargeAmt),
                TransTypeEnum.RECHARGE,
                "充值");//RECHARGE(1,"充值")
        transFlowService.saveTransFlow(transFlowBO);

        //发送短信消息
        log.info("发消息");
        String mobile = userInfoService.getMobileByBindCode(bindCode);//根据绑定协议号获取手机号
        SmsDTO smsDTO = new SmsDTO();
        smsDTO.setMobile(mobile);
        smsDTO.setMessage("充值成功");
        mqService.sendMessage(MQConst.EXCHANGE_TOPIC_SMS, MQConst.ROUTING_SMS_ITEM, smsDTO);

        return "success";
    }

    /**
     * 充值金额
     *
     * @param chargeAmt
     * @param userId
     * @return
     */
    @Override
    public String commitCharge(BigDecimal chargeAmt, Long userId) {

        UserInfo userInfo = userInfoMapper.selectById(userId);//根据userId查询用户(投资人)信息
        String bindCode = userInfo.getBindCode();
        //判断账户绑定状态 USER_NO_BIND_ERROR(302, "用户未绑定")
        Assert.notEmpty(bindCode, ResponseEnum.USER_NO_BIND_ERROR);

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("agentId", HfbConst.AGENT_ID);// 汇付宝分配的ID
        paramMap.put("agentBillNo", LendNoUtils.getNo());//随机生成充值单号
        paramMap.put("bindCode", bindCode);//充值人绑定协议号
        paramMap.put("chargeAmt", chargeAmt);//充值金额
        paramMap.put("feeAmt", new BigDecimal("0"));//充值手续费
        paramMap.put("notifyUrl", HfbConst.RECHARGE_NOTIFY_URL);//充值异步回调[后台通知]
        paramMap.put("returnUrl", HfbConst.RECHARGE_RETURN_URL);//充值同步回调[返回的页面]
        paramMap.put("timestamp", RequestHelper.getTimestamp());//时间戳
        String sign = RequestHelper.getSign(paramMap);//签名
        paramMap.put("sign", sign);

        //构建充值自动提交表单
        String formStr = FormHelper.buildForm(HfbConst.RECHARGE_URL, paramMap);
        return formStr;
    }
}
