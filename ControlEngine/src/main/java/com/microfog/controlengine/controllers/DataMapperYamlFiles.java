package com.microfog.controlengine.controllers;

import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.errors.*;
import io.minio.http.Method;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Singleton
public class DataMapperYamlFiles {

    private final static Logger LOGGER = Logger.getLogger(DataMapperYamlFiles.class.getName());

    String minioEndPoint = ConfigProvider.getConfig().getValue("minio.minioEndPoint", String.class);
    String accessKey = ConfigProvider.getConfig().getValue("minio.accesskey", String.class);
    String secretkey = ConfigProvider.getConfig().getValue("minio.secretkey", String.class);

    MinioClient minioClient;

    @PostConstruct
    public void init() {
       minioClient = MinioClient.builder().endpoint(minioEndPoint).
                credentials(accessKey, secretkey)
                . build();
    }

    /**
     *
     * @param bucketName
     * @param objectName  foldername (appId) /objectName (objectname matches what's store in redis for that command)
     * @return
     */
    public String getURLforObjectGET(String bucketName, String objectName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());

        if(found) {
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder().bucket(bucketName)
                            .object(objectName).method(Method.GET).build()
            );
            return url;
        }
        else{
            LOGGER.error("Bucket named - " + bucketName + " - not available");
            return "";
        }
    }

    //todo add method to add yaml



}
