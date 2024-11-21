package com.burskey.dailylife.party.api;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.burskey.dailylife.party.domain.Communication;
import com.burskey.dailylife.party.service.PartyService;
import jakarta.validation.ConstraintViolationException;
import org.apache.http.HttpStatus;

public class CommunicationSave extends AbstractLambda {


    public CommunicationSave() {
        super();
    }

    public CommunicationSave(PartyService dao) {
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
                Communication communication = this.getMapper().readValue(event.getBody(), Communication.class);

                if (communication == null ) {
                    response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
                    response.setBody("Missing communication");
                } else {


                    logger.log("Saving communication: ");
                    communication = this.getService().saveCommunication(communication);
                    if (communication != null) {
                        response.setBody(this.getMapper().writeValueAsString(communication));
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
