package yh.rabbit.public_rabbitmq_starter.comsumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * 默认消费者
 * @Author dengsd
 * @Date 2021/11/6 14:49
 */
@Slf4j
public class DefaultRabbitConsumer implements RabbitConsumer {
    @Override
    public void onSuccess(Object o) {
        try {
            String s = String.format("默认处理消息成功=============================>%s",
                    new ObjectMapper().writeValueAsString(o));
            System.out.println(s);
            log.debug(s);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFail(Object o) {
        try {
            String s = String.format("默认处理消息失败=============================>%s",
                    new ObjectMapper().writeValueAsString(o));
            System.out.println(s);
            log.debug(s);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    // 默认消费者支持所有类，即Object
    public boolean support(Class clazz) {
        return Object.class.isAssignableFrom(clazz);
    }
}
