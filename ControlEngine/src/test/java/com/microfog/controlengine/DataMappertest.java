//package com.microfog.scheduler;
//
//import com.microfog.scheduler.model.deployment.Namespace;
//import io.quarkus.test.junit.QuarkusTest;
//import io.smallrye.common.constraint.Assert;
//import org.junit.jupiter.api.Test;
//
//import javax.inject.Inject;
//import java.util.HashMap;
//import java.util.Map;
//
//
//@QuarkusTest
//public class DataMappertest {
//    @Inject
//    DataMapperMetadata dataMapper;
//    @Test
//    public void testDataMapper() {
//        Map<String,String> nsLabels = new HashMap<>();
//        nsLabels.put("istio-injection","enabled");
//        Namespace ns = new Namespace("sampleAPP","sample", nsLabels);
//
//        dataMapper.writeObjectAsBucket("ns_sample", ns);
//
//        Namespace nsR = dataMapper.getObjectFromBucket("ns_sample");
//        Assert.assertTrue(nsR.getName().equals("sample"));
//
//        Assert.assertTrue(dataMapper.deleteByKey("ns_sample", DataMapperMetadata.BUCKET));
//        dataMapper.disconnectRedisClient();
//    }
//}
