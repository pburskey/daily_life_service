package com.burskey.dailylife.task.api;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import com.burskey.dailylife.task.domain.Task;
import com.burskey.dailylife.task.service.TaskService;
import org.apache.http.HttpStatus;

public class TaskGetByID extends AbstractLambda {


    public TaskGetByID() {
        super();
    }

    public TaskGetByID(TaskService dao) {
        super(dao);
    }

//    public TaskInProgress changeTo(TaskInProgress tip, Status status);
    public APIGatewayProxyResponseEvent handleRequest_changeStatus(APIGatewayProxyRequestEvent event, Context context) {

        LambdaLogger logger = context.getLogger();
        logger.log("Event Details:" + event);

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setIsBase64Encoded(false);
        response.setStatusCode(200);
        try {

            if (event != null && event.getPathParameters() != null) {
                String taskId = event.getPathParameters().get("task_id");
                String partyId = event.getPathParameters().get("partyid");
                if (taskId == null || taskId.isEmpty()) {
                    response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
                    response.setBody("Missing task id");
                } else if (partyId == null || partyId.isEmpty()) {
                    response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
                    response.setBody("Missing party id");
                } else {


                    logger.log("Searching for task: " + taskId + " on party: " + partyId);
                    Task task = this.getService().getTask( taskId);
                    if (task != null) {
                        response.setBody(this.getMapper().writeValueAsString(task));
                    }
                }

            }

        } catch (Exception e) {
            logger.log(e.getMessage());
            response.setBody(e.getMessage());
            response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        return response;

    }

}
