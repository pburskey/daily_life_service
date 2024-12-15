package com.burskey.dailylife.task.api;




import com.burskey.dailylife.dynamodb.AssociationDynamoImpl;
import com.burskey.dailylife.task.service.TaskService;
import com.burskey.dailylife.task.service.TaskServiceImpl;
import com.burskey.dailylife.task.service.dao.DynamoServiceImpl;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractLambda {



    private TaskService service = null;
    private String ENV_TASK_TABLE = "TASK_TABLE";
    private String ENV_TASK_IN_PROGRESS_TABLE = "TASK_IN_PROGRESS_TABLE";
    private String ENV_ASSOCIATION_TABLE = "ASSOCIATION_TABLE";


    protected final ObjectMapper mapper = new ObjectMapper().
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);


    public AbstractLambda() {

        String taskTableName = System.getenv(ENV_TASK_TABLE);
        String tipTableName = System.getenv(ENV_TASK_IN_PROGRESS_TABLE);
        String associationTableName = System.getenv(ENV_ASSOCIATION_TABLE);
        this.service = new TaskServiceImpl(new DynamoServiceImpl(new AssociationDynamoImpl(associationTableName), taskTableName, tipTableName));
    }


    public AbstractLambda(TaskService dao) {
        this.service = dao;
    }

    public TaskService getService() {
        return service;
    }

    public ObjectMapper getMapper() {
        return mapper;
    }
}
