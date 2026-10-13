package com.example.geoconnect;

import java.util.ArrayList;
import java.util.List;

public class BusinessRepository {

    private static BusinessRepository instance;
    private final List<BusinessModel> businessList;
    private BusinessModel currentBusiness; // Stores the active signed-up business for the dashboard

    private BusinessRepository() {
        businessList = new ArrayList<>();
    }

    public static synchronized BusinessRepository getInstance() {
        if (instance == null) {
            instance = new BusinessRepository();
        }
        return instance;
    }

    // Add a newly registered business
    public void addBusiness(BusinessModel business) {
        businessList.add(business);
        this.currentBusiness = business; // Set as current active session business
    }

    // Get all registered businesses
    public List<BusinessModel> getBusinessList() {
        return businessList;
    }

    // Get active business for the dashboard
    public BusinessModel getCurrentBusiness() {
        return currentBusiness;
    }

    public void setCurrentBusiness(BusinessModel business) {
        this.currentBusiness = business;
    }
}