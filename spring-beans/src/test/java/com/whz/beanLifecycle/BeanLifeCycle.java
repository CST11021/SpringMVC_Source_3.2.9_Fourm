package com.whz.beanLifecycle;

import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class BeanLifeCycle {
    @Test
    public void LifeCycleInBeanFactory(){
        Resource res = new ClassPathResource("com/whz/beanLifecycle/bean.xml");
        BeanFactory bf = new XmlBeanFactory(res);

        // 注册 BeanDefinition 之后，在实例化 BeanDefinition之前调用
        BeanFactoryPostProcessor beanFactoryPostProcessor = new MyBeanFactoryPostProcessor();
        beanFactoryPostProcessor.postProcessBeanFactory((ConfigurableListableBeanFactory) bf);

        BeanDefinitionRegistryPostProcessor beanDefinitionRegistryPostProcessor =
            new MyBeanDefinitionRegistryPostProcessor();
        beanDefinitionRegistryPostProcessor.postProcessBeanFactory((ConfigurableListableBeanFactory) bf);

        InstantiationAwareBeanPostProcessorAdapter instantiationAwareBeanPostProcessor = new MyInstantiationAwareBeanPostProcessor();
        ((ConfigurableBeanFactory)bf).addBeanPostProcessor(instantiationAwareBeanPostProcessor);

        BeanPostProcessor beanPostProcessor = new MyBeanPostProcessor();
        ((ConfigurableBeanFactory)bf).addBeanPostProcessor(beanPostProcessor);

        MergedBeanDefinitionPostProcessor mergedBeanDefinitionPostProcessor = new MyMergedBeanDefinitionPostProcessor();
        ((ConfigurableBeanFactory)bf).addBeanPostProcessor(mergedBeanDefinitionPostProcessor);

        Car car1 = (Car)bf.getBean("car");
        car1.introduce();


//        Car car2 = bf.getBean("car", Car.class);
//        car2.introduce();
//        car2.setColor("红色");
        ((XmlBeanFactory)bf).destroySingletons();
    }

    @Test
    public void LifeCycleInApplicationContext() {
        // 注意：@PostConstruct 和 @PreDestroy 注解，在 ApplicationContext 级别时才能生效，并这两方法在配置的
        // init-method="myInit" destroy-method="myDestory" 的方法前先执行。
        //ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext("com/whz/beanLifecycle/bean.xml");
        //Car car = (Car) ctx.getBean("car");
        //System.out.println(car);
        // close()方法在 ConfigurableApplicationContext 接口中定义
        //ctx.close();
    }


}



// 配置：<bean id="car" class="com.whz.beanLifecycle.Car" init-method="myInit" destroy-method="myDestory" p:brand="红旗CA72" p:maxSpeed="200"/>


// 1、BeanFactoryPostProcessor.postProcessBeanFactory():该接口是在spring容器解析完配置文件（注册了BeanDefinition）之后，在bean实例化之前被调用的

// 2、InstantiationAwareBeanPostProcessor.postProcessBeforeInstantiation：在调用bean构造函数实例化前被调用
//    调用Car()构造函数
// 2、InstantiationAwareBeanPostProcessor.postProcessAfterInstantiation：在调用bean构造函数实例后前被调用

// 3、InstantiationAwareBeanPostProcessor.postProcessPropertyValues：给bean的属性赋值之前调用
//    调用setBrand()设置属性。
//    BeanNameAware.setBeanName()
//    BeanFactoryAware.setBeanFactory()
//
// 4、BeanPostProcessor.postProcessBeforeInitialization：Bean执行初始化方法前被调用
//    5、InitializingBean.afterPropertiesSet：在设置了所有的bean属性之后，由BeanFactory调用
//    6、调用<bean>配置中init-method
// 4、BeanPostProcessor.postProcessAfterInitialization：Bean执行初始化方法后被调用
//
// 7、DisposableBean.destroy：在bean被销毁的时候调用
//
