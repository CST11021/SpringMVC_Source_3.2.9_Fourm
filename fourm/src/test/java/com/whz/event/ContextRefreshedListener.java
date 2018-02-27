package com.whz.event;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

// 容器刷新事件监听，它用于监听 ContextRefreshedEvent 事件是否发生
public class ContextRefreshedListener implements ApplicationListener<ContextRefreshedEvent> {

    //Spring容器启动完毕后触发该方法
    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {

        // 为什么要加这个判断呢？？？
        // 在web 项目中（spring mvc），系统会存在两个容器，一个是root application context ,另一个就是我们自己的 projectName-servlet context（作为root application context的子容器）。
        // 这种情况下，就会造成onApplicationEvent方法被执行两次。为了避免上面提到的问题，我们可以只在root application context初始化完成后调用逻辑代码，其他的容器的初始化完成，则不做任何处理
        if(contextRefreshedEvent.getApplicationContext().getParent() == null){//root application context 没有parent，他就是老大.
            System.out.println("Spring容器启动完毕...");
        }

    }

}
