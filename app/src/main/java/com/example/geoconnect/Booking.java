package com.example.geoconnect;


    public class Booking {

        private String id;
        private String customerName;
        private String serviceTitle;
        private String price;
        private String dateTime;
        private String location;
        private String status;
        private String initials;

        // Required for Firestore
        public Booking() {
        }

        public Booking(String id, String customerName,
                       String serviceTitle, String price,
                       String dateTime, String location,
                       String status, String initials) {

            this.id = id;
            this.customerName = customerName;
            this.serviceTitle = serviceTitle;
            this.price = price;
            this.dateTime = dateTime;
            this.location = location;
            this.status = status;
            this.initials = initials;
        }

        public String getId() {
            return id;
        }

        public String getCustomerName() {
            return customerName;
        }

        public String getServiceTitle() {
            return serviceTitle;
        }

        public String getPrice() {
            return price;
        }

        public String getDateTime() {
            return dateTime;
        }

        public String getLocation() {
            return location;
        }

        public String getStatus() {
            return status;
        }

        public String getInitials() {
            return initials;
        }
    }



