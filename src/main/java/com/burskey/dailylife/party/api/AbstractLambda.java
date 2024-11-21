package com.burskey.dailylife.party.api;


import com.burskey.dailylife.party.service.PartyService;
import com.burskey.dailylife.party.service.ServiceImpl;
import com.burskey.dailylife.party.service.dao.DynamoServiceImpl;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractLambda {


    protected static final String ENV_PARTY_TABLE = "PARTY_TABLE";
    protected static final String ENV_COMMUNICATION_TABLE = "COMMUNICATION_TABLE";
    private PartyService service = null;

    protected final ObjectMapper mapper = new ObjectMapper().
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);


    public AbstractLambda() {

        String partyTableName = System.getenv(ENV_PARTY_TABLE);
        String communicationTableName = System.getenv(ENV_COMMUNICATION_TABLE);

        this.service = new ServiceImpl(new DynamoServiceImpl(partyTableName, communicationTableName));
    }


    public AbstractLambda(PartyService dao) {
        this.service = dao;
    }

    public PartyService getService() {
        return service;
    }

    public ObjectMapper getMapper() {
        return mapper;
    }
}
