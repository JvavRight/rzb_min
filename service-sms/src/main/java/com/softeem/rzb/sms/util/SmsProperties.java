package com.softeem.rzb.sms.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter //idea2020.2.3版配置文件自动提示需要这个
@Component
//注意prefix要写到最后一个 "." 符号之前
//调用setter为成员赋值
@ConfigurationProperties(prefix = "aliyun.sms")
public class SmsProperties implements InitializingBean {

    private String appcode;
    private String host;
    private String method;
    private String path;
    private String smsSignId;
    private String templateId;

    public static String APPCODE;
    public static String HOST;
    public static String METHOD;
    public static String PATH;
    public static String SMSSIGNID;
    public static String TEMPLATEID;

    //当私有成员被赋值后，此方法自动被调用，从而初始化常量
    @Override
    public void afterPropertiesSet() throws Exception {
        APPCODE = appcode;
        HOST = host;
        METHOD = method;
        PATH = path;
        SMSSIGNID = smsSignId;
        TEMPLATEID = templateId;
    }
}