package com.softeem.rzb.sms;

import com.softeem.rzb.sms.util.SmsProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UtilsTests {

    @Test
    public void testProperties(){
        System.out.println(SmsProperties.APPCODE);
        System.out.println(SmsProperties.HOST);
        System.out.println(SmsProperties.METHOD);
        System.out.println(SmsProperties.PATH);
        System.out.println(SmsProperties.SMSSIGNID);
        System.out.println(SmsProperties.TEMPLATEID);
    }
}