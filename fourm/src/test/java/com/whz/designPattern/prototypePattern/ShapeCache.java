package com.whz.designPattern.prototypePattern;

import java.util.Hashtable;

public class ShapeCache {

    private static Hashtable<String, Shape> shapeMap = new Hashtable<String, Shape>();

    public static Shape getShape(String shapeId) {
        Shape cachedShape = shapeMap.get(shapeId);
        // 注意：我们不是直接返回缓存对象，而是返回这个克隆对象
        return (Shape) cachedShape.clone();
    }

    // 这里我们假设三种原型对象的创建过程都很复杂（可能需要读取外部文件，查询数据库或算法等），所以我们缓存了这三个原型对象
    public static void loadCache() {
        Shape_Circle shapeCircle = new Shape_Circle();
        shapeCircle.setId("1");
        shapeMap.put(shapeCircle.getId(), shapeCircle);

        Shape_Square shapeSquare = new Shape_Square();
        shapeSquare.setId("2");
        shapeMap.put(shapeSquare.getId(), shapeSquare);

        Shape_Rectangle shapeRectangle = new Shape_Rectangle();
        shapeRectangle.setId("3");
        shapeMap.put(shapeRectangle.getId(), shapeRectangle);
    }
}

