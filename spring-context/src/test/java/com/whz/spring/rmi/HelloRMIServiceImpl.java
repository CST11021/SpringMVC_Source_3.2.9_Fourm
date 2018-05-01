package com.whz.spring.rmi;

/**
 * @author wb-whz291815
 * @version $Id: HelloRMIServiceImpl.java, v 0.1 2018-03-21 15:28 wb-whz291815 Exp $$
 */
public class HelloRMIServiceImpl implements HelloRMIService {

    @Override
    public int getAdd(int a, int b) {
        return a + b;
    }

}
