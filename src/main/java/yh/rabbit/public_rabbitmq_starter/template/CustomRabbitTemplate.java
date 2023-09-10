package yh.rabbit.public_rabbitmq_starter.template;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

//因为默认AMQP的自动配置类会注册RabbitTemplate的bean，为了防止注册冲突，自定义一个与RabbitTemplate完全一样的类使用
public class CustomRabbitTemplate extends RabbitTemplate {
    public CustomRabbitTemplate(CachingConnectionFactory connectionFactory) {
        super(connectionFactory);
    }
}
