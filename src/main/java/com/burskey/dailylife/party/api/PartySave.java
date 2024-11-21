package com.burskey.dailylife.party.api;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.burskey.dailylife.party.domain.Party;
import com.burskey.dailylife.party.domain.Person;
import com.burskey.dailylife.party.domain.ValidationException;
import com.burskey.dailylife.party.service.PartyService;
import jakarta.validation.ConstraintViolationException;
import org.apache.http.HttpStatus;

public class PartySave extends AbstractLambda {


    public PartySave() {
        super();
    }

    public PartySave(PartyService dao) {
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
                Party party = this.getMapper().readValue(event.getBody(), Person.class);

                if (party == null ) {
                    response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
                    response.setBody("Missing party");
                } else {


                    logger.log("Saving party: ");
                    party = this.getService().saveParty(party);
                    if (party != null) {
                        response.setBody(this.getMapper().writeValueAsString(party));
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
