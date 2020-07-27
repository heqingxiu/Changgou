package com.gc.Dao;

/**
 * Author: QX_He
 * DATA: 2020/7/6-20:43
 * Description:
 **/
public class Person {

    private String Organization;
    private String Name;
    private int Count;
    private Integer Number;

    public Person(int Count, String Organization) {

        this.Organization = Organization;
        this.Count = Count;
        System.out.println("Init success ");
    }

    public String getOrganization() {
        return Organization;
    }

    public void setOrganization(String organization) {
        Organization = organization;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public int getCount() {
        return Count;
    }

    public void setCount(int count) {
        Count = count;
    }

    public Integer getNumber() {
        return Number;
    }

    public void setNumber(Integer number) {
        Number = number;
    }
}
