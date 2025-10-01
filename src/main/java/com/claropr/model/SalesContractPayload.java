package com.claropr.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesContractPayload {
    private String source;              // SIF | COPS
    private String contractUid;
    private String orderId;
    private String ban;
    private String subscriberNumber;
    private String accountType;         // POSTPAID|...
    private String lob;                 // MOBILE|FIXED|...
    private String templateId;
    private String language;
    private Boolean portIn;
    private DeliveryPreferences deliveryPreferences;
    private Customer customer;
    private SaleInfo saleInfo;
    private PlanAndServices planAndServices;
    private List<Device> devices;
    private Totals totals;
    private Flags flags;
    private Legal legal;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryPreferences {
        private String email;
        private String sms;
        private List<String> via;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Customer {
        private String fullName;
        private String idType;
        private String idNumber;
        private String email;
        private String phone;
        private Address billingAddress;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Address {
            private String line1;
            private String line2;
            private String city;
            private String state;
            private String zip;
            private String country;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SaleInfo {
        private Store store;
        private Seller seller;
        private String saleDate;
        private String closeDate;
        private String notes;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Store {
            private String code;
            private String name;
            private String address;
            private String phone;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Seller {
            private String name;
            private String employeeId;
            private String email;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlanAndServices {
        private Plan plan;
        private List<Addon> addons;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Plan {
            private String name;
            private String code;
            private String billingCycle;
            private String dataPolicy;
            private BigDecimal basePrice;
            private Integer termMonths;
            private List<String> features;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Addon {
            private String code;
            private String description;
            private BigDecimal price;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Device {
        private String sku;
        private String description;
        private String serial;
        private String imei;
        private String iccid;
        private String esimEid;
        private BigDecimal price;
        private BigDecimal tax;
        private BigDecimal discount;
        private Financing financing;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Financing {
            private Boolean enabled;
            private Integer installments;
            private BigDecimal downPayment;
            private BigDecimal monthlyPayment;
            private BigDecimal apr;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Totals {
        private BigDecimal estimatedMonthly;
        private NextBillEstimate nextBillEstimate;
        private BigDecimal taxes;
        private BigDecimal fees;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class NextBillEstimate {
            private BigDecimal promoCharge;
            private BigDecimal proration;
            private BigDecimal deviceCharge;
            private BigDecimal otherChargesCredits;
            private BigDecimal estimatedTotal;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Flags {
        private Boolean portIn;
        private Boolean insurance;
        private Boolean bundle;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Legal {
        private String termsVersion;
        private Consents consents;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Consents {
            private Boolean electronicSignature;
            private Boolean privacyNotice;
            private Boolean creditCheck;
        }
    }
}
