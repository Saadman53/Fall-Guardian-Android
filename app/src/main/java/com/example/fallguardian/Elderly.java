package com.example.fallguardian;

class Elderly {
    String firstName;
    String lastName;
    String email;
    String monitor_phone_number;
    String dob;

    Boolean firstLogin;


    public Elderly(){

    }

    public Elderly(String firstName, String lastName, String email, String monitor_phone_number, String dob, Boolean firstLogin) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.monitor_phone_number = monitor_phone_number;
        this.dob = dob;
        this.firstLogin = firstLogin;
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

    public Boolean getFirstLogin() {
        return firstLogin;
    }

    public void setFirstLogin(Boolean firstLogin) {
        this.firstLogin = firstLogin;
    }
}
