package com.example.fallguardian;

import android.util.Log;

import java.util.HashMap;
public class Data {
    private double acc_x;
    private double acc_y;
    private double acc_z;
    private double gyro_x;
    private double gyro_y;
    private double gyro_z;

    private Long timestamp;

//    private double max_acc;
//    private double min_acc;
//    private double max_grav;
//    private double min_grav;
//    private double max_lin;
//    private double min_lin ;
//    private double max_agv ;
//    private double min_agv ;
//    private double max_gyro ;
//    private double min_gyro ;


    public Data(double acc_x, double acc_y, double acc_z, double gyro_x, double gyro_y, double gyro_z, Long timestamp) {
        this.acc_x = acc_x;
        this.acc_y = acc_y;
        this.acc_z = acc_z;
        this.gyro_x = gyro_x;
        this.gyro_y = gyro_y;
        this.gyro_z = gyro_z;
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

    public double getGyro_x() {
        return gyro_x;
    }

    public void setGyro_x(double gyro_x) {
        this.gyro_x = gyro_x;
    }

    public double getGyro_y() {
        return gyro_y;
    }

    public void setGyro_y(double gyro_y) {
        this.gyro_y = gyro_y;
    }

    public double getGyro_z() {
        return gyro_z;
    }

    public void setGyro_z(double gyro_z) {
        this.gyro_z = gyro_z;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
