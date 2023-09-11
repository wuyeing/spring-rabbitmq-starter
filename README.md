# spring-rabbitmq-starter

#### 介绍
* 本项目改自[spring-rabbitmq-starter](https://gitee.com/dengshidang/spring-rabbitmq-starter)。
* 为什么不用官方的starter？
    * 想通过yml配置队列、交换机、绑定关系、监听器
    * 想基于yml热更新（比如使用nacos更新），但是发现官方的starter貌似不能通过nacos修改配置热更新
* 对[spring-rabbitmq-starter](https://gitee.com/dengshidang/spring-rabbitmq-starter)主要变动如下：
    * properties下新增了RabbitMQSingletonManager类，用来管理和注册基于RabbitMqProperties参数的单例
    * 修改了RabbitMqProperties，实现了基于yml注册监听器的功能（[spring-rabbitmq-starter](https://gitee.com/dengshidang/spring-rabbitmq-starter)中是一个监听器监听所有的队列，基于不同的消费者来辨别，这里尽量实现官方指定队列监听的功能）
    * 基于第1条对RabbitMqProperties修改，使得在热更新yml时，清理掉之前注册过的队列、交换机、绑定关系、监听器实例并基于新参数重新注册
    * 同步更新RabbitMQ中间件上的队列、交换机、绑定信息


#### 安装教程

1. 实现接口
   ```java
   public interface RabbitConsume<T> {
   void  onSuccess(T t);
   void  onFail(T t);
   boolean  support(Class t);
   }
   
   ```
2. 配置

  ```yml
spring:
    main:
        # 需要开启覆盖
        allow-bean-definition-overriding: true
    rabbitmq:
        host: localhost 
        port: 5672
        username: xxx
        password: xxx
rabbitmq:
    # 是否启动监听器
    is-listener: true
     # 交换机
    exchanges:
       # 话题
        -   name: topicExchange
            type: TOPIC
            durable: false
     # 队列
    queues:
        -   name: insertUpdateQueue
            # 路由
            routing-key: insertUpdate.id
            # 指定交换机名称
            exchange-name: topicExchange
            durable: false
        -   name: deleteQueue
            routing-key: delete.id
            exchange-name: topicExchange
    # 监听器，is-listener=true生效
    listeners:
        -   name: insertUpdateQueueListener
            # 指定监听的队列名称
            queue-name: insertUpdateQueue
            # 最大同时处理量
            max-concurrent-consumers: 1
            # 最小同时处理量
            concurrent-consumers: 1
        -   name: deleteQueueListener
            queue-name: deleteQueue
            max-concurrent-consumers: 1
            concurrent-consumers: 1
  ```

#### 使用说明

1. 发送消息
   ```java
   public class DemoService {
    @Autowired
    RabbitProvider rabbitProvider;
    
    public void test() {
    rabbitProvider.send("delay.exchange","delay.key", RabbitData.builder().uuid("8888888").message(new Demo)).build(), 3000);
    ··· 代码
   }
  
   ```
2.监听   
 ```java
   public class MyRabbitConsume implements RabbitConsume<Demo> {

   @Override
   public void onSuccess(Demo o) {
      log.debug("默认处理消息成功=============================>{}" , JSONObject.toJSONString(o));
   }

   @Override
   public void onFail(Demo o) {
      log.debug("默认处理消息失败=============================>{}" , JSONObject.toJSONString(o));
   }

   @Override
   public boolean support(Class clazz) {
      return Demo.class.isAssignableFrom(clazz);
   }
 ```


#### 特技

1.  使用 Readme\_XXX.md 来支持不同的语言，例如 Readme\_en.md, Readme\_zh.md
2.  Gitee 官方博客 [blog.gitee.com](https://blog.gitee.com)
3.  你可以 [https://gitee.com/explore](https://gitee.com/explore) 这个地址来了解 Gitee 上的优秀开源项目
4.  [GVP](https://gitee.com/gvp) 全称是 Gitee 最有价值开源项目，是综合评定出的优秀开源项目
5.  Gitee 官方提供的使用手册 [https://gitee.com/help](https://gitee.com/help)
6.  Gitee 封面人物是一档用来展示 Gitee 会员风采的栏目 [https://gitee.com/gitee-stars/](https://gitee.com/gitee-stars/)
