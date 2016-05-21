package com.dualcnhq.sherlocked.models;

import java.util.ArrayList;

public class ContactNumbers {

    private String name;
    private ArrayList<String> contactNumbers;
    private String address;

    public ContactNumbers(String name, ArrayList<String> contactNumbers) {
        this.name = name;
        this.contactNumbers = contactNumbers;
    }

    public ContactNumbers(String name, ArrayList<String> contactNumbers, String address) {
        this.name = name;
        this.contactNumbers = contactNumbers;
        this.address = address;
    }


    public String getName() {
        return name;
    }

    public ArrayList<String> getContactNumbers() {
        return contactNumbers;
    }

    public String getAddress() {
        return address;
    }

}