package com.burskey.dailylife.task.api;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.burskey.dailylife.task.domain.SimpleTask;
import com.burskey.dailylife.task.domain.Task;
import com.burskey.dailylife.task.domain.TaskInProgress;
import com.burskey.dailylife.task.service.TaskService;
import jakarta.validation.ConstraintViolationException;
import org.apache.http.HttpStatus;

public class TaskHandler extends AbstractLambda {


    public TaskHandler() {
        super();
    }

    public TaskHandler(TaskService dao) {
        super(dao);
    }

    public APIGatewayProxyResponseEvent handleRequest_save(APIGatewayProxyRequestEvent event, Context context) {

        LambdaLogger logger = context.getLogger();
        logger.log("Event Details:" + event);

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setIsBase64Encoded(false);
        response.setStatusCode(200);
        try {

            if (event != null && event.getBody() != null && !event.getBody().isEmpty()) {
                Task task = this.getMapper().readValue(event.getBody(), SimpleTask.class);

                if (task == null ) {
                    response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
                    response.setBody("Missing task");
                } else {


                    logger.log("Saving task: ");
                    task = this.getService().saveTask(task);
                    if (task != null) {
                        response.setBody(this.getMapper().writeValueAsString(task));
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



    //    public TaskInProgress changeTo(TaskInProgress tip, Status status);
    public APIGatewayProxyResponseEvent handleRequest_getTaskByID(APIGatewayProxyRequestEvent event, Context context) {

        LambdaLogger logger = context.getLogger();
        logger.log("Event Details:" + event);

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setIsBase64Encoded(false);
        response.setStatusCode(200);
        try {

            if (event != null && event.getPathParameters() != null) {
                String taskId = event.getPathParameters().get("task_id");
//                String partyId = event.getPathParameters().get("partyid");
                if (taskId == null || taskId.isEmpty()) {
                    response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
                    response.setBody("Missing task id");
                }  else {


                    logger.log("Searching for task: " + taskId );
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



    public APIGatewayProxyResponseEvent handleRequest_getByParty(APIGatewayProxyRequestEvent event, Context context) {

        LambdaLogger logger = context.getLogger();
        logger.log("Event Details:" + event);

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setIsBase64Encoded(false);
        response.setStatusCode(200);
        try {

            if (event != null && event.getPathParameters() != null) {
                String partyId = event.getPathParameters().get("partyid");
                if (partyId == null || partyId.isEmpty()) {
                    response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
                    response.setBody("Missing party id");
                } else {


                    logger.log("Searching for tasks on party: " + partyId);
                    Task[] tasks = this.getService().getTasksByParty(partyId);
                    if (tasks != null) {
                        response.setBody(this.getMapper().writeValueAsString(tasks));
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


    //    public TaskInProgress start(Task task);
    public APIGatewayProxyResponseEvent handleRequest_Start(APIGatewayProxyRequestEvent event, Context context) {

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


                    logger.log("starting task: " + taskId);
                    TaskInProgress tip = this.getService().start(taskId);
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
