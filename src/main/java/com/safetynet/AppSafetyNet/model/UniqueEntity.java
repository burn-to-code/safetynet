package com.safetynet.AppSafetyNet.model;

public interface UniqueEntity {

    default String getId(){
        return getFirstName() + getLastName();
    }

    String getFirstName();

    String getLastName();
}
