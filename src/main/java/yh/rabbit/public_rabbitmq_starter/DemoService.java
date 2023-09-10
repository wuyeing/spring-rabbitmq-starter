package yh.rabbit.public_rabbitmq_starter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import yh.rabbit.public_rabbitmq_starter.message.RabbitData;
import yh.rabbit.public_rabbitmq_starter.message.RabbitDataMsg;
import yh.rabbit.public_rabbitmq_starter.provider.RabbitProvider;

@RestController
public class DemoService {
    @Autowired
    RabbitProvider rabbitProvider;
    @GetMapping("/test")
    public String test() {
        rabbitProvider.send(
                "topicExchange",
                "delay.key",
                RabbitData.builder()
                        .uuid("8888888")
                        .message(RabbitDataMsg.builder().data("1234654").build())
                        .build(),
                0);
        return "成功";
    }
}
