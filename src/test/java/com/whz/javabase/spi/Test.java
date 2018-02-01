package com.whz.javabase.spi;

/**
 * @author wb-whz291815
 * @version $Id: Test.java, v 0.1 2018-02-01 10:53 wb-whz291815 Exp $$
 */
public class Test {

    @org.junit.Test
    public void test() {
        Spi spiA = SpiFactory.getSpi("SPIA");
        System.out.println(spiA.sayHello());

        Spi spiB = SpiFactory.getSpi("SPIB");
        System.out.println(spiB.sayHello());

    }

}
