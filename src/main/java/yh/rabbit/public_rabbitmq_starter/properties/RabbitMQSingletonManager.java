package yh.rabbit.public_rabbitmq_starter.properties;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import yh.rabbit.public_rabbitmq_starter.listener.RabbitMessageListener;
import yh.rabbit.public_rabbitmq_starter.template.CustomRabbitTemplate;
import yh.rabbit.public_rabbitmq_starter.util.SpringBeanUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用于记录和管理基于RabbitMqProperties参数注册的单例，提供单例清除和注册功能
 */
public final class RabbitMQSingletonManager {
    // 下面是本地记录，用于记录基于RabbitMqProperties参数生成的RabbitMQ的相关单例bean
    private static Map<String, Exchange> exchanges = new ConcurrentHashMap<>();
    private static Map<String, Queue> queues = new ConcurrentHashMap<>();
    private static Map<String, Binding> bindings = new ConcurrentHashMap<>();
    private static Map<String, RabbitMessageListener> listeners = new ConcurrentHashMap<>();
    private static Map<String, SimpleMessageListenerContainer> listenerContainers = new ConcurrentHashMap<>();

    /**
     * 将基于RabbitMqProperties参数注册的单例全部清除，同步清理本地记录
     * @param map
     * @param <T>
     */
    private static  <T> void clearRabbitSingleton(Map<String, T> map) {
        for (String name : map.keySet()) {
            SpringBeanUtils.destroySingleton(name);
        }
        map.clear();
    }
    public static void clearRabbitSingletonExchanges() {
        clearRabbitSingleton(exchanges);
    }
    public static void clearRabbitSingletonQueues() {
        clearRabbitSingleton(queues);
    }
    public static void clearRabbitSingletonBindings() {
        clearRabbitSingleton(bindings);
    }
    public static void clearRabbitSingletonListeners() {
        clearRabbitSingleton(listeners);
    }
    public static void clearRabbitSingletonListenerContainers() {
        clearRabbitSingleton(listenerContainers);
    }

    /**
     * 注册单例，并同步添加到本地记录
     * @param name
     * @param entity
     * @param map
     * @param <T>
     */
    private static <T> void registerRabbitSingleton(String name, T entity, Map<String, T> map) {
        SpringBeanUtils.registerSingletonWithOverwrite(name, entity);
        map.put(name, entity);
    }
    public static void registerRabbitSingletonExchanges(String name, Exchange exchange) {
        registerRabbitSingleton(name, exchange, exchanges);
    }
    public static void registerRabbitSingletonQueues(String name, Queue queue) {
        registerRabbitSingleton(name, queue, queues);
    }
    public static void registerRabbitSingletonBindings(String name, Binding binding) {
        registerRabbitSingleton(name, binding, bindings);
    }
    public static void registerRabbitSingletonListeners(String name, RabbitMessageListener listener) {
        registerRabbitSingleton(name, listener, listeners);
    }
    public static void registerRabbitSingletonListenerContainers(String name, SimpleMessageListenerContainer listenerContainer) {
        registerRabbitSingleton(name, listenerContainer, listenerContainers);
    }


    // 注意要通过获取CustomRabbitTemplate来得到RabbitAdmin
    private static final RabbitAdmin rabbitAdmin = new RabbitAdmin(SpringBeanUtils.getBean(CustomRabbitTemplate.class));

    /**
     * 将新生成的交换机、队列、绑定关系在rabbitmq中间件中进行声明
     */
    public static void rabbitAdminInitialize() {
        for (Exchange exchange : exchanges.values()) {
            rabbitAdmin.declareExchange(exchange);
        }
        for (Queue queue : queues.values()) {
            rabbitAdmin.declareQueue(queue);
        }
        for (Binding binding : bindings.values()) {
            rabbitAdmin.declareBinding(binding);
        }
    }

    /**
     * 在rabbitmq中间件中移除定义为非持久的交换机，交换机名单来自本地本地记录
     */
    public static void deleteAllExchangesNotDurable() {
        exchanges.values().stream()
                .filter(exchange -> !exchange.isDurable())
                .forEach(exchange -> rabbitAdmin.deleteExchange(exchange.getName()));
    }

    /**
     * 在rabbitmq中间件中移除定义为非持久的队列，队列名单来自本地本地记录
     */
    public static void deleteAllQueuesNotDurable() {
        queues.values().stream()
                .filter(queue -> !queue.isDurable())
                .forEach(queue -> rabbitAdmin.deleteQueue(queue.getName()));
    }

    /**
     * 在rabbitmq中间件中移除所有的绑定关系，名单来自本地本地记录
     */
    public static void removeAllBindings() {
        for (Binding binding : bindings.values()) {
            rabbitAdmin.removeBinding(binding);
        }
    }
}