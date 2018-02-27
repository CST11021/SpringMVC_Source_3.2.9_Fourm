package com.whz.designPattern.strategyPattern;

// 定义不同执行策略
public class OperationAdd implements Strategy {
    @Override
    public int doOperation(int num1, int num2) {
        return num1 + num2;
    }
}