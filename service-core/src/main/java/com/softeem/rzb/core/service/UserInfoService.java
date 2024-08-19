package com.softeem.rzb.core.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.softeem.rzb.core.pojo.entity.UserInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.softeem.rzb.core.pojo.query.UserInfoQuery;
import com.softeem.rzb.core.pojo.vo.LoginVO;
import com.softeem.rzb.core.pojo.vo.RegisterVO;
import com.softeem.rzb.core.pojo.vo.UserIndexVO;
import com.softeem.rzb.core.pojo.vo.UserInfoVO;

/**
 * <p>
 * 用户基本信息 服务类
 * </p>
 *
 * @author min
 * @since 2024-06-27
 */
public interface UserInfoService extends IService<UserInfo> {

    void lock(Long id, Integer status);

    IPage<UserInfo> listPage(Page<UserInfo> pageParam, UserInfoQuery userInfoQuery);

    void register(RegisterVO registerVO);

    UserInfoVO login(LoginVO loginVO, String ip);

    boolean checkMobile(String mobile);

    UserIndexVO getIndexUserInfo(Long userId);

    String getMobileByBindCode(String bindCode);

}
