package com.whz.serviceloader;

public class HDFSService implements IService {
  
    @Override  
    public String sayHello() {  
        return "Hello HDFS!!";  
    }  
  
    @Override  
    public String getScheme() {  
        return "hdfs";  
    }  
}  