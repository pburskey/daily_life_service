package com.burskey.dailylife.party.api;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.burskey.dailylife.party.domain.Party;
import com.burskey.dailylife.party.service.PartyService;
import org.apache.http.HttpStatus;

public class PartyGetByID extends AbstractLambda {


    public PartyGetByID() {
        super();
    }

    public PartyGetByID(PartyService dao) {
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
                String id = event.getPathParameters().get("id");

                if (id == null || id.isEmpty()) {
                    response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
                    response.setBody("Missing id");
                } else {


                    logger.log("Searching for party: " + id);
                    Party party = this.getService().getParty(id);
                    if (party != null) {
                        response.setBody(this.getMapper().writeValueAsString(party));
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
