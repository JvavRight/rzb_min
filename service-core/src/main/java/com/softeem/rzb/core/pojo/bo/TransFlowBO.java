package com.softeem.rzb.core.pojo.bo;

import com.softeem.rzb.core.enums.TransTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransFlowBO {

    private String agentBillNo;  // 平台流水号
    private String bindCode;     // 绑定协议号
    private BigDecimal amount;  // 金额
    private TransTypeEnum transTypeEnum; // 交易类型
    private String memo; // 备注


}