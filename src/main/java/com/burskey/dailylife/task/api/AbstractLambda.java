package com.burskey.dailylife.task.api;




import com.burskey.dailylife.task.service.TaskService;
import com.burskey.dailylife.task.service.TaskServiceImpl;
import com.burskey.dailylife.task.service.dao.DynamoServiceImpl;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractLambda {



    private TaskService service = null;
    private String ENV_TASK_TABLE = "";


    protected final ObjectMapper mapper = new ObjectMapper().
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);


    public AbstractLambda() {

        String taskTableName = System.getenv(ENV_TASK_TABLE);
        this.service = new TaskServiceImpl(new DynamoServiceImpl(taskTableName));
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
