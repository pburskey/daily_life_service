package com.burskey.dailylife.party.api;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.burskey.dailylife.party.domain.Communication;
import com.burskey.dailylife.party.service.PartyService;
import org.apache.http.HttpStatus;

public class CommunicationsGetByParty extends AbstractLambda {


    public CommunicationsGetByParty() {
        super();
    }

    public CommunicationsGetByParty(PartyService dao) {
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


                    logger.log("Searching for communications by party: " + partyId);
                    Communication[] communications = this.getService().getCommunicationsByParty(partyId);
                    if (communications != null) {
                        response.setBody(this.getMapper().writeValueAsString(communications));
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
