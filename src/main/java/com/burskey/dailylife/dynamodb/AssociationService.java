package com.burskey.dailylife.dynamodb;

public interface AssociationService {


    public void associate(String parentId, String childId);

    public void dissociate(String parentId, String childId);

    public boolean isAssociated(String parentId, String childId);

    public String[] getAssociatedFrom(String parentId);

    public String getAssociationOwnerFrom(String childId);
}
