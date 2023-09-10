package yh.rabbit.public_rabbitmq_starter.provider;


import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import yh.rabbit.public_rabbitmq_starter.message.RabbitData;
import yh.rabbit.public_rabbitmq_starter.template.CustomRabbitTemplate;


/**
 * @Author dengsd
 * @Date 2021/11/6 17:17
 */

@Data
@AllArgsConstructor
public class DefaultRabbitProvider implements RabbitProvider {
    private CustomRabbitTemplate customRabbitTemplate;
    @Override
    public void send(String exchange, String routingKey, RabbitData data, int delay) {
        // 默认的消息发送接口实现类就可以符合大部分需求
        // 配置时采用了CORRELATED模式（使用CorrelationData关联消息），因此这里需要传入CorrelationData
        customRabbitTemplate.convertAndSend(exchange, routingKey, data,
                (d)->{
            d.getMessageProperties().setDelay(delay);
            return d;
            },
                new CorrelationData(data.getUuid()));
    }
}
