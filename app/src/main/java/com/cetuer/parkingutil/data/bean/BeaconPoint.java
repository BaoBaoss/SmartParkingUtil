package com.cetuer.parkingutil.data.bean;

/**
 * Created by Cetuer on 2022/3/12 14:45.
 * 信标坐标
 */
public class BeaconPoint {
    /**
     * x轴坐标
     */
    private Integer x;
    /**
     * y轴坐标
     */
    private Integer y;

    public BeaconPoint() {
    }

    public BeaconPoint(Integer x, Integer y) {
        this.x = x;
        this.y = y;
    }

    public Integer getX() {
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public Integer getY() {
        return y;
    }

    public void setY(Integer y) {
        this.y = y;
    }
}
