package com.burskey.dailylife.task.api;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.burskey.dailylife.task.domain.Task;
import com.burskey.dailylife.task.service.TaskService;
import jakarta.validation.ConstraintViolationException;
import org.apache.http.HttpStatus;

public class TaskSave extends AbstractLambda {


    public TaskSave() {
        super();
    }

    public TaskSave(TaskService dao) {
        super(dao);
    }

    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {

        LambdaLogger logger = context.getLogger();
        logger.log("Event Details:" + event);

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setIsBase64Encoded(false);
        response.setStatusCode(200);
        try {

            if (event != null && event.getBody() != null && !event.getBody().isEmpty()) {
                Task task = this.getMapper().readValue(event.getBody(), Task.class);

                if (task == null ) {
                    response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
                    response.setBody("Missing task");
                } else {


                    logger.log("Saving party: ");
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

}
