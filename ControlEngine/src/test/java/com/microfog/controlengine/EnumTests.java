//package com.microfog.scheduler;
//
//import com.microfog.scheduler.model.deployment.CommandTypeEnum;
//import io.quarkus.test.junit.QuarkusTest;
//import io.smallrye.common.constraint.Assert;
//import org.junit.jupiter.api.Test;
//
//import javax.inject.Inject;
//import java.util.List;
//
//@QuarkusTest
//public class EnumTests {
//
//    @Inject
//    DataMapperMetadata dataMapper;
//
//    @Test
//    public void testCommandEnums() {
//        dataMapper.writeObjectAsList("APP1_Commands",CommandTypeEnum.CREATE_NS);
//        dataMapper.writeObjectAsList("APP1_Commands",CommandTypeEnum.CREATE_S);
//        dataMapper.writeObjectAsList("APP1_Commands",CommandTypeEnum.CREATE_M);
//
//        List<CommandTypeEnum> commands = dataMapper.getListObject("APP1_Commands");
//        Assert.assertTrue(commands.get(0)==CommandTypeEnum.CREATE_NS);
//        Assert.assertTrue(commands.get(1)==CommandTypeEnum.CREATE_S);
//        Assert.assertTrue(commands.get(2)==CommandTypeEnum.CREATE_M);
//
//        Assert.assertTrue(dataMapper.deleteByKey("APP1_Commands", DataMapperMetadata.LIST));
//
//        dataMapper.disconnectRedisClient();
//    }
//}
