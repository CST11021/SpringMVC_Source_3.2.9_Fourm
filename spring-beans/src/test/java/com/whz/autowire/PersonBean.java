package com.whz.autowire;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.Resource;

// @Autowired、@Qualifier和@Resource注解装配Bean时都可以省略getter/setter方法
public class PersonBean {

    private String name;

    /**
        1、当容器中只有一个Address.class 类型的bean时：@Autowired 默认按类型匹配的方式，将其注入进来；
        2、当容器中存在多个Address.class 类型的bean时：@Autowired 会根据Bean名称进行注入，并且需要特别注意的是，beanName
           必须是注入的属性名，而不是setter()方法对应的驼峰命名规则；另外，在这种情况下，我们可以使用 @Qualifier("address")
           注解加上指定beanName的方式来指定要注入的bean；
        3、如果容器中一个和标注变量类型匹配的bean都没有，Spring容器启动时将报NoSuchBeanDefinitionException的异常；
        4、如果希望Spring即使找不到匹配的Bean完成注入也不要抛出异常，那么可以使用@AutoWired(required=false)进行标注。
     */
    @Autowired
    // 当bean没有配置name时，则默认使用id作为name；id和name都没有配置时，Spring会为它生成一个name，如："com.whz.autowire.Address#0"
    @Qualifier("com.whz.autowire.Address#0")
    private Address addr;


    /**
        1、@Resource默认是按照名称来装配注入的，只有当找不到与名称匹配的bean才会按照类型来装配注入，如果@Resource注解不指定name
           属性，那么直接根据类型来装配，此时，@Resource的装配规则和@Autowired一样，两者没有区别
        2、当 @Resource 注解指定了name属性时，则根据beanName匹配，如果没有匹配的Bean，则NoSuchBeanDefinitionException异常
     */
    // 注意：@Resource 注解不是spring中定义的注解
    @Resource(name = "car2")
    private Car c;






    public String getPersonName() {
        return name;
    }
    public void setPersonName(String name) {
        this.name = name;
    }
//    public Car getCar() {
//        return c;
//    }
//    public void setCar(Car c) {
//        this.c = c;
//    }

    @Override
    public String toString() {
        return "Person [name=" + name + ", address=" + addr + ", car=" + c + "]";
    }
}