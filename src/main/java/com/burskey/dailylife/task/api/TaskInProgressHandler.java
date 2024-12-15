package com.burskey.dailylife.task.api;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.burskey.dailylife.task.domain.Task;
import com.burskey.dailylife.task.domain.TaskInProgress;
import com.burskey.dailylife.task.service.TaskService;
import jakarta.validation.ConstraintViolationException;
import org.apache.http.HttpStatus;

public class TaskInProgressHandler extends AbstractLambda {


    public TaskInProgressHandler() {
        super();
    }

    public TaskInProgressHandler(TaskService dao) {
        super(dao);
    }


    public APIGatewayProxyResponseEvent handleRequest_GetByTask(APIGatewayProxyRequestEvent event, Context context) {

        LambdaLogger logger = context.getLogger();
        logger.log("Event Details:" + event);

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setIsBase64Encoded(false);
        response.setStatusCode(200);
        try {

            if (event != null && event.getPathParameters() != null) {
                String taskId = event.getPathParameters().get("task_id");
                if (taskId == null || taskId.isEmpty()) {
                    response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
                    response.setBody("Missing task id");
                } else {


                    logger.log("Searching for tasks in progress for task: " + taskId);
                    TaskInProgress[] tips = this.getService().getByTask( taskId);
                    if (tips != null) {
                        response.setBody(this.getMapper().writeValueAsString(tips));
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


    public APIGatewayProxyResponseEvent handleRequest_changeStatus(APIGatewayProxyRequestEvent event, Context context) {

        LambdaLogger logger = context.getLogger();
        logger.log("Event Details:" + event);

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setIsBase64Encoded(false);
        response.setStatusCode(200);
        try {

            if (event != null && event.getPathParameters() != null) {
                String tipId = event.getPathParameters().get("tip_id");
                String statusId = event.getPathParameters().get("status_id");
                if (tipId == null || tipId.isEmpty()) {
                    response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
                    response.setBody("Missing task in progress id");
                } else if (statusId == null || statusId.isEmpty()) {
                    response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
                    response.setBody("Missing status id");
                } else {


                    logger.log("Attempting to change status of task in progress: " + tipId + " to status id: " + statusId);
                    TaskInProgress tip = this.getService().changeTo(tipId, statusId);
                    if (tip != null) {
                        response.setBody(this.getMapper().writeValueAsString(tip));
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
