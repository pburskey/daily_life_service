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
import com.burskey.dailylife.dynamodb.AssociationService;
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

    final Table taskTable;
    final Table taskInProgressTable;
    private AssociationService associationService;

    public DynamoServiceImpl(AssociationService anAssociationService, String ataskTableName, String atipTableName) {
        this.taskTable = this.dynamoDB.getTable(ataskTableName);
        this.taskInProgressTable = this.dynamoDB.getTable(atipTableName);
        this.associationService = anAssociationService;
    }


    @Override
    public Task getTask(String id) {
        Task task = null;

        System.out.println("Getting task: " + id);

        GetItemSpec spec = new GetItemSpec();
        spec.withPrimaryKey("id", id);


        Item item = taskTable.getItem(spec);

        if (item != null) {
            Map<String, Object> aMap = item.asMap();
            String json = (String) aMap.get("details");
            try {
                task = mapper.readValue(json, Task.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("Completed Getting task: " + id);
        return task;
    }

    @Override
    public Task[] getTasksByParty(String partyId) {

        String[] ids = this.associationService.getAssociatedFrom(partyId);
        List<Task> aList = new ArrayList<>();
        Task[] tasks = null;
        if (ids != null && ids.length > 0) {
            for (int i = 0; i < ids.length; i++) {
                String id = ids[i];
                Task task = this.getTask(id);
                if (task != null) {
                    aList.add(task);
                } else {
                    System.err.println("Could not find task: " + id);
                }
            }

            if (aList != null && aList.size() > 0) {
                tasks = aList.toArray(new Task[aList.size()]);
            }

        }

        return tasks;
    }

    @Override
    public Task saveTask(Task task) {
        if (task != null) {
            try {
                SimpleTask castTask = (SimpleTask) task;

                if (task.getId() == null || task.getId().isEmpty()) {
                    castTask.setId(UUID.randomUUID().toString());

                    String json = mapper.writeValueAsString(task);
                    System.out.println("Inserting new Task: " + castTask.getId());
                    final Item item = new Item()
                            .withPrimaryKey("id", task.getId())
                            .withString("details", json);


                    taskTable.putItem(item);
                    System.out.println("Inserted new Task: " + castTask.getId());
                    this.associationService.associate(task.getPartyID(), task.getId());


                    System.out.println("Completed insertion of Task: " + castTask.getId());

                } else {
                    String json = mapper.writeValueAsString(task);
                    UpdateItemRequest updateItemRequest = new UpdateItemRequest();
                    updateItemRequest.setTableName(this.taskTable.getTableName());
                    updateItemRequest.addKeyEntry("id", new AttributeValue().withS(task.getId()));
                    updateItemRequest.addExpressionAttributeValuesEntry(":details", new AttributeValue().withS(json));
                    updateItemRequest.withUpdateExpression("set details = :details");

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
                    System.out.println("Inserting a task in progress: " + taskInProgress.getID());
                    SimpleStatusPoint castStatusPoint = (SimpleStatusPoint) taskInProgress.getStatus();
                    castStatusPoint.setId(UUID.randomUUID().toString());
                    String json = mapper.writeValueAsString(castTip);
                    final Item item = new Item()
                            .withPrimaryKey("id", taskInProgress.getID())
                            .withString("details", json);


                    this.taskInProgressTable.putItem(item);
                    System.out.println("Completed insertion of task in progress: " + taskInProgress.getID());

                    this.associationService.associate(taskInProgress.getTaskID(), taskInProgress.getID());


                } else {
                    String json = mapper.writeValueAsString(taskInProgress);
                    UpdateItemRequest updateItemRequest = new UpdateItemRequest();
                    updateItemRequest.setTableName(this.taskInProgressTable.getTableName());
                    updateItemRequest.addKeyEntry("id", new AttributeValue().withS(taskInProgress.getID()));
                    updateItemRequest.addExpressionAttributeValuesEntry(":details", new AttributeValue().withS(json));
                    updateItemRequest.withUpdateExpression("set details = :details");

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
        String[] ids = this.associationService.getAssociatedFrom(taskId);

        TaskInProgress[] tips = null;
        if (ids != null && ids.length > 0) {
            System.out.println("Found: " + ids.length + " associated task in progress ids for task: " + taskId);
            for (int i = 0; i < ids.length; i++) {
                String id = ids[i];
                TaskInProgress tip = this.getTaskInProgress(id);
                if (tip != null) {
                    aList.add(tip);
                } else {
                    System.err.println("Could not find task in progress: " + id);
                }
            }

            if (aList != null && aList.size() > 0) {
                tips = aList.toArray(new TaskInProgress[aList.size()]);
            }

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
        TaskInProgress tip = null;

        System.out.println("Getting task in progress: " + tipId);

        GetItemSpec spec = new GetItemSpec();
        spec.withPrimaryKey("id", tipId);


        Item item = taskInProgressTable.getItem(spec);

        if (item != null) {
            Map<String, Object> aMap = item.asMap();
            String json = (String) aMap.get("details");
            try {
                tip = mapper.readValue(json, TaskInProgress.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("Completed Getting task in progress: " + tipId);
        return tip;
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
