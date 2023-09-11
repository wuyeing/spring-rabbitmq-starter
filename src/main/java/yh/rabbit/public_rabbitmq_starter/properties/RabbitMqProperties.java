package yh.rabbit.public_rabbitmq_starter.properties;


import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.CollectionUtils;
import yh.rabbit.public_rabbitmq_starter.comsumer.RabbitConsumer;
import yh.rabbit.public_rabbitmq_starter.listener.RabbitMessageListener;
import yh.rabbit.public_rabbitmq_starter.util.SpringBeanUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * rabbitmq 消息队列和交换机 配置文件
 *
 * @author dengds
 */
@Slf4j
@Data
@ConfigurationProperties(prefix = "rabbitmq")
public class RabbitMqProperties {

    /**
     * 是否是监听器，如果开启则会消费被监听的队列，注意ACK抢占
     */
    private Boolean isListener = false;

    /**
     * 装载自定义配置交换机
     */
    private List<ExchangeConfig> exchanges = new ArrayList<>();

    /**
     * 装载自定义配置队列
     */
    private List<QueueConfig> queues = new ArrayList<>();

    /**
     * 装载自定义配置监听器
     */
    private List<ListenerConfig> listeners = new ArrayList<>();

    @Data
    public static class QueueConfig {
        /**
         * 队列名（每个队列的名称应该唯一）
         * 必须*
         */
        private String name;

        /**
         * 指定绑定交互机，可绑定多个（逗号分隔），中间不要加空格
         * 必须*
         */
        private String exchangeName;

        /**
         * 队列路由键（队列绑定交换机的匹配键，例如：“user” 将会匹配到指定路由器下路由键为：【*.user、#.user】的队列）
         */
        private String routingKey;

        /**
         * 是否为持久队列（该队列将在服务器重启后保留下来）
         */
        private Boolean durable = Boolean.TRUE;

        /**
         * 是否为排它队列
         */
        private Boolean exclusive = Boolean.FALSE;

        /**
         * 如果队列为空是否删除（如果服务器在不使用队列时是否删除队列）
         */
        private Boolean autoDelete = Boolean.FALSE;

        /**
         * 头队列是否全部匹配
         * 默认：是
         */
        private Boolean whereAll = Boolean.TRUE;

        /**
         * 参数
         */
        private Map<String, Object> args;

        /**
         * 消息头
         */
        private Map<String, Object> headers;

    }

    @Data
    public static class ExchangeConfig {

        /**
         * 交换机名
         */
        private String name;

        /**
         * 交换机类型
         */
        private ExchangeType type;

        /**
         * 自定义交换机类型
         */
        private String customType;

        /**
         * 是否为持久交换机（将在服务器重启后保留下来）
         */
        private Boolean durable = Boolean.TRUE;

        /**
         * 如果它不再被使用则删除
         */
        private Boolean autoDelete = Boolean.FALSE;

        /**
         * 交换机参数（自定义交换机）
         */
        private Map<String, Object> arguments;

    }

    public enum ExchangeType {
        /**
         * 自定义交换机
         */
        CUSTOM,
        /**
         * 直连交换机（全文匹配）
         */
        DIRECT,
        /**
         * 通配符交换机（两种通配符：*只能匹配一个单词，#可以匹配零个或多个）
         */
        TOPIC,
        /**
         * 头交换机（自定义键值对匹配，根据发送消息内容中的headers属性进行匹配）
         */
        HEADERS,
        /**
         * 扇形（广播）交换机 （将消息转发到所有与该交互机绑定的队列上）
         */
        FANOUT;
    }

    @Data
    public static class ListenerConfig {
        /**
         * 队列名
         */
        private String name;

        /**
         * 指定绑定队列，可绑定多个（逗号分隔），中间不要加空格
         * 必须*
         */
        private String queueName;

        /**
         * 最大并发消费者量
         */
        private Integer maxConcurrentConsumers;

        /**
         * 最小消费者数量
         */
        private Integer concurrentConsumers;
    }

    //把list根据元素名称和元素本身转换为map，然后根据名称获取对应元素
    public ExchangeConfig getExchangeConfig(String name) {
        Map<String, ExchangeConfig> collect = exchanges.stream().collect(Collectors.toMap(ExchangeConfig::getName, e -> e));
        return collect.get(name);
    }

    /**
     * 动态创建交换机
     * 当使用nacos配置时，修改配置参数会重新构造Properties类，从而触发@PostConstruct方法，
     * 因此关于交换机的新注册和绑定写在了Properties类中
     * @return
     */
    @PostConstruct
    public void init(){
        // rabbitmq上移除符合条件的交换机，清理掉之前注册过的交换机
        RabbitMQSingletonManager.deleteAllExchangesNotDurable();
        RabbitMQSingletonManager.clearRabbitSingletonExchanges();
        createExchange();

        // rabbitmq上移除符合条件的队列和全部绑定关系，清理掉之前注册过的队列和绑定对象
        RabbitMQSingletonManager.deleteAllQueuesNotDurable();
        RabbitMQSingletonManager.removeAllBindings();
        RabbitMQSingletonManager.clearRabbitSingletonQueues();
        RabbitMQSingletonManager.clearRabbitSingletonBindings();
        bindingQueueToExchange();

        // 清理全部的监听器及其容器
        RabbitMQSingletonManager.clearRabbitSingletonListeners();
        RabbitMQSingletonManager.clearRabbitSingletonListenerContainers();
        if (getIsListener())
            createListeners();

        // 向rabbitmq中间件声明新生成的队列、交换机、绑定关系
        RabbitMQSingletonManager.rabbitAdminInitialize();
    }

    /**
     * 创建交换机
     */
    public void createExchange() {
        List<ExchangeConfig> exchanges = getExchanges();
        if (!CollectionUtils.isEmpty(exchanges)) {
            // 遍历配置中的每个交换机
            exchanges.forEach(e -> {
                // 声明交换机，查看获取到的交换机类型，如果是指定的几种类型则构造对应实例
                Exchange exchange = null;
                switch (e.getType()) {
                    case DIRECT -> exchange = new DirectExchange(e.getName(), e.getDurable(), e.getAutoDelete(), e.getArguments());
                    case TOPIC -> exchange = new TopicExchange(e.getName(), e.getDurable(), e.getAutoDelete(), e.getArguments());
                    case HEADERS -> exchange = new HeadersExchange(e.getName(), e.getDurable(), e.getAutoDelete(), e.getArguments());
                    case FANOUT -> exchange = new FanoutExchange(e.getName(), e.getDurable(), e.getAutoDelete(), e.getArguments());
                    case CUSTOM -> exchange = new CustomExchange(e.getName(), e.getCustomType(), e.getDurable(), e.getAutoDelete(), e.getArguments());
                    default -> {
                    }
                }
                // 将交换机实例注册到spring bean工厂 让spring实现交换机的管理
                if (exchange != null) {
                    RabbitMQSingletonManager.registerRabbitSingletonExchanges(e.getName(), exchange);
                }
            });
        }

    }

    /**
     * 动态绑定队列和交换机
     *
     * @return
     */
    public void bindingQueueToExchange() {
        // 前面与创建交换机类似，都是针对逐个队列配置注册bean
        List<QueueConfig> queues = getQueues();
        if (!CollectionUtils.isEmpty(queues)) {
            queues.forEach(q -> {
                // 创建队列
                Queue queue = new Queue(q.getName(), q.getDurable(),
                        q.getExclusive(), q.getAutoDelete(), q.getArgs());
                // 注册队列单例
                RabbitMQSingletonManager.registerRabbitSingletonQueues(q.getName(), queue);

                // 获取队列绑定交换机名
                List<String> exchangeNameList;
                // 使用逗号分割多个指定的交换机名称
                if (q.getExchangeName().contains(",")) {
                    String[] split = q.getExchangeName().split(",");
                    exchangeNameList = Arrays.asList(split);
                } else {
                    exchangeNameList = List.of(q.getExchangeName());
                }
                exchangeNameList.forEach(name -> {
                    // 根据交换机名称获取交换机配置参数
                    ExchangeConfig exchangeConfig = getExchangeConfig(name);
                    // 将队列绑定到交换机
                    Binding binding = bindingBuilder(queue, q, exchangeConfig);
                    // 将绑定关系注册到spring bean工厂 让spring实现绑定关系的管理
                    if (binding != null) {
                        log.debug("queue [{}] binding exchange [{}] success!", q.getName(), exchangeConfig.getName());
                        RabbitMQSingletonManager.registerRabbitSingletonBindings(q.getName() + "-" + name, binding);
                    }
                });
            });
        }
    }

    /**
     * 绑定队列、路由、交换机
     * @return 绑定对象
     */
    public Binding bindingBuilder(Queue queue, QueueConfig q, ExchangeConfig exchangeConfig) {
        // 声明绑定关系
        Binding binding = null;

        // 根据不同的交换机模式 获取不同的交换机对象（注意：刚才注册时使用的是父类Exchange，这里获取的时候将类型获取成相应的子类）生成不同的绑定规则
        switch (exchangeConfig.getType()) {
            case TOPIC:
                // 使用AMQP官方的BindingBuilder生成绑定关系实例
                binding = BindingBuilder.bind(queue)
                        // 为了更加精确获取，使用指定子类获取而不是父类
                        .to(SpringBeanUtils.getBean(exchangeConfig.getName(), TopicExchange.class))
                        .with(q.getRoutingKey());
                break;
            case DIRECT:
                binding = BindingBuilder.bind(queue)
                        .to(SpringBeanUtils.getBean(exchangeConfig.getName(), DirectExchange.class))
                        .with(q.getRoutingKey());
                break;
            case HEADERS:
                if (q.getWhereAll()) {
                    binding = BindingBuilder.bind(queue)
                            .to(SpringBeanUtils.getBean(exchangeConfig.getName(), HeadersExchange.class))
                            .whereAll(q.getHeaders()).match();
                } else {
                    binding = BindingBuilder.bind(queue)
                            .to(SpringBeanUtils.getBean(exchangeConfig.getName(), HeadersExchange.class))
                            .whereAny(q.getHeaders()).match();
                }
                break;
            case FANOUT:
                binding = BindingBuilder.bind(queue)
                        .to(SpringBeanUtils.getBean(exchangeConfig.getName(), FanoutExchange.class));
                break;
            case CUSTOM:
                binding = BindingBuilder.bind(queue)
                        .to(SpringBeanUtils.getBean(exchangeConfig.getName(), CustomExchange.class))
                        .with(q.getRoutingKey()).noargs();
                break;
            default:
                log.warn("queue [{}] config unspecified exchange!", q.getName());
                break;
        }

        return binding;
    }

    /**
     * 创建监听器，如果没有设置则会监听所有队列
     */
    public void createListeners() {
        CachingConnectionFactory connectionFactory = SpringBeanUtils.getBean(CachingConnectionFactory.class);
        List<RabbitConsumer> rabbitConsumers = SpringBeanUtils.getBeansOfType(RabbitConsumer.class).values().stream().toList();

        List<ListenerConfig> listeners = getListeners();
        // 若没有配置监听器参数，按照下述配置，监听全部队列
        if (CollectionUtils.isEmpty(listeners)) {
            RabbitMessageListener messageListener = new RabbitMessageListener(rabbitConsumers);
            //注册监听器bean
            RabbitMQSingletonManager.registerRabbitSingletonListeners("rabbitMessageListener", messageListener);
            // 创建消息队列容器
            SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
            // 暴露频道用于手动ack
            container.setExposeListenerChannel(true);
            // 设置监听的队列，这里监听所有队列
            Queue[] queues = SpringBeanUtils.getBeansOfType(Queue.class).values().toArray(new Queue[]{});
            container.addQueues(queues);
            // 设置最大并发消费者量为10
            container.setMaxConcurrentConsumers(10);
            // 设置最小消费者数量
            container.setConcurrentConsumers(1);
            // 设置手动确认消息接收
            container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
            // 设置监听器这里使用手动定义的监听器
            container.setMessageListener(messageListener);
            //注册监听器容器bean
            RabbitMQSingletonManager.registerRabbitSingletonListenerContainers("simpleMessageListenerContainer", container);
        }
        else {
            listeners.forEach(listener -> {
                RabbitMessageListener messageListener = new RabbitMessageListener(rabbitConsumers);
                RabbitMQSingletonManager.registerRabbitSingletonListeners(listener.getName(), messageListener);
                SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
                container.setExposeListenerChannel(true);
                container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
                container.setMessageListener(messageListener);
                container.setMaxConcurrentConsumers(listener.getMaxConcurrentConsumers());
                container.setConcurrentConsumers(listener.getConcurrentConsumers());

                String[] queueNames = listener.getQueueName().split(",");
                List<Queue> queues = new ArrayList<>();
                for (String queueName : queueNames) {
                    queues.add(SpringBeanUtils.getBean(queueName, Queue.class));
                }
                container.addQueues(queues.toArray(new Queue[]{}));

                RabbitMQSingletonManager.registerRabbitSingletonListenerContainers(listener.getName() + "Container", container);
            });
        }
    }
}