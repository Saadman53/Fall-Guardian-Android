package com.example.fallguardian;

import android.util.Log;

import java.util.HashMap;
public class Data_ACC {
    private double acc_x;
    private double acc_y;
    private double acc_z;

    private Long timestamp;

    public Data_ACC(double acc_x, double acc_y, double acc_z, Long timestamp) {
        this.acc_x = acc_x;
        this.acc_y = acc_y;
        this.acc_z = acc_z;
        this.timestamp = timestamp;
    }

    public double getAcc_x() {
        return acc_x;
    }

    public void setAcc_x(double acc_x) {
        this.acc_x = acc_x;
    }

    public double getAcc_y() {
        return acc_y;
    }

    public void setAcc_y(double acc_y) {
        this.acc_y = acc_y;
    }

    public double getAcc_z() {
        return acc_z;
    }

    public void setAcc_z(double acc_z) {
        this.acc_z = acc_z;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
