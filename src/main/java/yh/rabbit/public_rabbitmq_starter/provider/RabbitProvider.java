package yh.rabbit.public_rabbitmq_starter.provider;


import yh.rabbit.public_rabbitmq_starter.message.RabbitData;

/**
 * @Author dengsd
 * @Date 2021/11/6 17:14
 */
public interface RabbitProvider {
   /**
    * 默认的消息发送接口
    * @param exchange 交换机
    * @param routingKey  路由key
    * @param data  消息对象
    * @param delay 延时时间消费 （单位毫秒）
    */
   void send(String exchange, String routingKey, RabbitData data, int delay);
}
