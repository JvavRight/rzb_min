package com.softeem.rzb.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.softeem.rzb.core.pojo.entity.UserAccount;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
 * <p>
 * 用户账户 Mapper 接口
 * </p>
 *
 * @author min
 * @since 2024-06-27
 */
public interface UserAccountMapper extends BaseMapper<UserAccount> {

    void updateAccount(
            @Param("bindCode") String bindCode,
            @Param("amount") BigDecimal amount,
            @Param("freezeAmount") BigDecimal freezeAmount);
}
