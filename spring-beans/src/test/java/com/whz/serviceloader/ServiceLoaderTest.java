package com.whz.serviceloader;

import java.util.ServiceLoader;

public class ServiceLoaderTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        ServiceLoader<IService> serviceLoader = ServiceLoader.load(IService.class);
        for (IService service : serviceLoader) {
            System.out.println(service.getScheme() + "=" + service.sayHello());
        }
    }

}  