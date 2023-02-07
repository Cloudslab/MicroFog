package com.microfog.controlengine.model.domainObjects;

import com.microfog.controlengine.model.deomainObjectsLight.CompositeServiceL;
import com.microfog.controlengine.model.deomainObjectsLight.DataflowL;
import com.microfog.controlengine.model.deomainObjectsLight.MicroserviceL;
import com.microfog.controlengine.model.deployment.*;
import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.Luminance;
import org.jgrapht.alg.util.Pair;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Application contains two types of data
 * 1. Application Model (microservices, services, dataflows, resource requirements for microservices, QoS requirements for services)
 * 2. Deployment Data (deployment commands at application level, at microservice level, at routing level)
 */
public class Application implements Serializable {
     String applicationId; //links to placement request

     // model
     List<Microservice> microservicesList = new ArrayList<>();
     List<CompositeService> compositeServices = new ArrayList<>();
     List<Dataflow> dataflows = new ArrayList<>();
     //DAG representation
     ApplicationDAG applicationDAG = new ApplicationDAG();

     // qos
     List<QoSParameter> qoSParameters = new ArrayList<>();

     // applevel deployment data
     List<Command> appLevelCommand = new ArrayList<>();

     Map<String,Microservice> microserviceMap = new HashMap<>();

     List<Microservice> sortedDAG = new ArrayList<>();

     List<Microservice> ingressMicroservices = new ArrayList<>();

     public Application(String applicationId){
          this.applicationId = applicationId;
     }

     public void initModel(List<Microservice> microservicesList, List<CompositeService> compositeServices, List<Dataflow> dataflows  ){
          this.microservicesList = microservicesList;
          this.compositeServices = compositeServices;
          this.dataflows = dataflows;
          generateMicroserviceMap(microservicesList);
          generateDAG();
          generateIngressMicroservices();
     }

     private void generateMicroserviceMap(List<Microservice> microservicesList) {
          for(Microservice m:microservicesList){
               microserviceMap.put(m.getMicroserviceId(),m);
          }
     }

     public Microservice getMicroserviceById(String mNme){
          return microserviceMap.get(mNme);
     }

     private void generateDAG() {
          applicationDAG.addVertices(microservicesList);
          List<Pair<Microservice,Microservice>> edges = new ArrayList<>();
          for(Dataflow d:dataflows){
               if(d.isInvokationDirection()){
                    Pair<Microservice,Microservice> edge = new Pair<>(d.startMicroservice,d.endMicroservice);
                    edges.add(edge);
               }
          }
          applicationDAG.addEdges(edges);
          sortDAG();
     }

     private void sortDAG() {
          List<Microservice> currentLevel = applicationDAG.getRootMicroservices();
          while (!currentLevel.isEmpty()) {
               sortedDAG.addAll(currentLevel);
               List<Microservice> nextLevel = new ArrayList<>();
               for (Microservice m : currentLevel) {
                    nextLevel.addAll(applicationDAG.getConsumedMicroservices(m));
               }
               currentLevel = nextLevel;
          }
     }

     public void initCommands(List<Command> commands){
          for(Command command:commands){
               if(command.getCommandLevel().equals(CommandLevelEnum.APP_LEVEL))
                    appLevelCommand.add(command);
               else if (command.getCommandLevel().equals(CommandLevelEnum.MICROSERVICE_INSTANCE_LEVEL)){
                    String commandMapper = ((MicroserviceLevelResource)command.getResource()).getMicroserviceIdentifier();
                    getMicroservice(commandMapper).addCommandsMlevel(command);
               }
               else if(command.getCommandLevel().equals(CommandLevelEnum.COMPOSITE_SERVICE_LEVEL)){
                    List<String> commandMapper = ((ServiceLevelResource)command.getResource()).getMicroserviceIdentifier();
                    for(String m:commandMapper)
                         getMicroservice(m).addCommandsSlevel(command);
               }
          }
     }

     public Microservice getMicroservice(String microserviceName) {
          for(Microservice m:microservicesList){
               if(m.getMicroserviceId().equals(microserviceName)){
                    return m;
               }
          }
          return null;
     }

     public String getApplicationId() {
          return applicationId;
     }


     public List<CompositeService> getCompositeServices() {
          return compositeServices;
     }

     public List<Dataflow> getDataflows() {
          return dataflows;
     }

     public List<Microservice> getMicroservicesList() {
          return microservicesList;
     }

     public List<QoSParameter> getQoSParameters() {
          return qoSParameters;
     }

     public ApplicationDAG getApplicationDAG() {
          return applicationDAG;
     }

     public List<Command> getAppLevelCommand() {
          return appLevelCommand;
     }

     public List<MicroserviceL> getMicroservicesL(){
          List<MicroserviceL> microserviceL = new ArrayList<>();
          for(Microservice m: microservicesList)
               microserviceL.add(new MicroserviceL(m.getMicroserviceId(),m.getRam(),m.getCpu(),m.getStorage(),m.getThroughput()));
          return getMicroservicesL();
     }

     public List<CompositeServiceL> getCompositeServicesL() {
          List<CompositeServiceL> compositeServiceL = new ArrayList<>();
          for (CompositeService c:compositeServices){
               compositeServiceL.add(new CompositeServiceL(c));
          }
          return compositeServiceL;
     }

     public List<DataflowL> getDataflowsL() {
          List<DataflowL> dataflowL = new ArrayList<>();
          for (Dataflow d:dataflows){
               dataflowL.add(new DataflowL(d));
          }
          return dataflowL;
     }

     public List<String> getMicroserviceids(){
          return new ArrayList<>(microserviceMap.keySet());
     }

    public Double getQoSValue(String throughput) {
          for(QoSParameter q:qoSParameters){
               if(q.parameter.equals(throughput))
                    return q.getValue();
          }
          return null;
    }

    public List<Microservice> getSortedDAG() {
          return sortedDAG;
    }

    private void generateIngressMicroservices(){
          for(CompositeService compositeService:compositeServices){
               ingressMicroservices.add(compositeService.getDatapaths().get(0).get(0).startMicroservice);
          }
    }

     public List<Microservice> getIngressMicroservices() {
          return ingressMicroservices;
     }
}
