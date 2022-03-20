package com.cetuer.parkingutil.domain;

import java.util.Objects;

/**
 * Created by Cetuer on 2022/3/12 20:59.
 * 信标指纹收集
 */
public class BeaconCollect {
    /**
     * 最大扫描多少次，就是多少次的平均值，越大越精确
     */
    public static int MAX = 100;

    public OnComplete onComplete;

    /**
     * 此信标的唯一标识
     */
    private String mac;
    /**
     * 收集了多少次
     */
    private int count;
    /**
     * 收集的和
     */
    private long sum;

    /**
     * 平均值
     */
    public int avg;

    public BeaconCollect(String mac) {
        this.mac = mac;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    /**
     * 添加一次信息
     *
     * @param rssi 本次收集到的rssi
     */
    public void add(int rssi) {
        if (this.count >= MAX) return;
        this.count++;
        this.sum += rssi;
        if(this.count == MAX) {
            this.onComplete.complete();
        }
    }

    /**
     * 获得平均值
     *
     * @return 平均值
     */
    public double getAvg() {
        return this.sum * 1.0 / MAX;
    }

    public long getSum() {
        return sum;
    }

    public void setSum(long sum) {
        this.sum = sum;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public void setOnComplete(OnComplete onComplete) {
        this.onComplete = onComplete;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BeaconCollect that = (BeaconCollect) o;
        return Objects.equals(mac, that.mac);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mac);
    }

    @Override
    public String toString() {
        return "BeaconCollect{" +
                "mac='" + mac + '\'' +
                ", count=" + count +
                ", sum=" + sum +
                '}';
    }

    /**
     * 收集完成回调
     */
    public interface OnComplete {
        void complete();
    }
}
