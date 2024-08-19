package com.softeem.rzb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.softeem.rzb.common.exception.Assert;
import com.softeem.rzb.common.result.ResponseEnum;
import com.softeem.rzb.core.enums.BorrowInfoStatusEnum;
import com.softeem.rzb.core.enums.BorrowerStatusEnum;
import com.softeem.rzb.core.enums.UserBindEnum;
import com.softeem.rzb.core.mapper.BorrowInfoMapper;
import com.softeem.rzb.core.mapper.BorrowerMapper;
import com.softeem.rzb.core.mapper.IntegralGradeMapper;
import com.softeem.rzb.core.mapper.UserInfoMapper;
import com.softeem.rzb.core.pojo.entity.BorrowInfo;
import com.softeem.rzb.core.pojo.entity.Borrower;
import com.softeem.rzb.core.pojo.entity.IntegralGrade;
import com.softeem.rzb.core.pojo.entity.UserInfo;
import com.softeem.rzb.core.pojo.vo.BorrowInfoApprovalVO;
import com.softeem.rzb.core.pojo.vo.BorrowerDetailVO;
import com.softeem.rzb.core.service.BorrowInfoService;
import com.softeem.rzb.core.service.BorrowerService;
import com.softeem.rzb.core.service.DictService;
import com.softeem.rzb.core.service.LendService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 借款信息表 服务实现类
 * </p>
 *
 * @author min
 * @since 2024-06-27
 */
@Service
public class BorrowInfoServiceImpl extends ServiceImpl<BorrowInfoMapper, BorrowInfo> implements BorrowInfoService {

    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private IntegralGradeMapper integralGradeMapper;
    @Resource
    private DictService dictService;
    @Resource
    private BorrowerMapper borrowerMapper;
    @Resource
    private BorrowerService borrowerService;
    @Resource
    private LendService lendService;

    /**
     * 审批借款信息
     *
     * @param borrowInfoApprovalVO
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approval(BorrowInfoApprovalVO borrowInfoApprovalVO) {

        //修改借款信息状态
        Long borrowInfoId = borrowInfoApprovalVO.getId();
        BorrowInfo borrowInfo = baseMapper.selectById(borrowInfoId);
        borrowInfo.setStatus(borrowInfoApprovalVO.getStatus());
        baseMapper.updateById(borrowInfo);

        //审核通过则创建标的                               数据从存储的值是否等于   CHECK_OK(2, "审核通过")
        if (borrowInfoApprovalVO.getStatus().intValue() == BorrowInfoStatusEnum.CHECK_OK.getStatus().intValue()) {
            //TODO
            //创建标的
            lendService.createLend(borrowInfoApprovalVO, borrowInfo);
        }
    }

    /**
     * 获取借款信息详情
     *
     * @param id
     * @return
     */
    @Override
    public Map<String, Object> getBorrowInfoDetail(Long id) {

        //查询借款对象
        BorrowInfo borrowInfo = baseMapper.selectById(id);
        //组装数据
        String returnMethod = dictService.getNameByParentDictCodeAndValue("returnMethod", borrowInfo.getReturnMethod());
        String moneyUse = dictService.getNameByParentDictCodeAndValue("moneyUse", borrowInfo.getMoneyUse());
        String status = BorrowInfoStatusEnum.getMsgByStatus(borrowInfo.getStatus());
        borrowInfo.getParam().put("returnMethod", returnMethod);
        borrowInfo.getParam().put("moneyUse", moneyUse);
        borrowInfo.getParam().put("status", status);

        //根据user_id获取借款人对象
        Borrower borrower = borrowerMapper.selectOne(new QueryWrapper<Borrower>().eq("user_id", borrowInfo.getUserId()));
        //组装借款人对象
        BorrowerDetailVO borrowerDetailVO = borrowerService.getBorrowerDetailVOById(borrower.getId());

        //组装数据
        Map<String, Object> result = new HashMap<>();
        result.put("borrowInfo", borrowInfo);
        result.put("borrower", borrowerDetailVO);
        return result;
    }

    /**
     * 查询借款信息列表
     *
     * @return
     */
    @Override
    public List<BorrowInfo> selectList() {
        List<BorrowInfo> borrowInfoList = baseMapper.selectBorrowInfoList();
        borrowInfoList.forEach(borrowInfo -> {
            // 获取字典数据中借款的还款方式
            String returnMethod = dictService.getNameByParentDictCodeAndValue("returnMethod", borrowInfo.getReturnMethod());
            // 获取字典数据中借款的用途
            String moneyUse = dictService.getNameByParentDictCodeAndValue("moneyUse", borrowInfo.getMoneyUse());
            String status = BorrowInfoStatusEnum.getMsgByStatus(borrowInfo.getStatus());// 设置审核状态
            borrowInfo.getParam().put("returnMethod", returnMethod);
            borrowInfo.getParam().put("moneyUse", moneyUse);
            borrowInfo.getParam().put("status", status);
        });

        return borrowInfoList;
    }

    /**
     * 根据userId查看审核状态
     *
     * @param userId
     * @return
     */
    @Override
    public Integer getStatusByUserId(Long userId) {

        // 根据userId查询借款信息,可能查询到多条借款信息(去年借过款,留有信息), 取最新一条
        List<Object> objects = baseMapper.selectObjs(
                new QueryWrapper<BorrowInfo>().select("status").eq("user_id", userId).orderByDesc("create_time"));

        if (objects.size() == 0) {
            //借款人尚未提交信息
            return BorrowInfoStatusEnum.NO_AUTH.getStatus();
        }
        Integer status = (Integer) objects.get(0);
        return status;
    }

    /**
     * 保存借款信息
     *
     * @param borrowInfo
     * @param userId
     */
    @Override
    public void saveBorrowInfo(BorrowInfo borrowInfo, Long userId) {

        //获取userInfo的用户数据
        UserInfo userInfo = userInfoMapper.selectById(userId);

        //判断用户绑定状态
        Assert.isTrue(
                userInfo.getBindStatus().intValue() == UserBindEnum.BIND_OK.getStatus().intValue(),
                ResponseEnum.USER_NO_BIND_ERROR);// USER_NO_BIND_ERROR(302, "用户未绑定")

        //判断用户信息是否审批通过
        Assert.isTrue(
                userInfo.getBorrowAuthStatus().intValue() == BorrowerStatusEnum.AUTH_OK.getStatus().intValue(),
                ResponseEnum.USER_NO_AMOUNT_ERROR);// USER_NO_AMOUNT_ERROR(303, "用户信息未审核")

        //判断借款额度是否足够
        BigDecimal borrowAmount = this.getBorrowAmount(userId);
        Assert.isTrue(
                borrowInfo.getAmount().doubleValue() <= borrowAmount.doubleValue(),
                ResponseEnum.USER_AMOUNT_LESS_ERROR);// USER_AMOUNT_LESS_ERROR(304, "您的借款额度不足")

        //存储数据
        borrowInfo.setUserId(userId);
        //百分比转成小数
        borrowInfo.setBorrowYearRate(borrowInfo.getBorrowYearRate().divide(new BigDecimal(100)));
        borrowInfo.setStatus(BorrowInfoStatusEnum.CHECK_RUN.getStatus());// 1：审核中
        baseMapper.insert(borrowInfo);
    }

    /**
     * 获取借款额度
     *
     * @param userId
     * @return
     */
    @Override
    public BigDecimal getBorrowAmount(Long userId) {

        //获取用户积分
        UserInfo userInfo = userInfoMapper.selectById(userId);
        Assert.notNull(userInfo, ResponseEnum.LOGIN_MOBILE_ERROR);
        Integer integral = userInfo.getIntegral();

        //根据积分查询借款额度
        IntegralGrade integralGradeConfig = integralGradeMapper.selectOne(
                new QueryWrapper<IntegralGrade>()
                        .le("integral_start", integral)
                        .ge("integral_end", integral));

        if (integralGradeConfig == null) {
            return new BigDecimal("0");
        }
        return integralGradeConfig.getBorrowAmount();
    }
}

