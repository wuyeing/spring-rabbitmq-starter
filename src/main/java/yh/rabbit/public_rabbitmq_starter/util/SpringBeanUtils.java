package yh.rabbit.public_rabbitmq_starter.util;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Map;

/**
 * 一个spring容器的工具类，这里主要用来注册和获取bean
 */
public final class SpringBeanUtils implements BeanFactoryPostProcessor, ApplicationContextAware {
    /**
     * Spring应用上下文环境
     */
    private static ConfigurableListableBeanFactory beanFactory;
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringBeanUtils.applicationContext = applicationContext;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        SpringBeanUtils.beanFactory = beanFactory;
    }

    /**
     * 获取对象
     *
     * @param name
     * @return Object 一个以所给名字注册的bean的实例
     * @throws BeansException
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) throws BeansException {
        return (T) beanFactory.getBean(name);
    }

    /**
     * 获取类型为requiredType的对象
     *
     * @param clz
     * @return
     * @throws BeansException
     */
    public static <T> T getBean(Class<T> clz) throws BeansException {
        T result = (T) beanFactory.getBean(clz);
        return result;
    }

    public static <T> T getBean(String name, Class<T> clz) throws BeansException {
        T result = (T) beanFactory.getBean(name, clz);
        return result;
    }

    /**
     * 获取指定类型的全部实例
     * @param clz
     * @return Map形式返回，包含名字和实例
     */
    public static <T> Map<String, T> getBeansOfType(Class<T> clz) {
        return beanFactory.getBeansOfType(clz);
    }

    /**
     * 如果BeanFactory包含一个与所给名称匹配的bean定义，则返回true
     *
     * @param name
     * @return boolean
     */
    public static boolean containsBean(String name) {
        return beanFactory.containsBean(name);
    }

    /**
     * 判断以给定名字注册的bean定义是一个singleton还是一个prototype。 如果与给定名字相应的bean定义没有被找到，将会抛出一个异常（NoSuchBeanDefinitionException）
     *
     * @param name
     * @return boolean
     * @throws NoSuchBeanDefinitionException
     */
    public static boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
        return beanFactory.isSingleton(name);
    }

    /**
     * @param name
     * @return Class 注册对象的类型
     * @throws NoSuchBeanDefinitionException
     */
    public static Class<?> getType(String name) throws NoSuchBeanDefinitionException {
        return beanFactory.getType(name);
    }

    /**
     * 如果给定的bean名字在bean定义中有别名，则返回这些别名
     *
     * @param name
     * @return
     * @throws NoSuchBeanDefinitionException
     */
    public static String[] getAliases(String name) throws NoSuchBeanDefinitionException {
        return beanFactory.getAliases(name);
    }

    /**
     * 将bean对象注册单例到bean工厂
     *
     * @param beanName
     * @param bean
     * @param <T>
     * @return
     */
    public static <T> boolean registerSingleton(String beanName, T bean) {
        // 将bean对象注册到bean工厂
        ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) SpringBeanUtils.applicationContext;
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) configurableApplicationContext.getBeanFactory();
        defaultListableBeanFactory.registerSingleton(beanName, bean);
        return true;
    }

    /**
     * 覆盖注册单例
     * @param beanName
     * @param bean
     * @param <T>
     * @return
     */
    public static <T> boolean registerSingletonWithOverwrite(String beanName, T bean) {
        ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) SpringBeanUtils.applicationContext;
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) configurableApplicationContext.getBeanFactory();
        if (containsBean(beanName))
            defaultListableBeanFactory.destroySingleton(beanName);
        defaultListableBeanFactory.registerSingleton(beanName, bean);
        return true;
    }

    /**
     * 指定bean名称销毁单例
     * @param beanName
     * @return
     */
    public static boolean destroySingleton(String beanName) {
        ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) SpringBeanUtils.applicationContext;
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) configurableApplicationContext.getBeanFactory();
        if (containsBean(beanName))
            defaultListableBeanFactory.destroySingleton(beanName);
        return true;
    }
}
