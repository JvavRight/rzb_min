package com.softeem.rzb.sms.receiver;

import com.softeem.rzb.base.dto.SmsDTO;
import com.softeem.rzb.rabbitutil.constant.MQConst;
import com.softeem.rzb.sms.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

@Component
@Slf4j
public class SmsReceiver {

    @Resource
    private SmsService smsService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MQConst.QUEUE_SMS_ITEM, durable = "true"),
            exchange = @Exchange(value = MQConst.EXCHANGE_TOPIC_SMS),
            key = {MQConst.ROUTING_SMS_ITEM}
    ))
    public void send(SmsDTO smsDTO) throws IOException {
        log.info("SmsReceiver 消息监听");
        //Map<String, Object> param = new HashMap<>();
        //param.put("code", smsDTO.getMessage());
        //smsService.send(smsDTO.getMobile(), SmsProperties.TEMPLATE_CODE, param);
        smsService.send(smsDTO.getMobile(), "6666");
    }
}