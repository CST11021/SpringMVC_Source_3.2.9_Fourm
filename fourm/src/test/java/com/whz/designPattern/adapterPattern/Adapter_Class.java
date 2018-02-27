package com.whz.designPattern.adapterPattern;

import com.whz.designPattern.adapterPattern.Adaptee;
import com.whz.designPattern.adapterPattern.Target;

public class Adapter_Class extends Adaptee implements Target {

    // 由于源类Adaptee没有方法sampleOperation2() 因此适配器补充上这个方法
    @Override
    public void sampleOperation2() {
        //可以复用父类的的 sampleOperation1() 方法实现适配接口的方法
        sampleOperation1();
    }

}