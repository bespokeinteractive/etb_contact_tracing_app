package com.bespoke.davie.etb_contact_tracing_app;
/**
 *
 */

public class Name {
    private String given_name;
    private int status;
    private String given_midd;
    private String given_last;
    private String mobile;

    public Name(String given_name,String given_midd  , String given_last , String mobile , int status) {
        this.given_name = given_name;
        this.given_midd = given_midd;
        this.given_last = given_last;
        this.mobile = mobile;
        this.status = status;

    }

    public String getName() {
        return given_name;
    }

    public int getStatus() {
        return status;
    }

    public String getMidd() {
        return given_midd;
    }


    public String getLast() {
        return given_last;
    }
    public String getMobile() {
        return mobile;
    }
}
