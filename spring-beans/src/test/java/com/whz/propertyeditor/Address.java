package com.whz.propertyeditor;

public class Address {
    private String street;

    private String doorNum;

    private String postCode;

    public String getDoorNum() {
        return doorNum;
    }

    public void setDoorNum(String doorNum) {
        this.doorNum = doorNum;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    @Override
    public String toString() {
        return "Address{" +
            "street='" + street + '\'' +
            ", doorNum='" + doorNum + '\'' +
            ", postCode='" + postCode + '\'' +
            '}';
    }
}
 