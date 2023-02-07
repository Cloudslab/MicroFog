package com.microfog.controlengine.model.domainObjects;

import com.microfog.controlengine.model.deployment.Command;
import com.microfog.controlengine.model.deployment.CommandTypeEnum;
import org.checkerframework.checker.units.qual.A;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Microservice implements Serializable {

    //resources required for the istio proxy
    BigDecimal proxyOverheadCPU = new BigDecimal(0.1);

    BigDecimal proxyOverheadMemory = getbytesForMib(128);

    String microserviceId;

    //resources
    BigDecimal ram;
    BigDecimal cpu;
    BigDecimal storage;
    BigDecimal throughput; //request rate supported by resources (per second)

    public static final String CPU = "cpu";
    public static final String RAM = "ram";
    public static final String STORAGE = "storage";
    public static final String THROUGHPUT = "throughput";

    List<Command> commandsMLevel = new ArrayList<>();
    List<Command> commandsSLevel = new ArrayList<>();

    public Microservice(String microserviceId) {
        this.microserviceId = microserviceId;
    }

    public Microservice(String microserviceId, Map<String, BigDecimal> resources) {
        this.microserviceId = microserviceId;
        if (resources.containsKey(CPU))
            this.cpu = resources.get(CPU);
        if (resources.containsKey(RAM))
            this.ram = resources.get(RAM);
        if (resources.containsKey(STORAGE))
            this.storage = resources.get(STORAGE);
        if (resources.containsKey(THROUGHPUT))
            this.throughput = resources.get(THROUGHPUT);
    }

    public BigDecimal getTotalCpu() {
        return cpu.add(proxyOverheadCPU);
    }

    public BigDecimal getCpu() {
        return cpu;
    }

    public BigDecimal getTotalRam() {
        return ram.add(proxyOverheadMemory);
    }

    public BigDecimal getRam() {
        return ram;
    }

    public BigDecimal getStorage() {
        return storage;
    }

    public BigDecimal getThroughput() {
        return throughput;
    }

    public String getMicroserviceId() {
        return microserviceId;
    }

    public void addCommandsMlevel(Command command) {
        commandsMLevel.add(command);
    }

    public void addCommandsSlevel(Command command) {
        commandsSLevel.add(command);
    }

    public List<Command> getCommandsMLevel() {
        return commandsMLevel;
    }

    public List<Command> getCommandsSLevelLB( ) {
        List<Command> commands = new ArrayList<>();
        for(Command c:commandsSLevel){
            if(c.getCommandType().equals(CommandTypeEnum.CREATE_VS) || c.getCommandType().equals(CommandTypeEnum.CREATE_DR))
                commands.add(c);
        }
        return commands;
    }

    public List<Command> getCommandsSLevelNoLB( ) {
        List<Command> commands = new ArrayList<>();
        for(Command c:commandsSLevel){
            if(c.getCommandType().equals(CommandTypeEnum.CREATE_VS) || c.getCommandType().equals(CommandTypeEnum.CREATE_DR))
                continue;
            else
                commands.add(c);
        }
        return commands;
    }

    public List<Command> getCommandsSLevelAll() {
        return commandsSLevel;
    }

    public BigDecimal getbytesForMib(int i) {
        return  new BigDecimal(i*1024*1024);
    }
}
