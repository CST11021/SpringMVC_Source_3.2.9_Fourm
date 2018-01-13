package com.whz.aop.advice.introinterceptor;

/**
 * Created by wb-whz291815 on 2017/7/27.
 */
// 该接口用来控制是否开启监视器
public interface Monitorable {
    void setMonitorActive(boolean active);
}
