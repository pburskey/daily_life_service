package com.burskey.dailylife.task.service.dao;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.amazonaws.services.dynamodbv2.model.UpdateItemResult;
import com.burskey.dailylife.task.domain.SimpleTask;
import com.burskey.dailylife.task.domain.StatusPoint;
import com.burskey.dailylife.task.domain.Task;
import com.burskey.dailylife.task.domain.TaskInProgress;
import com.burskey.dailylife.task.service.TaskService;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.Map;
import java.util.UUID;


public class DynamoServiceImpl implements TaskService {

    protected final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
    protected final DynamoDB dynamoDB = new DynamoDB(client);
    protected final ObjectMapper mapper = new ObjectMapper().
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    private String taskTableName;


    public DynamoServiceImpl(String ataskTableName) {
        this.taskTableName = ataskTableName;

    }


    @Override
    public Task getTask(String partyId, String id) {
        Task task = null;

        GetItemSpec spec = new GetItemSpec();
        spec.withPrimaryKey("id", id,"party_id", partyId);
        System.out.println("Using table name: " + taskTableName);
        final Table table = this.dynamoDB.getTable(this.taskTableName);
        Item item = table.getItem(spec);

        if (item != null) {
            Map<String, Object> aMap = item.asMap();
            String json = (String) aMap.get("details");
            try {
                task = mapper.readValue(json, SimpleTask.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        return task;
    }

    @Override
    public Task[] getTasksByParty(String s) {
        return new Task[0];
    }

    @Override
    public Task saveTask(Task task) {
        if (task != null) {
            try {
                SimpleTask castTask = (SimpleTask) task;

                if (task.getId() == null || task.getId().isEmpty()) {
                    castTask.setId(UUID.randomUUID().toString());
                    String json = mapper.writeValueAsString(task);
                    final Item item = new Item()
                            .withPrimaryKey("id", task.getId(), "party_id", task.getPartyID())
                            .withString("details", json);

                    final Table table = this.dynamoDB.getTable(this.taskTableName);
                    table.putItem(item);

                } else {
                    String json = mapper.writeValueAsString(task);
                    UpdateItemRequest updateItemRequest = new UpdateItemRequest();
                    updateItemRequest.setTableName(this.taskTableName);
                    updateItemRequest.addKeyEntry("id", new AttributeValue().withS(task.getId()));
                    updateItemRequest.addExpressionAttributeValuesEntry(":details", new AttributeValue().withS(json));
                    updateItemRequest.addExpressionAttributeValuesEntry(":partyID", new AttributeValue().withS(task.getPartyID()));
                    updateItemRequest.withUpdateExpression("set details = :details, party_id = :partyID");

                    UpdateItemResult result = this.client.updateItem(updateItemRequest);

                }
            } catch (Exception e) {
                System.err.println("Error updating item: " + e.getMessage());
            }
        }

        return task;
    }


    @Override
    public TaskInProgress save(TaskInProgress taskInProgress) {
        return null;
    }

    @Override
    public StatusPoint[] getStatusPoints(String s) {
        return new StatusPoint[0];
    }
}
