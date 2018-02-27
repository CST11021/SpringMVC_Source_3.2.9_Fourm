package com.whz.designPattern.templatePattern;

public abstract class Game {
    abstract void initialize();

    abstract void startPlay();

    abstract void endPlay();

    //模板
    public final void play() {
        initialize();    //初始化游戏
        startPlay();    //开始游戏
        endPlay();    //结束游戏
    }
}