package com.microfog.controlengine.controllers;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class DataMapperMetadata {

    private final static Logger LOGGER = Logger.getLogger(DataMapperMetadata.class.getName());

    //Radisson Object type
    public static final int BUCKET = 1;
    public static final int LIST = 2;

    RedissonClient redisClient;

    @PostConstruct
    public void init() {
        String redisUrl = ConfigProvider.getConfig().getValue("redis.url", String.class);
        Config config = new Config();
        config.useSingleServer().setAddress(redisUrl);
        redisClient = Redisson.create(config);
    }

    public <T extends Object> void writeObjectAsBucket(String key, T objectToAdd){
        RBucket<T> bucket = redisClient.getBucket(key);
        bucket.set(objectToAdd);
        T object = bucket.get();
    }


    public <T extends Object> T getObjectFromBucket(String key){
        RBucket<T> bucket = redisClient.getBucket(key);
        T object = bucket.get();
        return object;
    }

    public <T extends Object> void writeObjectAsList(String key, T objectToAdd){
        RList<T> list = redisClient.getList(key);
        list.add(objectToAdd);
    }

    public <T extends Object> List<T> getListObject(String key){
        RList<T> listR = redisClient.getList(key);
        List<T> list = listR.readAll();
        return list;
    }

    public boolean deleteByKey(String key, int objectType){
        switch (objectType){
            case BUCKET:
                return redisClient.getBucket(key).delete();
            case LIST:
                return redisClient.getList(key).delete();
            default:
                return false;
        }
    }

    public void disconnectRedisClient(){
        redisClient.shutdown();
    }

    public boolean ifExists(String key){
        if(redisClient.getKeys().countExists(key)>0)
            return true;
        else return false;
    }

}
