package yh.rabbit.public_rabbitmq_starter.listener;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.amqp.utils.SerializationUtils;
import yh.rabbit.public_rabbitmq_starter.comsumer.DefaultRabbitConsumer;
import yh.rabbit.public_rabbitmq_starter.comsumer.RabbitConsumer;
import yh.rabbit.public_rabbitmq_starter.message.RabbitData;


import java.io.IOException;
import java.util.List;


/**
 * 默认的监听器，基本够用
 * @Author dengsd
 * @Date 2021/11/5 14:30
 */
public class RabbitMessageListener implements ChannelAwareMessageListener {
    private List<RabbitConsumer> delegates;
    private RabbitConsumer defaultConsume;

    public RabbitMessageListener(List<RabbitConsumer> delegates) {
        // 分别收集自定义的消费者和创建默认消费者
        this.delegates = delegates;
        this.defaultConsume = new DefaultRabbitConsumer();
    }
    @Override
    public void onMessage(Message message, Channel channel) throws IOException {
        try {
            // 从消息对象中获取字节类型的消息体
            byte[] body = message.getBody();
            // 反序列化并转换为指定类型实例
            RabbitData rabbitData =  (RabbitData)SerializationUtils.deserialize(body);
            // ack表示是否处理完成
            boolean ack = false;
            // 对自定义消费者逐个适配并消费
            for (RabbitConsumer rabbitConsumer : delegates) {
                if (rabbitConsumer.support(rabbitData.getMessage().getClass())) {
                    handler(rabbitConsumer, rabbitData);
                    ack = true;
                }
            }
            // 如果没有自定义消费者，则不会被处理，因此使用默认消费者
            if(!ack){
                handler(defaultConsume, rabbitData);
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            // 确认接收，需要传入消息的标识DeliveryTag，指定是否批量确认
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
    }

    private void handler(RabbitConsumer rabbitConsumer, RabbitData rabbitData) {
        try {
            // 消费数据
            rabbitConsumer.onSuccess(rabbitData.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            // 失败则调用消费失败方法
            rabbitConsumer.onFail(rabbitData.getMessage());
        }
    }
}
