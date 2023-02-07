package com.microfog.controlengine.metadata;

import com.microfog.controlengine.model.domainObjects.Application;
import io.minio.errors.*;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public abstract class ApplicationSetup {

    Utils utils;

    public ApplicationSetup(Utils utils){
        this.utils = utils;
    }

    public abstract Application generateApplicationObject() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;
}
