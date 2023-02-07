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

public class SmartHealthAppSetup extends ApplicationSetup {
    public SmartHealthAppSetup(Utils utils) {
        super(utils);
    }

    public Application generateApplicationObject() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        String applicationId = "hcApp";
        /** application model
         *  1. microservices (3 + FE)
         *  2. Dataflows
         *  3. composite services (2)
         */
        Microservice hcm1 = new Microservice("hcApp_m1", new HashMap<>(){
            {
                put(Microservice.CPU, new BigDecimal(0.5));
                put(Microservice.RAM, utils.getbytesForMib(100));
                put(Microservice.THROUGHPUT, new BigDecimal(100));
            }
        });
        Microservice hcm2 = new Microservice("hcApp_m2", new HashMap<>(){
            {
                put(Microservice.CPU, new BigDecimal(1.0));
                put(Microservice.RAM, utils.getbytesForMib(100));
                put(Microservice.THROUGHPUT, new BigDecimal(100));
            }
        });
        Microservice hcm3 = new Microservice("hcApp_m3", new HashMap<>(){
            {
                put(Microservice.CPU, new BigDecimal(1.0));
                put(Microservice.RAM, utils.getbytesForMib(100));
                put(Microservice.THROUGHPUT, new BigDecimal(100));
            }
        });

        List<Microservice> microservices = Arrays.asList(new Microservice[]{hcm1,hcm2,hcm3});

        Dataflow m1_m2 = new Dataflow(hcm1,hcm2,true);
        Dataflow m2_m1 = new Dataflow(hcm2,hcm1, false);

        Dataflow m1_m3 = new Dataflow(hcm1,hcm3,true);

        List<Dataflow> dataflows = Arrays.asList(new Dataflow[]{m1_m2, m2_m1,m1_m3});

        CompositeService service1 = new CompositeService("hcAPP_S1",Arrays.asList(new Microservice[]{hcm1, hcm2}),
                Arrays.asList(Arrays.asList(new Dataflow[]{m1_m2, m2_m1})));

        CompositeService service2 = new CompositeService("hcAPP_S2",Arrays.asList(new Microservice[]{hcm1,hcm3}),
                Arrays.asList(Arrays.asList(new Dataflow[]{m1_m3})));

        List<CompositeService> compositeServices = Arrays.asList(new CompositeService[]{service1,service2});

        Application application = new Application(applicationId);
        application.initModel(microservices, compositeServices, dataflows);

        /**
         * Deployment Related Resources
         */
        List<Command> commands = new ArrayList<>();

        // application level commands
        Map<String,String> nsLabels = new HashMap<>();
        nsLabels.put("istio-injection","enabled");
        Namespace ns = new Namespace(applicationId, "hcapp", nsLabels);
        Command namespace = new Command(CommandTypeEnum.CREATE_NS, CommandLevelEnum.APP_LEVEL, ResourceEnum.NS, ns);
        commands.add(namespace);

        Role role = new Role("hcApp-role.yaml", applicationId,"hcapp");
        Command addRole = new Command(CommandTypeEnum.CREATE_PERMISSIONS,CommandLevelEnum.APP_LEVEL, ResourceEnum.ROLE,role);
        commands.add(addRole);

        Rolebinding rolebinding = new Rolebinding("hcApp-rolebinding.yaml", applicationId, "hcapp");
        Command rb = new Command(CommandTypeEnum.CREATE_PERMISSIONS, CommandLevelEnum.APP_LEVEL, ResourceEnum.ROLEBINDING, rolebinding);
        commands.add(rb);

        // composite service level commands
        // (clusters hosting microservices that consume other microservices
        // should have Kubernetes Services deployed for consumed microservices, so that the microservice discovery can happen )
        Service m1Service = new Service("hcApp-m1-service.yaml",ns.getName(), Arrays.asList(new String[]{"hcApp_m1"}));
        Command m1ServiceCommand = new Command(CommandTypeEnum.CREATE_S, CommandLevelEnum.COMPOSITE_SERVICE_LEVEL, ResourceEnum.SERVICE, m1Service);
        commands.add(m1ServiceCommand);

        IstioVirtualService m1VS = new IstioVirtualService("hcApp-m1-vs.yaml",ns.getName(), IstioVirtualService.Protocols.HTTP, Arrays.asList(new String[]{"hcApp_m1"}));
        Command m1VSCommand = new Command(CommandTypeEnum.CREATE_VS, CommandLevelEnum.COMPOSITE_SERVICE_LEVEL, ResourceEnum.VS, m1VS);
        commands.add(m1VSCommand);

        IstioDestinationRule m1dr = new IstioDestinationRule("hcApp-m1-dr.yaml",ns.getName(), Arrays.asList(new String[]{"hcApp_m1"}));
        Command m1drCommand = new Command(CommandTypeEnum.CREATE_DR, CommandLevelEnum.COMPOSITE_SERVICE_LEVEL, ResourceEnum.DR, m1dr);
        commands.add(m1drCommand);

        Gateway m1GW = new Gateway("hcApp-gateway.yaml",ns.getName(), Arrays.asList(new String[]{"hcApp_m1"}));
        Command app1GWCommand = new Command(CommandTypeEnum.CREATE_GW, CommandLevelEnum.COMPOSITE_SERVICE_LEVEL, ResourceEnum.GW, m1GW);
        commands.add(app1GWCommand);

        /*********************/

        Service m2S = new Service("hcApp-m2-service.yaml",ns.getName(),Arrays.asList(new String[]{"hcApp_m2"}));
        Command m2SCommand = new Command(CommandTypeEnum.CREATE_S, CommandLevelEnum.COMPOSITE_SERVICE_LEVEL, ResourceEnum.SERVICE, m2S);
        commands.add(m2SCommand);

        IstioVirtualService m2vs = new IstioVirtualService("hcApp-m2-vs.yaml",ns.getName(), IstioVirtualService.Protocols.HTTP, Arrays.asList(new String[]{"hcApp_m2"}));
        Command m2vsCommand = new Command(CommandTypeEnum.CREATE_VS, CommandLevelEnum.COMPOSITE_SERVICE_LEVEL, ResourceEnum.VS, m2vs);
        commands.add(m2vsCommand);

        IstioDestinationRule m2dr = new IstioDestinationRule("hcApp-m2-dr.yaml",ns.getName(), Arrays.asList(new String[]{"hcApp_m2"}));
        Command m2drCommand = new Command(CommandTypeEnum.CREATE_DR, CommandLevelEnum.COMPOSITE_SERVICE_LEVEL, ResourceEnum.DR, m2dr);
        commands.add(m2drCommand);

        /*********************/

        Service m3S = new Service("hcApp-m3-service.yaml",ns.getName(),Arrays.asList(new String[]{"hcApp_m3"}));
        Command m3SCommand = new Command(CommandTypeEnum.CREATE_S, CommandLevelEnum.COMPOSITE_SERVICE_LEVEL, ResourceEnum.SERVICE, m3S);
        commands.add(m3SCommand);

        IstioVirtualService m3vs = new IstioVirtualService("hcApp-m3-vs.yaml",ns.getName(), IstioVirtualService.Protocols.HTTP, Arrays.asList(new String[]{"hcApp_m3"}));
        Command m3vsCommand = new Command(CommandTypeEnum.CREATE_VS, CommandLevelEnum.COMPOSITE_SERVICE_LEVEL, ResourceEnum.VS, m3vs);
        commands.add(m3vsCommand);

        IstioDestinationRule m3dr = new IstioDestinationRule("hcApp-m3-dr.yaml",ns.getName(), Arrays.asList(new String[]{"hcApp_m3"}));
        Command m3drCommand = new Command(CommandTypeEnum.CREATE_DR, CommandLevelEnum.COMPOSITE_SERVICE_LEVEL, ResourceEnum.DR, m3dr);
        commands.add(m3drCommand);

        /**********************/


        //microservice level (each microservice instance is deployed using Pod definitions)
        ConfigMap cm1 = new ConfigMap("hcm1-config.yaml",ns.getName(),"hcApp_m1");
        Command cm1Command = new Command(CommandTypeEnum.CREATE_CONFIGMAP, CommandLevelEnum.MICROSERVICE_INSTANCE_LEVEL, ResourceEnum.CONFIGMAP, cm1);
        commands.add(cm1Command);

        ConfigMap cm2 = new ConfigMap("hcm2-config.yaml",ns.getName(),"hcApp_m2");
        Command cm2Command = new Command(CommandTypeEnum.CREATE_CONFIGMAP, CommandLevelEnum.MICROSERVICE_INSTANCE_LEVEL, ResourceEnum.CONFIGMAP, cm2);
        commands.add(cm2Command);

        ConfigMap cm3 = new ConfigMap("hcm3-config.yaml",ns.getName(),"hcApp_m3");
        Command cm3Command = new Command(CommandTypeEnum.CREATE_CONFIGMAP, CommandLevelEnum.MICROSERVICE_INSTANCE_LEVEL, ResourceEnum.CONFIGMAP, cm3);
        commands.add(cm3Command);



        Pod m1 = new Pod("hcApp-m1.yaml",ns.getName(),"hcApp_m1");
        Command m1Command = new Command(CommandTypeEnum.CREATE_M, CommandLevelEnum.MICROSERVICE_INSTANCE_LEVEL, ResourceEnum.POD, m1);
        commands.add(m1Command);

        Pod m2 = new Pod("hcApp-m2.yaml",ns.getName(),"hcApp_m2");
        Command m2Command = new Command(CommandTypeEnum.CREATE_M, CommandLevelEnum.MICROSERVICE_INSTANCE_LEVEL, ResourceEnum.POD, m2);
        commands.add(m2Command);

        Pod m3 = new Pod("hcApp-m3.yaml",ns.getName(),"hcApp_m3");
        Command m3Command = new Command(CommandTypeEnum.CREATE_M, CommandLevelEnum.MICROSERVICE_INSTANCE_LEVEL, ResourceEnum.POD, m3);
        commands.add(m3Command);




        utils.updateYamlUrl(commands, applicationId);
        application.initCommands(commands);
        return application;
    }
}
