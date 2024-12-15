package com.burskey.dailylife.dynamodb;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.burskey.dailylife.party.domain.Communication;
import com.burskey.dailylife.party.service.ServiceException;
import com.burskey.dailylife.task.domain.Task;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.*;


public class AssociationDynamoImpl implements AssociationService {

    protected final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
    protected final DynamoDB dynamoDB = new DynamoDB(client);

    final Table associationTable;


    public AssociationDynamoImpl(String anAssociationTableName) {
        this.associationTable = dynamoDB.getTable(anAssociationTableName);
    }


    public void associate(String parentId, String childId) {

        try {
            if (parentId != null || !parentId.isEmpty() && childId != null || !childId.isEmpty()) {
                if (!isAssociated(parentId, childId)) {
                    System.out.println("Inserting association from: " + parentId + " to: " + childId);
                    // insert parent, child
                    {

                        final Item item = new Item()
                                .withPrimaryKey("from_id", parentId, "to_id", childId);
                        this.associationTable.putItem(item);
                    }

                    System.out.println("Proceeding to insert reverse association");
                    {
                        final Table table = this.associationTable;
                        String key = this.buildChildHashToParentKey(childId);
                        System.out.println("Inserting reverse association from: " + key + " to: " + parentId);
                        // insert child#+childId
                        final Item item = new Item()
                                .withPrimaryKey("from_id", key, "to_id", parentId);
                        table.putItem(item);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error inserting association information: " + e.getMessage());
        }

    }

    public void dissociate(String parentId, String childId) {
        throw new NullPointerException("Not yet implemented");
    }

    public boolean isAssociated(String parentId, String childId) {
        boolean found = false;

        System.out.println("Considering association from: " + parentId + " to: " + childId);
        try {


            GetItemSpec spec = new GetItemSpec();
            spec.withPrimaryKey("from_id", parentId, "to_id", childId);
            final Table table = this.associationTable;
            final Item item = table.getItem(spec);

            if (item != null) {
                found = true;
                System.out.println("Found existing association from: " + parentId + " to: " + childId);
            }
        } catch (Exception e) {
            System.err.println("Error getting association information: " + e.getMessage());
        }
        return found;
    }

    public String[] getAssociatedFrom(String parentId) {
        List aList = new ArrayList<>();

        System.out.println("Getting getAssociations From: " + parentId);
        ValueMap valueMap = new ValueMap();
        valueMap.withString(":from_id", parentId);

        QuerySpec querySpec = new QuerySpec()
                .withKeyConditionExpression("from_id = :from_id ")
                .withValueMap(valueMap);

        ItemCollection<QueryOutcome> outcomes= this.associationTable.query(querySpec);

        if (outcomes != null) {

            for (Iterator iterator = outcomes.iterator(); iterator.hasNext();){
                Item outcome = (Item) iterator.next();
                Map<String, Object> aMap = outcome.asMap();
//                String fromId = (String) aMap.get("from_id");
                String toId = (String) aMap.get("to_id");
                System.out.println("found association from: " + parentId + " to: " + toId);
                aList.add(toId);
            }
        }

        String[] keys = null;
        if (aList != null && aList.size() > 0) {
            keys = (String[]) aList.toArray(new String[aList.size()]);
        }
        return keys;
    }

    public String getAssociationOwnerFrom(String childId) {

        String key = this.buildChildHashToParentKey(childId);
        String ownerId = null;

        System.out.println("Getting getAssociationOwnerFrom: " + childId + " using key: " + key);

        GetItemSpec spec = new GetItemSpec();
        spec.withPrimaryKey("from_id", key);

        final Table table = this.associationTable;
        Item item = table.getItem(spec);

        if (item != null) {
            Map<String, Object> aMap = item.asMap();
            String fromId = (String) aMap.get("from_id");
            String toId = (String) aMap.get("to_id");

            ownerId = toId;
        }


        return ownerId;
    }

    private String buildChildHashToParentKey(String childId) {
        return "child#" + childId;
    }


}
