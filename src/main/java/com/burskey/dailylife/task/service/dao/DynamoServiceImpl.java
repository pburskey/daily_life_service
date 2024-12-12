package com.burskey.dailylife.task.service.dao;

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
import com.burskey.dailylife.party.service.ServiceException;
import com.burskey.dailylife.task.domain.*;
import com.burskey.dailylife.task.service.TaskService;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.*;


public class DynamoServiceImpl implements TaskService {

    protected final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
    protected final DynamoDB dynamoDB = new DynamoDB(client);
    protected final ObjectMapper mapper = new ObjectMapper().
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    private String taskTableName;
    private String taskInProgressTableName;


    public DynamoServiceImpl(String ataskTableName, String atipTableName) {
        this.taskTableName = ataskTableName;
        this.taskInProgressTableName = atipTableName;
    }


    @Override
    public Task getTask( String id) {
        Task task = null;


        ValueMap valueMap = new ValueMap();
        valueMap.withString(":taskID", id);

        QuerySpec querySpec = new QuerySpec()
                .withKeyConditionExpression("id = :taskID ")
                .withValueMap(valueMap)
                .withConsistentRead(true);
//        GetItemSpec spec = new GetItemSpec();
//        spec.withPrimaryKey("id", id);
//        System.out.println("Using table name: " + taskTableName);
//        final Table table = this.dynamoDB.getTable(this.taskTableName);
//        Item item = table.getItem(spec);
//
//        if (item != null) {
//            Map<String, Object> aMap = item.asMap();
//            String json = (String) aMap.get("details");
//            try {
//                task = mapper.readValue(json, SimpleTask.class);
//            } catch (JsonProcessingException e) {
//                throw new RuntimeException(e);
//            }
//        }

        ItemCollection<QueryOutcome> outcomes= this.dynamoDB.getTable(this.taskInProgressTableName).query(querySpec);

        if (outcomes != null) {

            for (Iterator iterator = outcomes.iterator(); iterator.hasNext();){
                Item outcome = (Item) iterator.next();
                String json = (String) outcome.get("details");

                try {
                    task = this.mapper.readValue(json, Task.class);
                } catch (JsonProcessingException e) {
                    throw new ServiceException("Encountered problem trying to rehydrate a task", e);
                }
            }
        }

        return task;
    }

    @Override
    public Task[] getTasksByParty(String partyId) {
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
    public TaskInProgress saveTaskInProgress(TaskInProgress taskInProgress) {
        if (taskInProgress != null) {
            try {
                SimpleTaskInProgress castTip = (SimpleTaskInProgress) taskInProgress;

                if (taskInProgress.getID() == null || taskInProgress.getID().isEmpty()) {
                    castTip.setId(UUID.randomUUID().toString());
                    SimpleStatusPoint castStatusPoint = (SimpleStatusPoint) taskInProgress.getStatus();
                    castStatusPoint.setId(UUID.randomUUID().toString());
                    String json = mapper.writeValueAsString(castTip);
                    final Item item = new Item()
                            .withPrimaryKey("id", taskInProgress.getID(), "task_id", taskInProgress.getTaskID())
                            .withString("details", json);

                    final Table table = this.dynamoDB.getTable(this.taskInProgressTableName);
                    table.putItem(item);

                } else {
                    String json = mapper.writeValueAsString(taskInProgress);
                    UpdateItemRequest updateItemRequest = new UpdateItemRequest();
                    updateItemRequest.setTableName(this.taskInProgressTableName);
                    updateItemRequest.addKeyEntry("id", new AttributeValue().withS(taskInProgress.getID()));
                    updateItemRequest.addExpressionAttributeValuesEntry(":details", new AttributeValue().withS(json));
                    updateItemRequest.addExpressionAttributeValuesEntry(":task_id", new AttributeValue().withS(taskInProgress.getTaskID()));
                    updateItemRequest.withUpdateExpression("set details = :details, task_id = :task_id");

                    UpdateItemResult result = this.client.updateItem(updateItemRequest);

                }
            } catch (Exception e) {
                System.err.println("Error updating item: " + e.getMessage());
            }
        }

        return taskInProgress;
    }

    @Override
    public StatusPoint[] getStatusPoints(String taskInProgressId) {
        return new StatusPoint[0];
    }

    @Override
    public TaskInProgress[] getByTask(String taskId) {
        List<TaskInProgress> aList = new ArrayList<>();

        ValueMap valueMap = new ValueMap();
        valueMap.withString(":taskID", taskId);


        QuerySpec querySpec = new QuerySpec()
                .withKeyConditionExpression("task_id = :taskID ")
                .withValueMap(valueMap)
                .withConsistentRead(true);

        ItemCollection<QueryOutcome> outcomes= this.dynamoDB.getTable(this.taskInProgressTableName).query(querySpec);

        if (outcomes != null) {

            for (Iterator iterator = outcomes.iterator(); iterator.hasNext();){
                Item outcome = (Item) iterator.next();
                String json = (String) outcome.get("details");
                TaskInProgress aTip = null;
                try {
                    aTip = this.mapper.readValue(json, TaskInProgress.class);
                } catch (JsonProcessingException e) {
                    throw new ServiceException("Encountered problem trying to rehydrate a task in progress", e);
                }

                aList.add(aTip);
            }
        }

        TaskInProgress[] tips = null;
        if (aList != null && aList.size() > 0) {
            tips = aList.toArray(new TaskInProgress[aList.size()]);
        }
        return tips;
    }

    @Override
    public TaskInProgress[] getByParty(String partyId) {
        return new TaskInProgress[0];
    }


    @Override
    public TaskInProgress start(Task task) {
        throw new com.burskey.dailylife.task.service.ServiceException("Do not call", new NullPointerException());
    }

    @Override
    public TaskInProgress changeTo(Task task, TaskInProgress taskInProgress, Status status) {
        throw new com.burskey.dailylife.task.service.ServiceException("Do not call", new NullPointerException());
    }

    @Override
    public TaskInProgress getTaskInProgress(String tipId) {
        return null;
    }

    @Override
    public TaskInProgress start(String s) {
        throw new com.burskey.dailylife.task.service.ServiceException("Do not call", new NullPointerException());
    }

    @Override
    public TaskInProgress changeTo(String s, String s1) {
        throw new com.burskey.dailylife.task.service.ServiceException("Do not call", new NullPointerException());
    }
}
