package com.softeem.rzb.sms.service;

import java.util.Map;

public interface SmsService {

    /**
     * 发送短信
     * @param mobile 手机号
     * @param validateCode 验证码
     */
    void send(String mobile, String validateCode);
}