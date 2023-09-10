package yh.rabbit.public_rabbitmq_starter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import yh.rabbit.public_rabbitmq_starter.properties.RabbitMqProperties;
import yh.rabbit.public_rabbitmq_starter.provider.DefaultRabbitProvider;
import yh.rabbit.public_rabbitmq_starter.provider.RabbitProvider;
import yh.rabbit.public_rabbitmq_starter.template.CustomRabbitTemplate;
import yh.rabbit.public_rabbitmq_starter.util.SpringBeanUtils;


/**
 * @Author dengsd
 * @Date 2021/11/6 17:14
 */
@Slf4j
@AutoConfiguration
@Import({SpringBeanUtils.class, RabbitMqProperties.class})
public class RabbitAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(RabbitProvider.class)
    public RabbitProvider defaultRabbitProvider(CustomRabbitTemplate customRabbitTemplate) {
        return new DefaultRabbitProvider(customRabbitTemplate);
    }

    @Bean
    //因为默认AMQP的自动配置类会注册RabbitTemplate的bean，为了防止注册冲突，自定义一个与RabbitTemplate完全一样的类注册bean
    public CustomRabbitTemplate customRabbitTemplate(CachingConnectionFactory connectionFactory) {
        // 设置消息确认类型，使用CorrelationData关联消息确认与消息发送
        connectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
        // 确认发布信息后回调
        connectionFactory.setPublisherReturns(true);
        CustomRabbitTemplate rabbitTemplate = new CustomRabbitTemplate(connectionFactory);
        // 开启消息发送失败退回机制，配合setReturnsCallback使用
        rabbitTemplate.setMandatory(true);
        // 设置发送成功回调方法接口
        rabbitTemplate.setConfirmCallback(
                (correlationData, ack, cause) -> {
                    String s = String.format("消息发送成功:correlationData(%s),ack(%s),cause(%s)", correlationData, ack, cause);
                    System.out.println(s);
                    log.debug(s);
                }
        );
        // 设置有返回内容时的回调方法接口，用来处理因消息发送失败退回机制而返回的数据
        rabbitTemplate.setReturnsCallback(returned ->{
            String s = String.format("消息丢失:exchange(%s),route(%s),replyCode(%s),replyText(%s),message:%s",
                    returned.getExchange(),
                    returned.getRoutingKey(),
                    returned.getReplyCode(),
                    returned.getReplyText(),
                    returned.getMessage());
            System.out.println(s);
            log.debug(s);
        });
        return rabbitTemplate;
    }

//    @Bean
//    //绑定监听器与队列，并设置其他参数
//    public SimpleMessageListenerContainer simpleMessageListenerContainer(CachingConnectionFactory connectionFactory,
//                                                                         @Qualifier("rabbitMessageListener") MessageListener messageListener,
//                                                                         Queue... queues) {
//        // 创建消息队列容器
//        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
//        // 暴露频道用于手动ack
//        container.setExposeListenerChannel(true);
//        // 设置监听的队列
//        container.addQueues(queues);
//        // 设置最大并发消费者量为10
//        container.setMaxConcurrentConsumers(10);
//        // 设置最小消费者数量
//        container.setConcurrentConsumers(1);
//        // 设置手动确认消息接收
//        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
//        // 设置监听器这里使用手动定义的监听器
//        container.setMessageListener(messageListener);
//        return container;
//    }
//
//    @Bean("rabbitMessageListener")
//    //获取所有消费者的bean
//    public MessageListener rabbitMessageListener(List<RabbitConsumer> delegate) {
//        return new RabbitMessageListener(delegate);
//    }
}
