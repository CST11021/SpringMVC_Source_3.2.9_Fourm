package com.whz.javabase.spi;

public interface Spi {
       boolean isSupport(String name);
       String sayHello();
}