package com.microfog.controlengine.metadata;


import com.microfog.controlengine.model.deployment.*;
import com.microfog.controlengine.model.domainObjects.Application;
import com.microfog.controlengine.model.domainObjects.CompositeService;
import com.microfog.controlengine.model.domainObjects.Dataflow;
import com.microfog.controlengine.model.domainObjects.Microservice;
import io.minio.errors.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class DummyApp1Setup extends ApplicationSetup {
    public DummyApp1Setup(Utils utils) {
        super(utils);
    }

    public Application generateApplicationObject() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        String applicationId = "app1";
        /** application model
         *  1. microservices (6)
         *  2. Dataflows
         *  3. composite services (4)
         */
        Microservice a1m1 = new Microservice("APP1_m1", new HashMap<>(){
            {
                put(Microservice.CPU, new BigDecimal(1.0));
                put(Microservice.RAM, utils.getbytesForMib(100));
                put(Microservice.THROUGHPUT, new BigDecimal(100));
            }
        });
        Microservice a1m2 = new Microservice("APP1_m2", new HashMap<>(){
            {
                put(Microservice.CPU, new BigDecimal(0.8));
                put(Microservice.RAM, utils.getbytesForMib(100));
                put(Microservice.THROUGHPUT, new BigDecimal(100));
            }
        });
        Microservice a1m3 = new Microservice("APP1_m3", new HashMap<>(){
            {
                put(Microservice.CPU, new BigDecimal(1.0));
                put(Microservice.RAM, utils.getbytesForMib(100));
                put(Microservice.THROUGHPUT, new BigDecimal(100));
            }
        });
        Microservice a1m4 = new Microservice("APP1_m4", new HashMap<>(){
            {
                put(Microservice.CPU, new BigDecimal(0.8));
                put(Microservice.RAM, utils.getbytesForMib(100));
                put(Microservice.THROUGHPUT, new BigDecimal(100));
            }
        });
        List<Microservice> microservices = Arrays.asList(new Microservice[]{a1m1, a1m2, a1m3,a1m4});

        Dataflow m1_m2 = new Dataflow(a1m1,a1m2,true);
        Dataflow m2_m1 = new Dataflow(a1m2,a1m1, false);

        Dataflow m2_m3 = new Dataflow(a1m2,a1m3,true);
        Dataflow m3_m2 = new Dataflow(a1m3,a1m2, false);

        Dataflow m3_m4 = new Dataflow(a1m3,a1m4,true);
        Dataflow m4_m3 = new Dataflow(a1m4,a1m3, false);

        List<Dataflow> dataflows = Arrays.asList(new Dataflow[]{m1_m2, m2_m1,m2_m3,m3_m2,m3_m4,m4_m3});

        CompositeService service1 = new CompositeService("APP1_S1",Arrays.asList(new Microservice[]{a1m1, a1m2,a1m3,a1m4}),
                Arrays.asList(Arrays.asList(new Dataflow[]{m1_m2, m2_m3, m3_m2, m2_m1,m3_m4,m4_m3})));

        List<CompositeService> compositeServices = Arrays.asList(new CompositeService[]{service1});

        Application application = new Application(applicationId);
        application.initModel(microservices, compositeServices, dataflows);

        /**
         * Deployment Related Resources
         */
        List<Command> commands = new ArrayList<>();

        // application level commands
        Map<String,String> nsLabels = new HashMap<>();
        nsLabels.put("istio-injection","enabled");
        Namespace ns = new Namespace(applicationId, "app1", nsLabels);
        Command namespace = new Command(CommandTypeEnum.CREATE_NS, CommandLevelEnum.APP_LEVEL, ResourceEnum.NS, ns);
        commands.add(namespace);

        Role role = new Role("app1-role.yaml", "app1","app1");
        Command addRole = new Command(CommandTypeEnum.CREATE_PERMISSIONS,CommandLevelEnum.APP_LEVEL, ResourceEnum.ROLE,role);
        commands.add(addRole);

        Rolebinding rolebinding = new Rolebinding("app1-rolebinding.yaml", "app1", "app1");
        Command rb = new Command(CommandTypeEnum.CREATE_PERMISSIONS, CommandLevelEnum.APP_LEVEL, ResourceEnum.ROLEBINDING, rolebinding);
        commands.add(rb);

        // composite service level commands
        // (clusters hosting microservices that consume other microservices
        // should have Kubernetes Services deployed for consumed microservices, so that the microservice discovery can happen )
        Service m1Service = new Service("app1-m1-service.yaml",ns.getName(), Arrays.asList(new String[]{"APP1_m1"}));
        Command m1ServiceCommand = new Command(CommandTypeEnum.CREATE_S, CommandLevelEnum.COMPOSITE_SERVICE_LEVEL, ResourceEnum.SERVICE, m1Service);
        commands.add(m1ServiceCommand);

        IstioVirtualService m1VS = new IstioVirtualService("app1-m1-vs.yaml",ns.getName(), IstioVirtualService.Protocols.HTTP, Arrays.asList(new String[]{"APP1_m1"}));
        Command m1VSCommand = new Command(CommandTypeEnum.CREATE_VS, CommandLevelEnum.COMPOSITE_SERVICE_LEVEL, ResourceEnum.VS, m1VS);
        commands.add(m1VSCommand);

        IstioDestinationRule m1dr = new IstioDestinationRule("app1-m1-dr.yaml",ns.getName(), Arrays.asList(new String[]{"APP1_m1"}));
        Command m1drCommand = new Command(CommandTypeEnum.CREATE_DR, CommandLevelEnum.COMPOSITE_SERVICE_LEVEL, ResourceEnum.DR, m1dr);
        commands.add(m1drCommand);

        Gateway m1GW = new Gateway("app1-gateway.yaml",ns.getName(), Arrays.asList(new String[]{"APP1_m1"}));
        Command app1GWCommand = new Command(CommandTypeEnum.CREATE_GW, CommandLevelEnum.COMPOSITE_SERVICE_LEVEL, ResourceEnum.GW, m1GW);
        commands.add(app1GWCommand);

        /*********************/

        Service m2S = new Service("app1-m2-service.yaml",ns.getName(),Arrays.asList(new String[]{"APP1_m2"}));
        Command m2SCommand = new Command(CommandTypeEnum.CREATE_S, CommandLevelEnum.COMPOSITE_SERVICE_LEVEL, ResourceEnum.SERVICE, m2S);
        commands.add(m2SCommand);

        IstioVirtualService m2vs = new IstioVirtualService("app1-m2-vs.yaml",ns.getName(), IstioVirtualService.Protocols.HTTP, Arrays.asList(new String[]{"APP1_m2"}));
        Command m2vsCommand = new Command(CommandTypeEnum.CREATE_VS, CommandLevelEnum.COMPOSITE_SERVICE_LEVEL, ResourceEnum.VS, m2vs);
        commands.add(m2vsCommand);

        IstioDestinationRule m2dr = new IstioDestinationRule("app1-m2-dr.yaml",ns.getName(), Arrays.asList(new String[]{"APP1_m2"}));
        Command m2drCommand = new Command(CommandTypeEnum.CREATE_DR, CommandLevelEnum.COMPOSITE_SERVICE_LEVEL, ResourceEnum.DR, m2dr);
        commands.add(m2drCommand);

        /*********************/

        Service m3S = new Service("app1-m3-service.yaml",ns.getName(),Arrays.asList(new String[]{"APP1_m3"}));
        Command m3SCommand = new Command(CommandTypeEnum.CREATE_S, CommandLevelEnum.COMPOSITE_SERVICE_LEVEL, ResourceEnum.SERVICE, m3S);
        commands.add(m3SCommand);

        IstioVirtualService m3vs = new IstioVirtualService("app1-m3-vs.yaml",ns.getName(), IstioVirtualService.Protocols.HTTP, Arrays.asList(new String[]{"APP1_m3"}));
        Command m3vsCommand = new Command(CommandTypeEnum.CREATE_VS, CommandLevelEnum.COMPOSITE_SERVICE_LEVEL, ResourceEnum.VS, m3vs);
        commands.add(m3vsCommand);

        IstioDestinationRule m3dr = new IstioDestinationRule("app1-m3-dr.yaml",ns.getName(), Arrays.asList(new String[]{"APP1_m3"}));
        Command m3drCommand = new Command(CommandTypeEnum.CREATE_DR, CommandLevelEnum.COMPOSITE_SERVICE_LEVEL, ResourceEnum.DR, m3dr);
        commands.add(m3drCommand);

        /*********************/

        Service m4S = new Service("app1-m4-service.yaml",ns.getName(),Arrays.asList(new String[]{"APP1_m4"}));
        Command m4SCommand = new Command(CommandTypeEnum.CREATE_S, CommandLevelEnum.COMPOSITE_SERVICE_LEVEL, ResourceEnum.SERVICE, m4S);
        commands.add(m4SCommand);

        IstioVirtualService m4vs = new IstioVirtualService("app1-m4-vs.yaml",ns.getName(), IstioVirtualService.Protocols.HTTP, Arrays.asList(new String[]{"APP1_m4"}));
        Command m4vsCommand = new Command(CommandTypeEnum.CREATE_VS, CommandLevelEnum.COMPOSITE_SERVICE_LEVEL, ResourceEnum.VS, m4vs);
        commands.add(m4vsCommand);

        IstioDestinationRule m4dr = new IstioDestinationRule("app1-m4-dr.yaml",ns.getName(), Arrays.asList(new String[]{"APP1_m4"}));
        Command m4drCommand = new Command(CommandTypeEnum.CREATE_DR, CommandLevelEnum.COMPOSITE_SERVICE_LEVEL, ResourceEnum.DR, m4dr);
        commands.add(m4drCommand);

        /**********************/


        //microservice level (each microservice instance is deployed using Pod definitions)
        ConfigMap cm1 = new ConfigMap("a1m1-config.yaml",ns.getName(),"APP1_m1");
        Command cm1Command = new Command(CommandTypeEnum.CREATE_CONFIGMAP, CommandLevelEnum.MICROSERVICE_INSTANCE_LEVEL, ResourceEnum.CONFIGMAP, cm1);
        commands.add(cm1Command);

        ConfigMap cm2 = new ConfigMap("a1m2-config.yaml",ns.getName(),"APP1_m2");
        Command cm2Command = new Command(CommandTypeEnum.CREATE_CONFIGMAP, CommandLevelEnum.MICROSERVICE_INSTANCE_LEVEL, ResourceEnum.CONFIGMAP, cm2);
        commands.add(cm2Command);

        ConfigMap cm3 = new ConfigMap("a1m3-config.yaml",ns.getName(),"APP1_m3");
        Command cm3Command = new Command(CommandTypeEnum.CREATE_CONFIGMAP, CommandLevelEnum.MICROSERVICE_INSTANCE_LEVEL, ResourceEnum.CONFIGMAP, cm3);
        commands.add(cm3Command);

        ConfigMap cm4 = new ConfigMap("a1m4-config.yaml",ns.getName(),"APP1_m4");
        Command cm4Command = new Command(CommandTypeEnum.CREATE_CONFIGMAP, CommandLevelEnum.MICROSERVICE_INSTANCE_LEVEL, ResourceEnum.CONFIGMAP, cm4);
        commands.add(cm4Command);

        Pod m1 = new Pod("app1-m1.yaml",ns.getName(),"APP1_m1");
        Command m1Command = new Command(CommandTypeEnum.CREATE_M, CommandLevelEnum.MICROSERVICE_INSTANCE_LEVEL, ResourceEnum.POD, m1);
        commands.add(m1Command);

        Pod m2 = new Pod("app1-m2.yaml",ns.getName(),"APP1_m2");
        Command m2Command = new Command(CommandTypeEnum.CREATE_M, CommandLevelEnum.MICROSERVICE_INSTANCE_LEVEL, ResourceEnum.POD, m2);
        commands.add(m2Command);

        Pod m3 = new Pod("app1-m3.yaml",ns.getName(),"APP1_m3");
        Command m3Command = new Command(CommandTypeEnum.CREATE_M, CommandLevelEnum.MICROSERVICE_INSTANCE_LEVEL, ResourceEnum.POD, m3);
        commands.add(m3Command);

        Pod m4 = new Pod("app1-m4.yaml",ns.getName(),"APP1_m4");
        Command m4Command = new Command(CommandTypeEnum.CREATE_M, CommandLevelEnum.MICROSERVICE_INSTANCE_LEVEL, ResourceEnum.POD, m4);
        commands.add(m4Command);


        utils.updateYamlUrl(commands, applicationId);
        application.initCommands(commands);
        return application;
    }
}
