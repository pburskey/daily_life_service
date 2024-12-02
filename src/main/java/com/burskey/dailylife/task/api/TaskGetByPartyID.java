package com.burskey.dailylife.task.api;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.burskey.dailylife.task.domain.Task;
import com.burskey.dailylife.task.service.TaskService;
import org.apache.http.HttpStatus;

public class TaskGetByPartyID extends AbstractLambda {


    public TaskGetByPartyID() {
        super();
    }

    public TaskGetByPartyID(TaskService dao) {
        super(dao);
    }

    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {

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

}
