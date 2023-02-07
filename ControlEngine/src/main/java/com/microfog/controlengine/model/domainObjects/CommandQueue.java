//package com.microfog.controlengine.model.domainObjects;
//
//import com.microfog.controlengine.controllers.CommandHandler;
//import com.microfog.controlengine.model.deployment.Command;
//import io.quarkus.scheduler.Scheduled;
//import org.jboss.logging.Logger;
//
//import javax.inject.Inject;
//import javax.inject.Singleton;
//import java.util.List;
//import java.util.Queue;
//import java.util.concurrent.ConcurrentLinkedDeque;
//
//@Singleton
//public class CommandQueue {
//
//    private final static Logger LOGGER = Logger.getLogger(CommandQueue.class.getName());
//
//    //todo double check concurrency for the queue;
//    private static Queue<Command> commandQueue = new ConcurrentLinkedDeque<>();
//
//    @Inject
//    CommandHandler commandHandler;
//
//    public synchronized boolean addToQueue(Command command){
//        return commandQueue.add(command);
//    }
//
//    public synchronized void addToQueue(List<Command> command){
//        commandQueue.addAll(command);
//    }
//
//    @Scheduled(every="0.001s")
//    public void execute(){
//       while(!commandQueue.isEmpty()){
//           Command command = commandQueue.poll();
//           commandHandler.executeCommand(command);
//       }
//    }
//
//    public int getPrQueueSize(){
//        return commandQueue.size();
//    }
//}
