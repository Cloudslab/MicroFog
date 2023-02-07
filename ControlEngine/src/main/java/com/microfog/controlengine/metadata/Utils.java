package com.microfog.controlengine.metadata;

import com.microfog.controlengine.controllers.DataMapperYamlFiles;
import com.microfog.controlengine.model.deployment.*;
import io.minio.errors.*;
import org.eclipse.microprofile.config.ConfigProvider;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Singleton
public class Utils {
    @Inject
    DataMapperYamlFiles dataMapperYamlFiles;

    public void updateYamlUrl(List<Command> commands, String appId) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        for(Command c:commands){
            if(c.getCommandLevel().equals(CommandLevelEnum.APP_LEVEL)){
                AppLevelResource appLevelResource = (AppLevelResource) c.getResource();
                if(appLevelResource.getYamlFile()!=null){
                    String bucketName = ConfigProvider.getConfig().getValue("minio.bucketname", String.class);
                    String obejectName = appId + "/" + appLevelResource.getYamlFile();
                    URL url = new URL(dataMapperYamlFiles.getURLforObjectGET(bucketName,obejectName));
                    appLevelResource.setYamlUrl(url);
                }
            }
            else if(c.getCommandLevel().equals(CommandLevelEnum.COMPOSITE_SERVICE_LEVEL)){
                ServiceLevelResource serviceLevelResource = (ServiceLevelResource) c.getResource();
                if(serviceLevelResource.getYamlFile()!=null){
                    String bucketName = ConfigProvider.getConfig().getValue("minio.bucketname", String.class);
                    String obejectName = appId + "/" + serviceLevelResource.getYamlFile();
                    URL url = new URL(dataMapperYamlFiles.getURLforObjectGET(bucketName,obejectName));
                    serviceLevelResource.setYamlUrl(url);
                }
            }
            else if(c.getCommandLevel().equals(CommandLevelEnum.MICROSERVICE_INSTANCE_LEVEL)){
                MicroserviceLevelResource microserviceLevelResource = (MicroserviceLevelResource) c.getResource();
                if(microserviceLevelResource.getYamlFile()!=null){
                    String bucketName = ConfigProvider.getConfig().getValue("minio.bucketname", String.class);
                    String obejectName = appId + "/" + microserviceLevelResource.getYamlFile();
                    URL url = new URL(dataMapperYamlFiles.getURLforObjectGET(bucketName,obejectName));
                    microserviceLevelResource.setYamlUrl(url);
                }
            }
        }
    }

    public BigDecimal getbytesForMib(int i) {
        return  new BigDecimal(i*1024*1024);
    }
}
