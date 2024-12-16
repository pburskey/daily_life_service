package com.burskey.dailylife.party.service.dao;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.amazonaws.services.dynamodbv2.model.UpdateItemResult;
import com.burskey.dailylife.party.domain.Communication;
import com.burskey.dailylife.party.domain.Party;
import com.burskey.dailylife.party.domain.Person;
import com.burskey.dailylife.party.service.PartyService;
import com.burskey.dailylife.party.service.ServiceException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


import java.util.*;


public class DynamoServiceImpl implements PartyService {

    protected final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
    protected final DynamoDB dynamoDB = new DynamoDB(client);
    protected final ObjectMapper mapper = new ObjectMapper().
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    private final Table partyTable;
    private final Table communicationTable;


    public DynamoServiceImpl(String partyTableName, String communicationTableName) {
        this.partyTable = dynamoDB.getTable(partyTableName);
        this.communicationTable = dynamoDB.getTable(communicationTableName);
    }

    @Override
    public Party getParty(String partyId) {
        Party party = null;

        GetItemSpec spec = new GetItemSpec();
        spec.withPrimaryKey("id", partyId);
        final Table table = this.partyTable;
        Item item = table.getItem(spec);

        if (item != null) {
            Map<String, Object> aMap = item.asMap();
            String json = (String) aMap.get("details");
            try {
                party = mapper.readValue(json, Person.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        return party;

    }

    @Override
    public Party saveParty(Party party) {

        if (party != null) {
            try {

                if (party.getId() == null || party.getId().isEmpty()) {
                    party.setId(UUID.randomUUID().toString());
                    String json = mapper.writeValueAsString(party);
                    final Item item = new Item()
                            .withPrimaryKey("id", party.getId())
                            .withString("details", json);

                    final Table table = this.partyTable;
                    table.putItem(item);

                } else {
                    String json = mapper.writeValueAsString(party);
                    Person person = (Person) party;
                    UpdateItemRequest updateItemRequest = new UpdateItemRequest();
                    updateItemRequest.setTableName(this.partyTable.getTableName());
                    updateItemRequest.addKeyEntry("id", new AttributeValue().withS(party.getId()));
                    updateItemRequest.addExpressionAttributeValuesEntry(":details", new AttributeValue().withS(json));
                    updateItemRequest.withUpdateExpression("set details = :details");

                    UpdateItemResult result = this.client.updateItem(updateItemRequest);

                }
            } catch (Exception e) {
                System.err.println("Error updating item: " + e.getMessage());
            }
        }
        return party;
    }

    @Override
    public Communication getCommunication(String partyId, String communicationID) {
        Communication communication = null;


        GetItemSpec spec = new GetItemSpec();
        spec.withPrimaryKey("id", communicationID,"party_id", partyId);

        final Table table = this.communicationTable;
        Item item = table.getItem(spec);

        if (item != null) {
            Map<String, Object> aMap = item.asMap();
            String json = (String) aMap.get("details");
            try {
                communication = mapper.readValue(json, Communication.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        return communication;
    }

    @Override
    public Communication saveCommunication(Communication communication) {
        if (communication != null) {
            try {

                if (communication.getId() == null || communication.getId().isEmpty()) {
                    communication.setId(UUID.randomUUID().toString());
                    String json = mapper.writeValueAsString(communication);
                    final Item item = new Item()
                            .withPrimaryKey("id", communication.getId(),"party_id", communication.getPartyID())
                            .withString("details", json);

                    final Table table = this.communicationTable;
                    table.putItem(item);

                } else {
                    String json = mapper.writeValueAsString(communication);
                    UpdateItemRequest updateItemRequest = new UpdateItemRequest();
                    updateItemRequest.setTableName(this.communicationTable.getTableName());
                    updateItemRequest.addKeyEntry("id", new AttributeValue().withS(communication.getId()));
                    updateItemRequest.addExpressionAttributeValuesEntry(":details", new AttributeValue().withS(json));
                    updateItemRequest.addExpressionAttributeValuesEntry(":partyID", new AttributeValue().withS(communication.getPartyID()));
                    updateItemRequest.withUpdateExpression("set details = :details, party_id = :partyID");

                    UpdateItemResult result = this.client.updateItem(updateItemRequest);

                }
            } catch (Exception e) {
                System.err.println("Error updating item: " + e.getMessage());
            }
        }

        return communication;
    }


    @Override
    public Communication[] getCommunicationsByParty(String partyID) {
        List<Communication> aList = new ArrayList<>();

        ValueMap valueMap = new ValueMap();
        valueMap.withString(":partyID", partyID);


        QuerySpec querySpec = new QuerySpec()
                .withKeyConditionExpression("party_id = :partyID ")
                .withValueMap(valueMap)
                .withConsistentRead(true);

        ItemCollection<QueryOutcome> outcomes= this.communicationTable.query(querySpec);

        if (outcomes != null) {

            for (Iterator iterator = outcomes.iterator(); iterator.hasNext();){
                Item outcome = (Item) iterator.next();
                String json = (String) outcome.get("details");
                Communication aCommunication = null;
                try {
                    aCommunication = this.mapper.readValue(json, Communication.class);
                } catch (JsonProcessingException e) {
                    throw new ServiceException("Encountered problem trying to rehydrate a communication", e);
                }

                aList.add(aCommunication);
            }
        }

        Communication[] communications = null;
        if (aList != null && aList.size() > 0) {
            communications = aList.toArray(new Communication[aList.size()]);
        }
        return communications;
    }
}
