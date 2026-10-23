package com.example.geoconnect;

public class BusinessModel {
    private String businessName;
    private String category;
    private String address;
    private String contactNumber;
    private String businessEmail;
    private String about;

    public BusinessModel(String businessName, String category, String address,
                         String contactNumber, String businessEmail, String about) {
        this.businessName = businessName;
        this.category = category;
        this.address = address;
        this.contactNumber = contactNumber;
        this.businessEmail = businessEmail;
        this.about = about;
    }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getBusinessEmail() { return businessEmail; }
    public void setBusinessEmail(String businessEmail) { this.businessEmail = businessEmail; }

    public String getAbout() { return about; }
    public void setAbout(String about) { this.about = about; }
}