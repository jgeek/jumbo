package com.jumbo.adapter.out.persistence;

import lombok.Data;

@Data
public class StoreEntity {

    private String city;

    private String postalCode;

    private String street;

    private String street2;

    private String street3;

    private String addressName;

    private String uuid;

    private double longitude;

    private double latitude;

    private String complexNumber;

    private boolean showWarningMessage;

    private String todayOpen;

    private String todayClose;

    private String locationType;

    private boolean collectionPoint;

    private String sapStoreID;
}