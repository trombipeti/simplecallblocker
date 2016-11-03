package com.trombipeti.simplecallblocker.model;

public class Contact implements Comparable {

    private String name;
    private String phoneNumber;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Contact(String theName, String theNumber) {
        name = theName;
        phoneNumber = theNumber;
    }

    @Override
    public int compareTo(Object another) {
        Contact other = (Contact)another;
        return name.compareTo(other.getName()) + phoneNumber.compareTo(other.getPhoneNumber());
    }
}
