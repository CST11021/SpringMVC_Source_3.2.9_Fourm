package org.springframework.beans.annotation;

import lombok.Getter;
import lombok.Setter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.junit.Test;
import org.springframework.beans.annotation.AnnotationBeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.annotation.Annotation;

public class AnnotationBeanUtilsTest {

    @Test
    public void copyPropertiesToBeanTest() {

        //构造一个注解实例，等价于@RequestMapping(value = {"value1","value2"},params = {"param1","param1"})
        Annotation annotation = new RequestMapping() {
            public Class<? extends Annotation> annotationType() {
                return RequestMapping.class;
            }
            public String[] value() {
                return new String[]{"value1","value2"};
            }
            public String[] params() {
                return new String[]{"param1","param1"};
            }
            public RequestMethod[] method() {
                return new RequestMethod[0];
            }
            public String[] headers() {
                return new String[0];
            }
            public String[] consumes() {
                return new String[0];
            }
            public String[] produces() {
                return new String[0];
            }
        };

        Bean testBean = new Bean();
        //将注解类的value属性和param属性复制到 Bean
        AnnotationBeanUtils.copyPropertiesToBean(annotation, testBean, "method", "headers", "consumes", "produces");
        System.out.print("");

    }


    class Bean {

        private String[] value;
        private String[] params;

        // getter and setter ...
        public String[] getValue() {
            return value;
        }
        public void setValue(String[] value) {
            this.value = value;
        }
        public String[] getParams() {
            return params;
        }
        public void setParams(String[] params) {
            this.params = params;
        }

    }



}