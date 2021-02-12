package com.example.fallguardian;

class Elderly {
    String firstName;
    String lastName;
    String email;
    String phone_number;
    String monitor_first_name;
    String monitor_last_name;
    String monitor_phone_number;
    String dob;


    public Elderly(){

    }

    public Elderly(String firstName, String lastName, String email, String phone_number, String monitor_first_name, String monitor_last_name, String monitor_phone_number, String dob) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone_number = phone_number;
        this.monitor_first_name = monitor_first_name;
        this.monitor_last_name = monitor_last_name;
        this.monitor_phone_number = monitor_phone_number;
        this.dob = dob;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getMonitor_first_name() {
        return monitor_first_name;
    }

    public void setMonitor_first_name(String monitor_first_name) {
        this.monitor_first_name = monitor_first_name;
    }

    public String getMonitor_last_name() {
        return monitor_last_name;
    }

    public void setMonitor_last_name(String monitor_last_name) {
        this.monitor_last_name = monitor_last_name;
    }

    public String getMonitor_phone_number() {
        return monitor_phone_number;
    }

    public void setMonitor_phone_number(String monitor_phone_number) {
        this.monitor_phone_number = monitor_phone_number;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

}
