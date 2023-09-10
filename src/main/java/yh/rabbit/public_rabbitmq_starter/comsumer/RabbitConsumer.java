package yh.rabbit.public_rabbitmq_starter.comsumer;

/**
 * 消费者接口
 * @Author dengsd
 * @Date 2021/11/6 14:38
 */
public interface RabbitConsumer<T> {
   // 消费数据
   void onSuccess(T t);
   // 消费失败方法调用的方法
   void onFail(T t);
   // 根据信息的类型判断是否符合本消费者，因此建议自定义专用消息类型，而不用通用类型
   boolean support(Class t);
}
