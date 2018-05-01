package com.whz.spring.jmx.exportmbean;

public class JmxTestBean implements IJmxTestBean{
    private String name;  
    private int age;  
  
    public String getName() {  
        return name;  
    }  
  
    public void setName(String name) {  
        this.name = name;  
    }  
  
    public int getAge() {  
        return age;  
    }  
  
    public void setAge(int age) {  
        this.age = age;  
    }  
  
    public int add(int x, int y) {  
        return x+y;  
    }  
  
    public long operation() {  
        return 100;  
    }  
    public void dontExportOperation(){  
        throw  new RuntimeException("do not export me");  
    }  
}  