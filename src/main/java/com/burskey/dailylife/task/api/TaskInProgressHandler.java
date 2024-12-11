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


    public APIGatewayProxyResponseEvent handleRequestGetByTask(APIGatewayProxyRequestEvent event, Context context) {

        LambdaLogger logger = context.getLogger();
        logger.log("Event Details:" + event);

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setIsBase64Encoded(false);
        response.setStatusCode(200);
        try {

            if (event != null && event.getPathParameters() != null) {
                String partyId = event.getPathParameters().get("partyid");
                String taskId = event.getPathParameters().get("task_id");
                if (partyId == null || partyId.isEmpty()) {
                    response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
                    response.setBody("Missing party id");
                } else if (taskId == null || taskId.isEmpty()) {
                    response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
                    response.setBody("Missing task id");
                } else {


                    logger.log("Searching for tasks in progress on party: " + partyId + " for task: " + taskId);
                    TaskInProgress[] tips = this.getService().getByTask(partyId, taskId);
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

    public APIGatewayProxyResponseEvent handleRequestSave(APIGatewayProxyRequestEvent event, Context context) {

        LambdaLogger logger = context.getLogger();
        logger.log("Event Details:" + event);

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setIsBase64Encoded(false);
        response.setStatusCode(200);
        try {

            if (event != null && event.getBody() != null && !event.getBody().isEmpty()) {
                TaskInProgress tip = this.getMapper().readValue(event.getBody(), TaskInProgress.class);

                if (tip == null ) {
                    response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
                    response.setBody("Missing task in progress");
                } else {
                    logger.log("Saving task in progress: ");
                    tip = this.getService().saveTaskInProgress(tip);
                    if (tip != null) {
                        response.setBody(this.getMapper().writeValueAsString(tip));
                    }

                }
            }

        }
        catch (ConstraintViolationException e) {
            logger.log(e.getMessage());
            response.setBody(e.getMessage());
            response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
        }
        catch (Exception e) {
            logger.log(e.getMessage());
            response.setBody(e.getMessage());
            response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        return response;

    }

}
