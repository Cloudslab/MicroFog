package com.microfog.controlengine.metadata;

import com.microfog.controlengine.controllers.DataMapperMetadata;
import com.microfog.controlengine.model.domainObjects.Application;
import io.minio.errors.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Singleton
public class MeteDataGenerator {

    @Inject
    DataMapperMetadata dataMapperMetadata;

    @Inject
    Utils utils;

    List<Application> applications = new ArrayList<>();

    public void  generetaMetaData() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        applications.add(new BookinfoSetup(utils).generateApplicationObject());
        applications.add(new DummyApp1Setup(utils).generateApplicationObject());
        applications.add(new DummyApp2Setup(utils).generateApplicationObject());
        applications.add(new SmartHealthAppSetup(utils).generateApplicationObject());

        for(Application app:applications){
            dataMapperMetadata.writeObjectAsBucket(app.getApplicationId(),app);
        }
    }





}
